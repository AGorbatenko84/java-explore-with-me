package ru.practicum.mainservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.practicum.mainservice.dto.compilation.CompilationDto;
import ru.practicum.mainservice.dto.compilation.NewCompilationDto;
import ru.practicum.mainservice.dto.compilation.UpdateCompilationRequest;
import ru.practicum.mainservice.exception.NotFoundException;
import ru.practicum.mainservice.mapper.CompilationMapper;
import ru.practicum.mainservice.model.Compilation;
import ru.practicum.mainservice.repository.CompilationRepository;
import ru.practicum.mainservice.repository.EventRepository;
import ru.practicum.mainservice.service.CompilationService;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompilationServiceImpl implements CompilationService {

    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;
    private final CompilationMapper compilationMapper;

    @Override
    @Transactional
    public CompilationDto create(NewCompilationDto dto) {

        Compilation compilation = compilationMapper.toEntity(dto);
        if (!CollectionUtils.isEmpty(dto.getEvents())) {
            compilation.setEvents(eventRepository.findByIdIn(dto.getEvents()));
        }
        if (compilation.getPinned() == null) {
            compilation.setPinned(false);
        }
        Compilation savedCompilation = compilationRepository.save(compilation);
        log.info("Подборка событий с id {} создана", savedCompilation.getId());
        return compilationMapper.toDto(savedCompilation);
    }

    @Override
    public CompilationDto getById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(() -> {
            throw new NotFoundException("Подборка событий с id " + compId + " не найдена");
        });
        return compilationMapper.toDto(compilation);
    }

    @Override
    public List<CompilationDto> getAll(Boolean pinned, Integer from, Integer size) {
        PageRequest page = PageRequest.of(from / size, size);
        return Objects.isNull(pinned) ? compilationMapper.toDtoList(compilationRepository.findAll(page).getContent()) :
                compilationMapper.toDtoList(compilationRepository.findAllByPinned(pinned, page).getContent());
    }

    @Override
    @Transactional
    public void deleteById(Long compId) {
        compilationRepository.findById(compId).orElseThrow(() -> {
            throw new NotFoundException("Подборка событий с id " + compId + " не найдена");
        });
        compilationRepository.deleteById(compId);
    }

    @Override
    @Transactional
    public CompilationDto patch(UpdateCompilationRequest request, Long compId) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(() -> {
            throw new NotFoundException("Подборка событий с id " + compId + " не найдена");
        });

        compilationMapper.patch(request, compilation);
        if (Objects.nonNull(request.getEvents())) {
            compilation.setEvents(eventRepository.findByIdIn(request.getEvents()));
        }
        CompilationDto compilationDto = compilationMapper.toDto(compilation);
        return compilationDto;
    }
}
