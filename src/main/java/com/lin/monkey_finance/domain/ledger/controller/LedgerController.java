package com.lin.monkey_finance.domain.ledger.controller;

import com.lin.monkey_finance.domain.ledger.dto.LedgerDetailedResponseDto;
import com.lin.monkey_finance.domain.ledger.dto.LedgerRequestDto;
import com.lin.monkey_finance.domain.ledger.dto.LedgerResponseDto;
import com.lin.monkey_finance.domain.ledger.dto.LedgerUpdateDto;
import com.lin.monkey_finance.domain.ledger.service.LedgerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ledgers")
public class LedgerController {
    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService){
        this.ledgerService = ledgerService;
    }

    @GetMapping()
    public ResponseEntity<List<LedgerResponseDto>> getCurrentUserLedgers(@AuthenticationPrincipal Jwt jwt){
        UUID userId = UUID.fromString(jwt.getSubject());
        System.out.println("TOKEN    " + jwt.getTokenValue());
        System.out.println("SUBJECT    " + jwt.getSubject());

        List<LedgerResponseDto> ledgerResponseDtoList = ledgerService.getCurrentUserLedgers(userId);
        return ResponseEntity.ok(ledgerResponseDtoList);
    }

    @GetMapping("/{ledgerId}")
    public ResponseEntity<LedgerDetailedResponseDto> getLedgerById(@PathVariable UUID ledgerId) {
        LedgerDetailedResponseDto dto = ledgerService.getLedgerById(ledgerId);
        return ResponseEntity.ok(dto);
    }

    @PostMapping()
    public ResponseEntity<LedgerDetailedResponseDto> create(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody LedgerRequestDto ledgerRequestDto
    ){
        UUID userId = UUID.fromString(jwt.getSubject());
        LedgerDetailedResponseDto ledgerDetailedResponseDto = ledgerService.create(ledgerRequestDto, userId);
        return ResponseEntity.status(201).body(ledgerDetailedResponseDto);
    }

    @PatchMapping("/{ledgerId}")
    public ResponseEntity<LedgerDetailedResponseDto> edit(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID ledgerId,
            @RequestBody @Valid LedgerUpdateDto ledgerUpdateDto
            ){
        UUID userId = UUID.fromString(jwt.getSubject());
        LedgerDetailedResponseDto ledgerDetailedResponseDto = ledgerService.edit(ledgerUpdateDto, userId, ledgerId);
        return ResponseEntity.ok(ledgerDetailedResponseDto);
    }
}
