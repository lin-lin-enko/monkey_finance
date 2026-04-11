package com.lin.monkey.controller;

import com.lin.monkey.dto.LedgerAccessDto;
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

import java.net.URI;
import java.util.ArrayList;
import java.util.List;
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

    @GetMapping("/accessible")
    public ResponseEntity<List<LedgerResponseDto>> getAllAccessedByUser(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        List<Ledger> ledgers = ledgerService.findAllAccessedByUser(userDetails.getId());
        List<LedgerResponseDto> dtoLedgers = ledgers.stream()
                .map(LedgerResponseDto::fromLedger)
                .toList();

        return ResponseEntity.ok().body(dtoLedgers);

    }

    @PatchMapping("/{ledgerId}/settings/modifyAccess/addAccess")
    public ResponseEntity<LedgerAccessDto> addUserRole(
            @Valid @RequestBody LedgerAccessDto accessDto,
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        LedgerAccessDto responseDto = ledgerService.addLedgerAccess(accessDto);
        return ResponseEntity.ok().body(responseDto);
    }


}
