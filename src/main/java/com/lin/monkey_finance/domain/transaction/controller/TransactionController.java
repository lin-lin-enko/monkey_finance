package com.lin.monkey_finance.domain.transaction.controller;

import com.lin.monkey_finance.domain.transaction.dto.TransactionCreateDto;
import com.lin.monkey_finance.domain.transaction.dto.TransactionResponseDto;
import com.lin.monkey_finance.domain.transaction.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RequestMapping("/api/v1/transactions")
@RestController
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(
            TransactionService transactionService
    ){
        this.transactionService = transactionService;
    }

    @PostMapping()
    public ResponseEntity<TransactionResponseDto> create(
            @Valid @RequestBody TransactionCreateDto createDto,
            @AuthenticationPrincipal Jwt jwt
            ){
        UUID userId = UUID.fromString(jwt.getSubject());
        TransactionResponseDto responseDto = transactionService.create(userId, createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

}
