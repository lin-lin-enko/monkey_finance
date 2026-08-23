package com.lin.monkey_finance.domain.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record UserUpdateDto(
    @Size(min = 5, max = 20, message = "Username must be 5 to 20 characters long")
    String username,

    @Email(message = "Invalid email format")
    @Size(min = 5, max = 100, message = "Email must be 5 to 100 characters long")
    String email,

    @Size(min = 2, max = 50, message = "Name must be 2 to 50 characters long")
    String name,

    @Past(message = "Date of birth must be in the past")
    LocalDate dateOfBirth
){}
