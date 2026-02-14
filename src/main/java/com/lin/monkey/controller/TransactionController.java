package com.lin.monkey.controller;

import com.lin.monkey.dto.TransactionCreationDto;
import com.lin.monkey.dto.TransactionResponseDto;
import com.lin.monkey.dto.TransactionUpdateDto;
import com.lin.monkey.model.TransactionType;
import com.lin.monkey.security.CustomUserDetails;
import com.lin.monkey.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
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

    @PatchMapping("/{transactionId}")
    public ResponseEntity<TransactionResponseDto> update(
            @Valid @RequestBody TransactionUpdateDto updateDto,
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID transactionId
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        TransactionResponseDto responseDto = transactionService.update(updateDto, transactionId);
        return ResponseEntity.ok(responseDto);
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<TransactionResponseDto> getById(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID transactionId
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok().body(transactionService.getById(transactionId));
    }

    @GetMapping
    public ResponseEntity<List<TransactionResponseDto>> getFiltered(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable UUID ledgerId,
            @RequestParam(required = false) UUID category,
            @RequestParam(required = false) UUID subcategory,
            @RequestParam(required = false) TransactionType type,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime toDate

    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        return ResponseEntity.ok().body(transactionService.getFiltered(ledgerId, category, subcategory, type, fromDate, toDate));
    }

}
