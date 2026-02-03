package com.lin.monkey.controller;

import com.lin.monkey.dto.LedgerCreationDto;
import com.lin.monkey.dto.LedgerResponseDto;
import com.lin.monkey.model.Ledger;
import com.lin.monkey.security.CustomUserDetails;
import com.lin.monkey.service.LedgerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@RestController
@RequestMapping("/api/ledgers")
public class LedgerController {
    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @PostMapping
    public ResponseEntity<LedgerResponseDto> create(
            @Valid @RequestBody LedgerCreationDto ledgerCreationDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        LedgerResponseDto responseDto = ledgerService.create(ledgerCreationDto);

        URI location = URI.create("/api/ledgers/" + responseDto.id());
        return ResponseEntity.created(location).body(responseDto);
    }

    @GetMapping
    public ResponseEntity<List<LedgerResponseDto>> getAllLedgers(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UUID userId = userDetails.getId();

        List<Ledger> ledgers = ledgerService.findAllByOwnerId(userId);
        List<LedgerResponseDto> dtoLedgers = new ArrayList<>();
        ledgers.forEach(ledger -> {
            dtoLedgers.add(LedgerResponseDto.fromLedger(ledger));
        });

        return ResponseEntity.ok().body(dtoLedgers);

    }

    @GetMapping("/default")
    public ResponseEntity<LedgerResponseDto> getDefaultLedger(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UUID userId = userDetails.getId();

        return ledgerService.findDefaultByOwnerId(userId)
                .map(LedgerResponseDto::fromLedger)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
