package com.lin.monkey.controller;

import com.lin.monkey.dto.UserResponseDto;
import com.lin.monkey.exception.UserNotFoundException;
import com.lin.monkey.model.User;
import com.lin.monkey.security.CustomUserDetails;
import com.lin.monkey.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UUID userId = userDetails.getId();
        User user = userService.findById(userId)
                .orElseThrow(
                        () -> new UserNotFoundException("User not found with such id: " + userId));

        UserResponseDto responseDto = UserResponseDto.fromUser(
                user
        );

        return ResponseEntity.ok(responseDto);
    }
}
