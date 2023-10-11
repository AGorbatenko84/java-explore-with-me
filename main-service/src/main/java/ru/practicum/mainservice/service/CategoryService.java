package ru.practicum.mainservice.service;

import ru.practicum.mainservice.dto.category.CategoryDto;
import ru.practicum.mainservice.dto.category.NewCategoryDto;
import ru.practicum.mainservice.model.Category;

import java.util.List;

public interface CategoryService {
    CategoryDto create(NewCategoryDto dto);

    CategoryDto patch(CategoryDto dto, Long id);

    void delete(Long id);

    List<CategoryDto> getAll(Integer from, Integer size);

    CategoryDto getById(Long id);

    Category getEntityById(Long id);
}
