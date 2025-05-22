package ru.practicum.shareit.user.service;

import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.practicum.shareit.exception.NotFoundException;
import ru.practicum.shareit.exception.ValidateException;
import ru.practicum.shareit.user.User;
import ru.practicum.shareit.user.dto.UserDto;
import ru.practicum.shareit.user.dto.UserMapper;
import ru.practicum.shareit.user.repository.UserRepository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor(onConstructor_ = @Autowired)
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;

    @Override
    @Transactional(readOnly = true)
    public List<UserDto> getAll() {
        log.debug("Request GET to /users");
        return userRepository.findAll()
                .stream()
                .map(userMapper::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UserDto getById(long id) {
        log.debug("Request GET to /users/{}", id);
        return userRepository.findById(id)
                .map(userMapper::toDto)
                .orElseThrow(() -> new NotFoundException("User with id = " + id + " is not found"));
    }

    @Override
    @Transactional
    public UserDto create(UserDto userDto) {
        log.debug("Request POST to /users, with id = {}, name = {}, email = {}",
                userDto.getId(), userDto.getName(), userDto.getEmail());
        if (userRepository.findByEmail(userDto.getEmail()).isPresent()) {
            throw new ValidateException("Email already exists");
        }
        User user = userMapper.fromDto(userDto);
        return userMapper.toDto(userRepository.save(user));
    }

    @Override
    @Transactional
    public UserDto update(long id, UserDto userDto) {
        log.debug("Request PATCH to /users, with id = {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new NotFoundException("User with id = " + id + " not found"));
        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            Optional<User> userWithSameEmail = userRepository.findByEmail(userDto.getEmail());
            if (userWithSameEmail.isPresent() && userWithSameEmail.get().getId() != id) {
                throw new ValidateException("Email already exists");
            }
            user.setEmail(userDto.getEmail());
        }
        userDto.setId(id);
        return userMapper.toDto(update(userDto, user));
    }

    @Override
    @Transactional
    public void deleteById(long id) {
        log.debug("Request DELETE to /users/{}", id);
        userRepository.deleteById(id);
    }

    @Override
    @Transactional
    public void deleteAll() {
        log.debug("Request DELETE to /users)");
        userRepository.deleteAll();
    }

    private User update(UserDto userDto, User user) {
        if (userDto.getName() != null && !userDto.getName().isBlank()) {
            user.setName(userDto.getName());
        }
        if (userDto.getEmail() != null && !userDto.getEmail().isBlank()) {
            user.setEmail(userDto.getEmail());
        }
        return user;
    }
}
