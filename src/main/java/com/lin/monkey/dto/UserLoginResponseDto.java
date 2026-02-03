package com.lin.monkey.dto;

import java.util.UUID;

public record UserLoginResponseDto(
        String token,
        UUID userId
) {
}
