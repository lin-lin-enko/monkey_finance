package com.lin.monkey_finance.domain.auth.controller;

import com.lin.monkey_finance.domain.auth.service.AuthService;
import com.lin.monkey_finance.domain.user.dto.AuthResponseDto;
import com.lin.monkey_finance.domain.user.dto.UserLoginDto;
import com.lin.monkey_finance.domain.user.dto.UserRegisterDto;
import com.lin.monkey_finance.domain.user.dto.UserResponseDto;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService){
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(
            @RequestBody @Valid UserRegisterDto userRegisterDto
    ){
        UserResponseDto userResponseDto = authService.register(userRegisterDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(userResponseDto);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponseDto> login(
            @RequestBody @Valid UserLoginDto userLoginDto
    ){
        AuthResponseDto authResponseDto = authService.login(userLoginDto);
        return ResponseEntity.status(HttpStatus.OK).body(authResponseDto);
    }

    @GetMapping("/confirm")
    public ResponseEntity<String> activateUser(
            @RequestParam String token
    ){
        authService.confirmEmailAddress(token);
        return ResponseEntity.ok("Successfully activated");
    }
}

