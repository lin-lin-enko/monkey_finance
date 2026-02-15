package com.lin.monkey.dto;

import com.lin.monkey.model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponseDto(
        UUID id,
        String username,
        String email,
        LocalDate birthDate,
        String phoneNumber,
        LocalDateTime createdAt,
        UUID defaultLedgerId
) {
    public static UserResponseDto fromUser(User user) {
        if (user == null) return null;

        return new UserResponseDto(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getBirthDate(),
                user.getPhoneNumber(),
                user.getCreatedAt(),
                user.getDefaultLedgerId());
    }
}
