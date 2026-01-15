package com.lin.monkey.controller;

import com.lin.monkey.dto.UserLoginRequestDto;
import com.lin.monkey.dto.UserLoginResponseDto;
import com.lin.monkey.dto.UserRegistrationRequestDto;
import com.lin.monkey.dto.UserRegistrationResponseDto;
import com.lin.monkey.model.User;
import com.lin.monkey.security.JwtUtil;
import com.lin.monkey.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

// Means class returns JSON, not HTML
@RestController
// Means all methods begin from /api/auth
@RequestMapping("/api/auth")
public class AuthController {

    // I call user service to do app logic
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // Spring calls constructor
    public AuthController(UserService userService, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    /* The POST endpoint will be /api/auth/register
     * @PostMapping means this method will manage POST-requests
     * to the /api/auth/register
     */
    @PostMapping("/register")
    /* ResponseEntity<UserRegistrationRequestDto> to make a safe response obj
     * ResponseEntity is used to form an HTTP response
     */
    public ResponseEntity<UserRegistrationResponseDto> register(
            /* Takes JSON from request body, validates it through
             * UserRegistrationRequestDto and turns into dto obj
             */
            @Valid @RequestBody UserRegistrationRequestDto registrationDto
    ) {
        /* Calling user service and passing registration obj (dto) to it
         * Saving calling result into a safe response obj (dto)
         */
        UserRegistrationResponseDto responseDto = userService.register(registrationDto);

        // Returns 201 response + Location header + body with response dto
        return ResponseEntity.created(URI.create("/api/users/" + responseDto.getUsername())).body(responseDto);
    }

    @PostMapping("/login")
    public ResponseEntity<UserLoginResponseDto> login(
            @Valid @RequestBody UserLoginRequestDto loginDto
    ) {
        System.out.println("hi");
        User user = userService.findByEmail(loginDto.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("No user with such email"));

        if (!passwordEncoder.matches(loginDto.getPassword(), user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid password");
        }

        String token = jwtUtil.generateToken(user.getUsername());

        UserLoginResponseDto responseDto = new UserLoginResponseDto(
                token, user.getUsername(), user.getEmail()
        );

        return ResponseEntity.ok(responseDto);
    }
}
