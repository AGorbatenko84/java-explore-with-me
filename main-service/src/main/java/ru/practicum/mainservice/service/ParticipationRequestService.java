package ru.practicum.mainservice.service;

import ru.practicum.mainservice.dto.participation_request.ParticipationRequestDto;

import java.util.List;

public interface ParticipationRequestService {

    ParticipationRequestDto create(Long userId, Long eventId);

    List<ParticipationRequestDto> getAllByUserId(Long userId);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);
}
