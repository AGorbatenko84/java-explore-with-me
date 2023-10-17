package ru.practicum.mainservice.service;

import ru.practicum.mainservice.dto.user.UserDto;
import ru.practicum.mainservice.model.User;

import java.util.List;

public interface UserService {
    UserDto create(UserDto dto);

    List<UserDto> getAll(List<Long> ids, Integer from, Integer size);

    void deleteById(Long id);

    User getEntityById(Long id);
}
