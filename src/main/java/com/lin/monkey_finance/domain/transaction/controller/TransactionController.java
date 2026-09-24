package com.lin.monkey_finance.domain.transaction.controller;

import com.lin.monkey_finance.domain.transaction.dto.TransactionCreateDto;
import com.lin.monkey_finance.domain.transaction.dto.TransactionResponseDto;
import com.lin.monkey_finance.domain.transaction.dto.TransactionUpdateDto;
import com.lin.monkey_finance.domain.transaction.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/api/v1")
@RestController
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(
            TransactionService transactionService
    ){
        this.transactionService = transactionService;
    }

    @PostMapping("/transactions")
    public ResponseEntity<TransactionResponseDto> create(
            @Valid @RequestBody TransactionCreateDto createDto,
            @AuthenticationPrincipal Jwt jwt
            ){
        UUID userId = UUID.fromString(jwt.getSubject());
        TransactionResponseDto responseDto = transactionService.create(userId, createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PatchMapping("/ledgers/{ledgerId}/transactions/{transactionId}")
    public ResponseEntity<TransactionResponseDto> edit(
        @Valid @RequestBody TransactionUpdateDto updateDto,
        @AuthenticationPrincipal Jwt jwt,
        @PathVariable UUID ledgerId,
        @PathVariable UUID transactionId
    ){
        UUID userId = UUID.fromString(jwt.getSubject());
        TransactionResponseDto responseDto = transactionService.edit(userId, ledgerId, transactionId, updateDto);
        return ResponseEntity.ok(responseDto);
    }

}
