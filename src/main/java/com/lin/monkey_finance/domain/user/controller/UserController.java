package com.lin.monkey_finance.domain.user.controller;


import com.lin.monkey_finance.domain.user.dto.UserResponseDto;
import com.lin.monkey_finance.domain.user.dto.UserUpdateDto;
import com.lin.monkey_finance.domain.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

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

    @PatchMapping()
    public ResponseEntity<UserResponseDto> edit(
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody UserUpdateDto userUpdateDto
            ){
        UUID userId = UUID.fromString(jwt.getSubject());
        UserResponseDto userResponseDto = userService.edit(userId, userUpdateDto);
        return ResponseEntity.ok(userResponseDto);
    }

    @DeleteMapping()
    public ResponseEntity<UserResponseDto> delete(
            @AuthenticationPrincipal Jwt jwt
    ){
        UUID userId = UUID.fromString(jwt.getSubject());
        UserResponseDto userResponseDto = userService.delete(userId);
        return ResponseEntity.ok(userResponseDto);
    }
}
