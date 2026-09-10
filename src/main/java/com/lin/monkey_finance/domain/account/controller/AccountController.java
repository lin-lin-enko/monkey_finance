package com.lin.monkey_finance.domain.account.controller;


import com.lin.monkey_finance.domain.account.dto.AccountCreateDto;
import com.lin.monkey_finance.domain.account.dto.AccountResponseDto;
import com.lin.monkey_finance.domain.account.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/api/v1/ledgers/{ledgerId}/accounts")
@RestController
public class AccountController{
    private final AccountService accountService;

    public AccountController(
        AccountService accountService
    ){
        this.accountService = accountService;
    }

    @PostMapping()
    public ResponseEntity<AccountResponseDto> create(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID ledgerId,
            @Valid @RequestBody AccountCreateDto createDto
            ){
        UUID userId = UUID.fromString(jwt.getSubject());
        AccountResponseDto responseDto = accountService.create(userId, ledgerId, createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

}
