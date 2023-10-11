package ru.practicum.mainservice.service.impl;

import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import ru.practicum.mainservice.dto.user.UserDto;
import ru.practicum.mainservice.exception.NotFoundException;
import ru.practicum.mainservice.mapper.UserMapper;
import ru.practicum.mainservice.model.QUser;
import ru.practicum.mainservice.model.User;
import ru.practicum.mainservice.repository.UserRepository;
import ru.practicum.mainservice.service.UserService;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional
    public UserDto create(UserDto dto) {
        User savedUser = userRepository.save(userMapper.toEntity(dto));
        log.info("Пользователь с id {} создан", savedUser.getId());
        return userMapper.toDto(savedUser);
    }

    @Override
    public List<UserDto> getAll(List<Long> ids, Integer from, Integer size) {
        BooleanBuilder builder = new BooleanBuilder();
        if (!CollectionUtils.isEmpty(ids)) {
            builder.and(QUser.user.id.in(ids));
        }
        PageRequest pageRequest = PageRequest.of(from / size, size);
        List<User> users = userRepository.findAll(builder, pageRequest).getContent();
        return userMapper.toDtoList(users);
    }

    @Override
    @Transactional
    public void deleteById(Long id) {
        if (!userRepository.existsById(id)) {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        }
        userRepository.deleteById(id);
        log.info("Пользователь с id {} удален", id);
    }

    @Override
    public User getEntityById(Long id) {
        return userRepository.findById(id).orElseThrow(() -> {
            throw new NotFoundException("Пользователь с id " + id + " не найден");
        });
    }
}
