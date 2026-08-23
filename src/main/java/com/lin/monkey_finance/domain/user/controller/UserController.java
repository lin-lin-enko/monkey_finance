package com.lin.monkey_finance.domain.user.controller;


import com.lin.monkey_finance.domain.user.dto.UserResponseDto;
import com.lin.monkey_finance.domain.user.dto.UserUpdateDto;
import com.lin.monkey_finance.domain.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(
            UserService userService
    ){
        this.userService = userService;
    }

    @PatchMapping("/edit")
    public ResponseEntity<UserResponseDto> editUserInfo(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UserUpdateDto userUpdateDto
            ){
        UUID userId = UUID.fromString(jwt.getSubject());
        UserResponseDto userResponseDto = userService.editUserInfo(userId, userUpdateDto);
        return ResponseEntity.ok(userResponseDto);
    }
}
