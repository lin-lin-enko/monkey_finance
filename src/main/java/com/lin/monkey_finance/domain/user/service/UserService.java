package com.lin.monkey_finance.domain.user.service;

import com.lin.monkey_finance.common.exception.ResourceNotFoundException;
import com.lin.monkey_finance.domain.user.dto.UserResponseDto;
import com.lin.monkey_finance.domain.user.dto.UserUpdateDto;
import com.lin.monkey_finance.domain.user.mapper.UserMapper;
import com.lin.monkey_finance.domain.user.model.User;
import com.lin.monkey_finance.domain.user.model.UserStatus;
import com.lin.monkey_finance.domain.user.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final UserMapper mapper;

    public UserService(
            UserRepository userRepository,
            UserMapper mapper
    ){
        this.userRepository = userRepository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public UserResponseDto getById(UUID userId){
        User user = userRepository.findById(userId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "No user with such id"
                        )
                );
        return mapper.toResponseDto(user);
    }

    @Transactional(readOnly = true)
    public UserResponseDto getByEmail(String email){
        User user = userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "No user with such email " + email
                        )
                );
        return mapper.toResponseDto(user);
    }

    @Transactional(readOnly = true)
    public boolean existsByEmail(String email){
        return userRepository.existsByEmail(email);
    }

    @Transactional
    public UserResponseDto edit(UUID userId, UserUpdateDto userUpdateDto){
        User user = userRepository.findById(userId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "No user with such id"
                        )
                );

        mapper.updateUserFromDto(userUpdateDto, user);

        return mapper.toResponseDto(user);
    }

    @Transactional
    public UserResponseDto delete(UUID userId){
        User user = userRepository.findById(userId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "No user with such id"
                        )
                );
        user.setStatus(UserStatus.DELETED);
        return mapper.toResponseDto(user);
    }
}
