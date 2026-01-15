package com.lin.monkey.dto;

import com.lin.monkey.model.User;

import java.time.LocalDate;

public class UserRegistrationResponseDto {
    private final String username;
    private final String email;
    private final LocalDate birthDate;
    private final String phoneNumber;

    private UserRegistrationResponseDto(String username, String email, LocalDate birthDate, String phoneNumber) {
        this.username = username;
        this.email = email;
        this.birthDate = birthDate;
        this.phoneNumber = phoneNumber;
    }

    public static UserRegistrationResponseDto fromUser(User user) {
        if (user == null) return null;

        return new UserRegistrationResponseDto(user.getUsername(), user.getEmail(), user.getBirthDate(), user.getPhoneNumber());
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
}
