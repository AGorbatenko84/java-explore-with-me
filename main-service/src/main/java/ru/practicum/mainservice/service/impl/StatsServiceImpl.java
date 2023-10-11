package ru.practicum.mainservice.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import ru.practicum.mainservice.dto.event.EventFullDto;
import ru.practicum.mainservice.dto.event.EventShortDto;
import ru.practicum.mainservice.service.StatsService;
import ru.practicum.statsservice.client.StatsServiceClient;
import ru.practicum.statsservice.dto.EndpointHitDto;
import ru.practicum.statsservice.dto.ViewStatsDto;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {

    private static final String APP_NAME = "ewm-main-service";

    private final StatsServiceClient statsServiceClient;
    private final ObjectMapper objectMapper = new ObjectMapper();

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

    @Override
    public void setStatsViewsToEventShortDtos(List<EventShortDto> dtos) {
        List<Long> ids = dtos.stream().map(EventShortDto::getId).collect(Collectors.toList());
        Map<Long, Integer> viewsMap = getStatsViewsMap(ids);
        dtos.forEach(d -> d.setViews(viewsMap.getOrDefault(d.getId(), 0)));
    }

    @Override
    public void setStatsViewsToEventFullDtos(List<EventFullDto> dtos) {
        List<Long> ids = dtos.stream().map(EventFullDto::getId).collect(Collectors.toList());
        Map<Long, Integer> viewsMap = getStatsViewsMap(ids);
        dtos.forEach(d -> d.setViews(viewsMap.getOrDefault(d.getId(), 0)));
    }

    @Override
    public void saveStats(HttpServletRequest request) {
        EndpointHitDto hit = new EndpointHitDto();
        hit.setApp(APP_NAME);
        hit.setUri(request.getRequestURI());
        hit.setIp(request.getRemoteAddr());
        hit.setTimestamp(LocalDateTime.now());
        statsServiceClient.create(hit);
    }
}
