package com.lin.monkey_finance.domain.account.controller;


import com.lin.monkey_finance.domain.account.dto.AccountCreateDto;
import com.lin.monkey_finance.domain.account.dto.AccountEditDto;
import com.lin.monkey_finance.domain.account.dto.AccountResponseDto;
import com.lin.monkey_finance.domain.account.service.AccountService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @GetMapping()
    public ResponseEntity<List<AccountResponseDto>> getAll(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID ledgerId
    ){
        UUID userId = UUID.fromString(jwt.getSubject());
        List<AccountResponseDto> accounts = accountService.getAll(userId, ledgerId);
        return ResponseEntity.ok(accounts);
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

    @PatchMapping("/{accountId}")
    public ResponseEntity<AccountResponseDto> edit(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID ledgerId,
            @Valid @RequestBody AccountEditDto editDto,
            @PathVariable UUID accountId
    ){
        UUID userId = UUID.fromString(jwt.getSubject());
        AccountResponseDto responseDto = accountService.edit(userId, ledgerId, accountId, editDto);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{accountId}")
    public ResponseEntity<AccountResponseDto> delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID ledgerId,
            @PathVariable UUID accountId
    ){
        UUID userId = UUID.fromString(jwt.getSubject());
        accountService.delete(userId, ledgerId, accountId);
        return ResponseEntity.status(204).build();
    }

}
