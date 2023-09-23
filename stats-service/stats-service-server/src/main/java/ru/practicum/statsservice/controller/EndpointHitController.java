package ru.practicum.statsservice.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.statsservice.dto.EndpointHitDto;
import ru.practicum.statsservice.dto.GetStatsRequest;
import ru.practicum.statsservice.dto.ViewStatsDto;
import ru.practicum.statsservice.service.EndpointHitService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@Slf4j
@Validated
@RestController
@RequiredArgsConstructor
public class EndpointHitController {

    private final EndpointHitService endpointHitService;

    @PostMapping("/hit")
    @ResponseStatus(HttpStatus.CREATED)
    public void create(@RequestBody @Valid EndpointHitDto dto) {
        endpointHitService.create(dto);
    }

    @GetMapping("/stats")
    @ResponseStatus(HttpStatus.OK)
    public List<ViewStatsDto> getStats(
            @RequestParam(name = "start") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam(name = "end") @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @RequestParam(name = "uris", required = false) List<String> uris,
            @RequestParam(name = "unique", defaultValue = "false") Boolean unique) {
        return endpointHitService.getStats(GetStatsRequest.of(start, end, uris, unique));
    }

    @GetMapping("/stats/all")
    @ResponseStatus(HttpStatus.OK)
    public List<ViewStatsDto> getAllStatsByUris(@RequestParam(name = "uris") List<String> uris) {
        return endpointHitService.getAllStatsByUris(uris);
    }
}
