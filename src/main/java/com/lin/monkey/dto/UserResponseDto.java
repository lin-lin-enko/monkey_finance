package com.lin.monkey.dto;

import com.lin.monkey.model.User;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class UserResponseDto {
    private final UUID id;
    private final String username;
    private final String email;
    private final LocalDate birthDate;
    private final String phoneNumber;
    private final LocalDateTime createdAt;

    private UserResponseDto(UUID id, String username, String email, LocalDate birthDate, String phoneNumber, LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.email = email;
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
        this.createdAt = createdAt;
    }

    public static UserResponseDto fromUser(User user) {
        if (user == null) return null;

        return new UserResponseDto(user.getId(), user.getUsername(), user.getEmail(), user.getBirthDate(), user.getPhoneNumber(), user.getCreatedAt());
    }

    public String getUsername() {
        return username;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getBirthDate() {
        return birthDate;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
