package ru.practicum.mainservice.service;

import ru.practicum.mainservice.dto.event.EventFullDto;
import ru.practicum.mainservice.dto.event.EventShortDto;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public interface StatsService {
    void setStatsViewsToEventShortDtos(List<EventShortDto> dtos);

    void setStatsViewsToEventFullDtos(List<EventFullDto> dtos);

    void saveStats(HttpServletRequest request);
}
