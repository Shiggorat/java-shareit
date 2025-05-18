package ru.practicum.shareit.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Autowired
    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public List<UserDto> getAll() {
        log.debug("Request GET to /users");
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public UserDto getById(long id) {
        log.debug("Request GET to /users/{}", id);
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new NotFoundException("User with id = " + id + " is not found"));
    }

    @Override
    public UserDto create(UserDto userDto) {
        log.debug("Request POST to /users, with id = {}, name = {}, email = {}",
                userDto.getId(), userDto.getName(), userDto.getEmail());
        User user = userMapper.fromDto(userDto);
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    public UserDto update(long id, UserDto userDto) {
        log.debug("Request PATCH to /users, with id = {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("User with id = " + id + " not found"));
        userDto.setId(id);
        return userMapper.toDto(userRepository.update(userDto, user));
    }

    @Override
    public void deleteById(long id) {
        log.debug("Request DELETE to /users/{}", id);
        userRepository.deleteById(id);
    }

    @Override
    public void deleteAll() {
        log.debug("Request DELETE to /users)");
        userRepository.deleteAll();
    }
}
