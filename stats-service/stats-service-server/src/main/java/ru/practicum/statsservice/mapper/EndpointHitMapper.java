package ru.practicum.statsservice.mapper;

import org.mapstruct.Mapper;
import ru.practicum.statsservice.dto.EndpointHitDto;
import ru.practicum.statsservice.model.EndpointHit;

@Mapper
public interface EndpointHitMapper {

    EndpointHit toEntity(EndpointHitDto dto);

    EndpointHitDto toDto(EndpointHit entity);
}
