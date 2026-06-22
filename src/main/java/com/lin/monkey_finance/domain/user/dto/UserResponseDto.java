package com.lin.monkey_finance.domain.user.dto;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record UserResponseDto(
    UUID id,
    String username,
    String email,
    String name,
    LocalDate dateOfBirth,
    OffsetDateTime createdAt
){}
