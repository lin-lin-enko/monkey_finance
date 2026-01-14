package com.lin.monkey.controller;

import com.lin.monkey.dto.UserRegistrationDto;
import com.lin.monkey.dto.UserResponseDto;
import com.lin.monkey.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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

    // Spring calls constructor
    public AuthController(UserService userService) {
        this.userService = userService;
    }

    /* The POST endpoint will be /api/auth/register
     * @PostMapping means this method will manage POST-requests
     * to the /api/auth/register
     */
    @PostMapping("/register")
    /* ResponseEntity<UserRegistrationDto> to make a safe response obj
     * ResponseEntity is used to form an HTTP response
     */
    public ResponseEntity<UserResponseDto> register(
            /* Takes JSON from request body, validates it through
             * UserRegistrationDto and turns into dto obj
             */
            @Valid @RequestBody UserRegistrationDto registrationDto
    ) {
        /* Calling user service and passing registration obj (dto) to it
         * Saving calling result into a safe response obj (dto)
         */
        UserResponseDto responseDto = userService.register(registrationDto);

        // Returns 201 response + Location header + body with response dto
        return ResponseEntity.created(URI.create("/api/users/" + responseDto.getUsername())).body(responseDto);
    }
}
