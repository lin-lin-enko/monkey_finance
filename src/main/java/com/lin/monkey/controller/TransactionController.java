package com.lin.monkey.controller;

import com.lin.monkey.dto.TransactionCreationDto;
import com.lin.monkey.dto.TransactionResponseDto;
import com.lin.monkey.security.CustomUserDetails;
import com.lin.monkey.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/ledgers/{ledgerId}/transactions")
public class TransactionController {

    private final TransactionService transactionService;

    public TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping
    public ResponseEntity<TransactionResponseDto> create(
            @Valid @RequestBody TransactionCreationDto creationDto,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID ledgerId
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        TransactionResponseDto responseDto = transactionService.create(creationDto, ledgerId);

        return ResponseEntity.ok(responseDto);
    }
}
