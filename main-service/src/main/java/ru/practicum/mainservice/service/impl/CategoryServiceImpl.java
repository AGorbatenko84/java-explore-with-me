package ru.practicum.mainservice.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.mainservice.dto.category.CategoryDto;
import ru.practicum.mainservice.dto.category.NewCategoryDto;
import ru.practicum.mainservice.exception.NotFoundException;
import ru.practicum.mainservice.mapper.CategoryMapper;
import ru.practicum.mainservice.model.Category;
import ru.practicum.mainservice.repository.CategoryRepository;
import ru.practicum.mainservice.service.CategoryService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CategoryServiceImpl implements CategoryService {

    private static final String CATEGORY_NOT_FOUND = "Category with id %d not found";
    private final CategoryRepository categoryRepository;
    private final CategoryMapper categoryMapper;


    @Override
    @Transactional
    public CategoryDto create(NewCategoryDto dto) {
        Category savedCategory = categoryRepository.save(categoryMapper.toEntity(dto));
        log.info("Категория с id {} создана", savedCategory.getId());
        return categoryMapper.toDto(savedCategory);
    }

    @Override
    @Transactional
    public CategoryDto patch(CategoryDto dto, Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> {
            throw new NotFoundException(String.format(CATEGORY_NOT_FOUND, id));
        });
        categoryMapper.patchCategory(dto, category);
        log.info("Категория с id {} изменена", category.getId());
        return categoryMapper.toDto(category);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!categoryRepository.existsById(id)) {
            throw new NotFoundException(String.format(CATEGORY_NOT_FOUND, id));
        }
        categoryRepository.deleteById(id);
        log.info("Категория с id {} удалена", id);
    }

    @Override
    public List<CategoryDto> getAll(Integer from, Integer size) {
        PageRequest pageRequest = PageRequest.of(from / size, size);
        List<Category> categories = categoryRepository.findAll(pageRequest).getContent();
        return categoryMapper.toDtoList(categories);
    }

    @Override
    public CategoryDto getById(Long id) {
        Category category = categoryRepository.findById(id).orElseThrow(() -> {
            throw new NotFoundException(String.format(CATEGORY_NOT_FOUND, id));
        });
        return categoryMapper.toDto(category);
    }

    @Override
    public Category getEntityById(Long id) {
        return categoryRepository.findById(id).orElseThrow(() -> {
            throw new NotFoundException(String.format(CATEGORY_NOT_FOUND, id));
        });
    }
}
