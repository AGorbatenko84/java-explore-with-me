package ru.practicum.mainservice.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.practicum.mainservice.dto.event.*;
import ru.practicum.mainservice.dto.participation_request.EventRequestStatusUpdateRequest;
import ru.practicum.mainservice.dto.participation_request.EventRequestStatusUpdateResult;
import ru.practicum.mainservice.dto.participation_request.ParticipationRequestDto;
import ru.practicum.mainservice.dto.participation_request.ParticipationRequestStatus;
import ru.practicum.mainservice.exception.*;
import ru.practicum.mainservice.mapper.EventMapper;
import ru.practicum.mainservice.mapper.ParticipationRequestMapper;
import ru.practicum.mainservice.model.*;
import ru.practicum.mainservice.model.QEvent;
import ru.practicum.mainservice.repository.CategoryRepository;
import ru.practicum.mainservice.repository.EventRepository;
import ru.practicum.mainservice.repository.ParticipationRequestRepository;
import ru.practicum.mainservice.repository.UserRepository;
import ru.practicum.mainservice.service.EventService;
import ru.practicum.statsservice.client.StatsServiceClient;
import ru.practicum.statsservice.dto.EndpointHitDto;
import ru.practicum.statsservice.dto.ViewStatsDto;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EventServiceImpl implements EventService {

    private static final Sort SORT_BY_EVENT_DATE = Sort.by(Sort.Direction.ASC, "eventDate");
    private static final String APP_NAME = "ewm-main-service";
    private final EventRepository eventRepository;
    private final ParticipationRequestRepository requestRepository;
    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventMapper eventMapper;
    private final ParticipationRequestMapper requestMapper;
    private final StatsServiceClient statsServiceClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    @Transactional
    public EventFullDto create(NewEventDto dto, Long userId) {
        checkEventDate(dto.getEventDate());
        User initiator = userRepository.findById(userId).orElseThrow(() -> {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        });
        Long id = dto.getCategory();
        Category category = categoryRepository.findById(id).orElseThrow(() -> {
            throw new NotFoundException(String.format("Категория с id %d не найдена", id));
        });
        Event event = eventMapper.toEntity(dto);
        event.setInitiator(initiator);
        event.setCategory(category);
        if (event.getPaid() == null) {
            event.setPaid(false);
        }
        if (event.getRequestModeration() == null) {
            event.setRequestModeration(true);
        }
        if (event.getParticipantLimit() == null) {
            event.setParticipantLimit(0);
        }
        Event savedEvent = eventRepository.save(event);
        log.info("Событие с id {} создано", savedEvent.getId());
        return eventMapper.toFullDto(savedEvent);
    }

    @Override
    public List<EventShortDto> getEventsByUserId(Long userId, Integer from, Integer size) {
        userRepository.findById(userId).orElseThrow(() -> {
            throw new NotFoundException("Пользователь с id " + userId + " не найден");
        });
        PageRequest pageRequest = PageRequest.of(from / size, size);
        List<Event> events = eventRepository.findEventsByInitiatorId(userId, pageRequest).getContent();
        List<EventShortDto> dtos = eventMapper.toShortDtoList(events);
        setStatsViewsToEventShortDtos(dtos);
        return dtos;
    }

    @Override
    public EventFullDto getUserEventById(Long userId, Long eventId) {
        Event event = eventRepository.findEventByIdAndInitiatorId(eventId, userId).orElseThrow(() -> {
            throw new NotFoundException("Событие с id " + eventId + " не найдено");
        });
        EventFullDto dto = eventMapper.toFullDto(event);
        setStatsViewsToEventFullDtos(List.of(dto));
        return dto;
    }

    @Override
    @Transactional
    public EventFullDto patch(UpdateEventUserRequest request, Long userId, Long eventId) {
        if (Objects.nonNull(request.getEventDate())) {
            checkEventDate(request.getEventDate());
        }
        Event event = eventRepository.findEventByIdAndInitiatorId(eventId, userId).orElseThrow(() -> {
            throw new NotFoundException("Событие с id " + eventId + " не найдено");
        });
        if (event.getState() == EventState.PUBLISHED) {
            throw new ConflictException("Можно изменить только отмененные или ожидающие публикации события");
        }
        eventMapper.patchEventByUserRequest(request, event);
        if (Objects.nonNull(request.getStateAction())) {
            if (request.getStateAction() == UserStateAction.SEND_TO_REVIEW) {
                event.setState(EventState.PENDING);
            }
            if (request.getStateAction() == UserStateAction.CANCEL_REVIEW) {
                event.setState(EventState.CANCELED);
            }
        }
        if (Objects.nonNull(request.getCategory())) {
            Long id = request.getCategory();
            Category category = categoryRepository.findById(id).orElseThrow(() -> {
                throw new NotFoundException(String.format("Категория с id %d не найдена", id));
            });
            event.setCategory(category);
        }
        return eventMapper.toFullDto(event);
    }

    @Override
    public List<ParticipationRequestDto> getUserEventRequests(Long userId, Long eventId) {
        List<ParticipationRequest> requests = requestRepository.findAllByEventInitiatorIdAndEventId(userId, eventId);
        return requestMapper.toDtoList(requests);
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateEventRequestStatus(
            Long userId, Long eventId, EventRequestStatusUpdateRequest dto) {
        Event event = eventRepository.findEventByIdAndInitiatorId(eventId, userId).orElseThrow(() -> {
            throw new NotFoundException("Событие с id " + eventId + " не найдено");
        });
        if (event.getParticipantLimit() != 0 && (event.getParticipantLimit() - event.getConfirmedRequests()) <= 0) {
            throw new ConflictException("В этом мероприятии достигнут лимит участников");
        }
        List<ParticipationRequest> requests = requestRepository.findParticipationRequestByEventIdAndIdIn(
                eventId, dto.getRequestIds());
        if (event.getParticipantLimit().equals(0)) {
            return setRequestStatusEventWithoutLimit(dto.getStatus(), requests, event);
        } else {
            return setRequestStatusEventWithLimit(dto.getStatus(), requests, event);
        }
    }

    @Override
    public List<EventShortDto> getAll(
            GetEventsRequest request, Integer from, Integer size, HttpServletRequest httpRequest) {
        PageRequest page = PageRequest.of(from / size, size, SORT_BY_EVENT_DATE);
        LocalDateTime rangeStart = request.getRangeStart();
        LocalDateTime rangeEnd = request.getRangeEnd();
        if (rangeStart == null) {
            rangeStart = LocalDateTime.now().minusYears(100);
        }
        if (rangeEnd == null) {
            rangeEnd = LocalDateTime.now().plusYears(100);
        }
        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new BadRequestException("Время начала выборки должно быть после времени конца выборки.");
        }
        BooleanBuilder predicate = createPredicate(request);
        if (Objects.isNull(request.getRangeStart()) && Objects.isNull(request.getRangeEnd())) {
            predicate.and(QEvent.event.eventDate.after(LocalDateTime.now()));
        }
        List<EventShortDto> dtos = eventMapper.toShortDtoList(eventRepository.findAll(predicate, page).getContent());
        saveStats(httpRequest);
        setStatsViewsToEventShortDtos(dtos);
        if (Objects.nonNull(request.getSort())) {
            if (request.getSort().equalsIgnoreCase("views")) {
                dtos.sort(Comparator.comparing(EventShortDto::getViews));
            }
        }
        return dtos;
    }

    @Override
    public EventFullDto getById(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findById(eventId).orElseThrow(() -> {
            throw new NotFoundException("Событие с id " + eventId + " не найдено");
        });
        if (event.getPublishedOn() == null) {
            throw new NotFoundException("Событие с id " + eventId + " не найдено");
        }
        EventFullDto dto = eventMapper.toFullDto(event);
        saveStats(request);
        if (dto.getViews() == null) {
            dto.setViews(1);
        }
        return dto;
    }

    @Override
    public List<EventFullDto> getAllAdmin(GetEventsRequestAdmin request, Integer from, Integer size) {
        PageRequest page = PageRequest.of(from / size, size);
        BooleanBuilder predicate = createPredicateAdmin(request);
        return eventMapper.toFullDtoList(eventRepository.findAll(predicate, page).getContent());
    }

    @Override
    @Transactional
    public EventFullDto confirmOrReject(UpdateEventAdminRequest request, Long eventId) {
        checkEventDate(request.getEventDate());
        Event event = eventRepository.findById(eventId).orElseThrow(() -> {
            throw new NotFoundException("Событие с id " + eventId + " не найдено");
        });
        if (event.getState() != EventState.PUBLISHED) {
            if (Objects.nonNull(request.getStateAction())) {
                if (request.getStateAction() == AdminStateAction.PUBLISH_EVENT &&
                        event.getState() == EventState.PENDING) {
                    event.setState(EventState.PUBLISHED);
                    event.setPublishedOn(LocalDateTime.now());
                } else if (request.getStateAction() == AdminStateAction.REJECT_EVENT) {
                    event.setState(EventState.CANCELED);
                } else {
                    throw new WrongStateException(
                            "Не удается опубликовать событие, потому что оно находится в неправильном состоянии: "
                                    + event.getState());
                }
            }
            eventMapper.patchEventByAdminRequest(request, event);
            return eventMapper.toFullDto(eventRepository.save(event));
        }
        throw new WrongStateException(
                "Не удается опубликовать событие, потому что оно находится в неправильном состоянии: "
                        + event.getState());
    }

    private void checkEventDate(LocalDateTime dateTime) {
        if (Objects.nonNull(dateTime)) {
            if (dateTime.minusHours(2).isBefore(LocalDateTime.now())) {
                throw new EventDateNotValidException("Время проведения мероприятия не может быть ранее, " +
                        "чем через два часа с этого момента");
            }
        }
    }

    private EventRequestStatusUpdateResult setRequestStatusEventWithoutLimit(
            ParticipationRequestStatus status,
            List<ParticipationRequest> requests,
            Event event) {
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        for (ParticipationRequest req : requests) {
            if (req.getStatus() == RequestStatus.PENDING) {
                if (status == ParticipationRequestStatus.CONFIRMED) {
                    req.setStatus(RequestStatus.CONFIRMED);
                    event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                    result.getConfirmedRequests().add(requestMapper.toDto(req));
                } else {
                    req.setStatus(RequestStatus.REJECTED);
                    result.getRejectedRequests().add(requestMapper.toDto(req));
                }
            } else {
                throw new ConflictException("Можно подтверждать только ожидающие запросы");
            }
        }
        return result;
    }

    private EventRequestStatusUpdateResult setRequestStatusEventWithLimit(
            ParticipationRequestStatus status,
            List<ParticipationRequest> requests,
            Event event) {
        EventRequestStatusUpdateResult result = new EventRequestStatusUpdateResult();
        for (ParticipationRequest req : requests) {
            if (req.getStatus() != RequestStatus.PENDING) {
                throw new ConflictException("Можно подтверждать только ожидающие запросы");
            }
            if (status == ParticipationRequestStatus.CONFIRMED) {
                int freeSpace = event.getParticipantLimit() - event.getConfirmedRequests();
                if (freeSpace > 0) {
                    req.setStatus(RequestStatus.CONFIRMED);
                    event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                    result.getConfirmedRequests().add(requestMapper.toDto(req));
                } else {
                    req.setStatus(RequestStatus.REJECTED);
                    result.getRejectedRequests().add(requestMapper.toDto(req));
                }
            } else {
                req.setStatus(RequestStatus.REJECTED);
                result.getRejectedRequests().add(requestMapper.toDto(req));
            }
        }
        return result;
    }

    private BooleanBuilder createPredicate(GetEventsRequest req) {
        BooleanBuilder predicate = new BooleanBuilder();
        if (Objects.nonNull(req.getText())) {
            predicate.and(QEvent.event.annotation.likeIgnoreCase(req.getText())
                    .or(QEvent.event.description.likeIgnoreCase(req.getText())));
        }
        if (!CollectionUtils.isEmpty(req.getCategories())) {
            predicate.and(QEvent.event.category.id.in(req.getCategories()));
        }
        if (Objects.nonNull(req.getPaid())) {
            predicate.and(QEvent.event.paid.eq(req.getPaid()));
        }
        if (Objects.nonNull(req.getRangeStart())) {
            predicate.and(QEvent.event.eventDate.after(req.getRangeStart()));
        }
        if (Objects.nonNull(req.getRangeEnd())) {
            predicate.and(QEvent.event.eventDate.before(req.getRangeEnd()));
        }
        if (Boolean.TRUE.equals(req.getOnlyAvailable())) {
            predicate.and(QEvent.event.participantLimit.eq(0)
                    .or(QEvent.event.participantLimit.gt(QEvent.event.confirmedRequests)));
        }
        return predicate;
    }

    private BooleanBuilder createPredicateAdmin(GetEventsRequestAdmin req) {
        BooleanBuilder predicate = new BooleanBuilder();
        if (!CollectionUtils.isEmpty(req.getUsers())) {
            predicate.and(QEvent.event.initiator.id.in(req.getUsers()));
        }
        if (!CollectionUtils.isEmpty(req.getStates())) {
            predicate.and(QEvent.event.state.in(req.getStates()));
        }
        if (!CollectionUtils.isEmpty(req.getCategories())) {
            predicate.and(QEvent.event.category.id.in(req.getCategories()));
        }
        if (Objects.nonNull(req.getRangeStart())) {
            predicate.and(QEvent.event.eventDate.after(req.getRangeStart()));
        }
        if (Objects.nonNull(req.getRangeEnd())) {
            predicate.and(QEvent.event.eventDate.before(req.getRangeEnd()));
        }
        return predicate;
    }

    private Map<Long, Integer> getStatsViewsMap(List<Long> ids) {
        List<String> uris = ids
                .stream()
                .map(id -> "/events/" + id)
                .collect(Collectors.toList());
        List<ViewStatsDto> viewStatsDtos = objectMapper
                .convertValue(statsServiceClient.getAllStatsByUris(uris).getBody(), new TypeReference<>() {
                });
        Map<Long, Integer> eventIdViewsMap = new HashMap<>();
        if (!CollectionUtils.isEmpty(viewStatsDtos)) {
            viewStatsDtos.forEach(d -> {
                String[] lines = d.getUri().split("/");
                Long id = Long.parseLong(lines[2]);
                eventIdViewsMap.put(id, d.getHits());
            });
        }
        return eventIdViewsMap;
    }

    private void setStatsViewsToEventShortDtos(List<EventShortDto> dtos) {
        List<Long> ids = dtos.stream().map(EventShortDto::getId).collect(Collectors.toList());
        Map<Long, Integer> viewsMap = getStatsViewsMap(ids);
        dtos.forEach(d -> d.setViews(viewsMap.getOrDefault(d.getId(), 0)));
    }

    private void setStatsViewsToEventFullDtos(List<EventFullDto> dtos) {
        List<Long> ids = dtos.stream().map(EventFullDto::getId).collect(Collectors.toList());
        Map<Long, Integer> viewsMap = getStatsViewsMap(ids);
        dtos.forEach(d -> d.setViews(viewsMap.getOrDefault(d.getId(), 0)));
    }

    private void saveStats(HttpServletRequest request) {
        EndpointHitDto hit = new EndpointHitDto();
        hit.setApp(APP_NAME);
        hit.setUri(request.getRequestURI());
        hit.setIp(request.getRemoteAddr());
        hit.setTimestamp(LocalDateTime.now());
        statsServiceClient.create(hit);
    }
}
