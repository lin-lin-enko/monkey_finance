package com.lin.monkey_finance.domain.user.controller;

import com.lin.monkey_finance.domain.user.dto.UserRegisterDto;
import com.lin.monkey_finance.domain.user.dto.UserResponseDto;
import com.lin.monkey_finance.domain.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService){
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponseDto> register(
            @RequestBody @Valid UserRegisterDto userRegisterDto
            ){
        UserResponseDto userResponseDto = userService.register(userRegisterDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(userResponseDto);
    }
}
