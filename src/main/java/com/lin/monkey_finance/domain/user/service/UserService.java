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
    private final UserMapper userMapper;

    public UserService(
            UserRepository userRepository,
            UserMapper userMapper
    ){
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Transactional
    public UserResponseDto edit(UUID userId, UserUpdateDto userUpdateDto){
        User user = userRepository.findById(userId)
                .orElseThrow(
                        () -> new ResourceNotFoundException(
                                "No user with such id"
                        )
                );

        userMapper.updateUserFromDto(userUpdateDto, user);

        return userMapper.toResponseDto(user);
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
        return userMapper.toResponseDto(user);
    }
}
