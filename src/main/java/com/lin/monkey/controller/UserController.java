package com.lin.monkey.controller;

import com.lin.monkey.dto.ChangeDefaultLedgerDto;
import com.lin.monkey.dto.UserResponseDto;
import com.lin.monkey.exception.UserNotFoundException;
import com.lin.monkey.model.User;
import com.lin.monkey.security.CustomUserDetails;
import com.lin.monkey.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

        UserResponseDto dto = UserResponseDto.fromUser(user);

        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/me/default-ledger")
    public ResponseEntity<UserResponseDto> changeDefaultLedger(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid ChangeDefaultLedgerDto changeDefaultLedgerDto
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserResponseDto userResponseDto = userService.changeDefaultLedger(userDetails.getId(), changeDefaultLedgerDto.ledgerId());

        return ResponseEntity.ok(userResponseDto);
    }
}
