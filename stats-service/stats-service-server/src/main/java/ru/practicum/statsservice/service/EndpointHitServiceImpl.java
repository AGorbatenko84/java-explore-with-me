package ru.practicum.statsservice.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.practicum.statsservice.dto.EndpointHitDto;
import ru.practicum.statsservice.dto.GetStatsRequest;
import ru.practicum.statsservice.dto.ViewStatsDto;
import ru.practicum.statsservice.exception.ValidationException;
import ru.practicum.statsservice.mapper.EndpointHitMapper;
import ru.practicum.statsservice.model.EndpointHit;
import ru.practicum.statsservice.repository.EndpointHitRepository;

import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EndpointHitServiceImpl implements EndpointHitService {

    private final EndpointHitRepository endpointHitRepository;
    private final EndpointHitMapper endpointHitMapper;

    @Override
    @Transactional
    public void create(EndpointHitDto dto) {
        EndpointHit endpointHit = endpointHitMapper.toEntity(dto);
        EndpointHit savedendpointHit = endpointHitRepository.save(endpointHit);
        log.info("EndpointHit с id {} создан", savedendpointHit.getId());
    }

    @Override
    public List<ViewStatsDto> getStats(GetStatsRequest request) {
        LocalDateTime start = request.getStart();
        LocalDateTime end = request.getEnd();
        if (start == null) {
            start = LocalDateTime.now().minusYears(100);
        }
        if (end == null) {
            end = LocalDateTime.now().plusYears(100);
        }
        if (start.isAfter(end) || start.isEqual(end)) {
            throw new ValidationException("Дата начала отбора должна быть до даты конца отбора.");
        }
        if (CollectionUtils.isEmpty(request.getUris())) {
            return handleStatsWithoutUris(request);
        } else {
            return handleStatsWithUris(request);
        }
    }

    private List<ViewStatsDto> handleStatsWithoutUris(GetStatsRequest request) {
        if (request.getUnique()) {
            return endpointHitRepository.getViewStatsWithUniqueIps(request.getStart(), request.getEnd());
        } else {
            return endpointHitRepository.getViewStats(request.getStart(), request.getEnd());
        }
    }

    private List<ViewStatsDto> handleStatsWithUris(GetStatsRequest request) {
        if (request.getUnique()) {
            return endpointHitRepository.getViewStatsByUriInWithUniqueIps(
                    request.getStart(), request.getEnd(), request.getUris());
        } else {
            return endpointHitRepository.getViewStatsByUriIn(
                    request.getStart(), request.getEnd(), request.getUris());
        }
    }

    @Override
    public List<ViewStatsDto> getAllStatsByUris(List<String> uris) {
        return endpointHitRepository.getAllViewStatsByUriIn(uris);
    }
}
