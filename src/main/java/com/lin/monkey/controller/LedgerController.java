package com.lin.monkey.controller;

import com.lin.monkey.dto.LedgerCreationDto;
import com.lin.monkey.dto.LedgerResponseDto;
import com.lin.monkey.service.LedgerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/ledgers")
public class LedgerController {
    private final LedgerService ledgerService;

    public LedgerController(LedgerService ledgerService) {
        this.ledgerService = ledgerService;
    }

    @PostMapping("/")
    public ResponseEntity<LedgerResponseDto> create(
            @Valid @RequestBody LedgerCreationDto ledgerCreationDto
    ) {
        LedgerResponseDto responseDto = ledgerService.create(ledgerCreationDto);

        return ResponseEntity.created(URI.create("/api/ledgers/" + responseDto.name())).body(responseDto);
    }
}
