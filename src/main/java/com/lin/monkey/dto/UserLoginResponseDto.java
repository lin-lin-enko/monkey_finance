package com.lin.monkey.dto;

import java.util.UUID;

public class UserLoginResponseDto {
    private String token;
    private UUID userId;

    public UserLoginResponseDto(String token, UUID userId) {
        this.token = token;
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }
}
