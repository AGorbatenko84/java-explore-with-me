package ru.practicum.mainservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.mainservice.model.ParticipationRequest;

import java.util.List;
import java.util.Optional;

public interface ParticipationRequestRepository extends JpaRepository<ParticipationRequest, Long> {

    List<ParticipationRequest> findAllByEventInitiatorIdAndEventId(Long userId, Long eventId);

    List<ParticipationRequest> findParticipationRequestByEventIdAndIdIn(Long eventId, List<Long> requestIds);

    List<ParticipationRequest> findAllByRequesterId(Long userId);

    Optional<ParticipationRequest> findByIdAndRequesterId(Long requestId, Long userId);

    Optional<ParticipationRequest> findByEventIdAndRequesterId(Long eventId, Long requesterId);
}
