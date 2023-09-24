package ru.practicum.statsservice.service;

import ru.practicum.statsservice.dto.EndpointHitDto;
import ru.practicum.statsservice.dto.GetStatsRequest;
import ru.practicum.statsservice.dto.ViewStatsDto;

import java.util.List;

public interface EndpointHitService {

    void create(EndpointHitDto dto);

    List<ViewStatsDto> getStats(GetStatsRequest request);

    List<ViewStatsDto> getAllStatsByUris(List<String> uris);
}
