package com.lin.monkey_finance.domain.user.dto;

public record AuthResponseDto(
        String token,
        UserResponseDto userResponseDto
) {
}
