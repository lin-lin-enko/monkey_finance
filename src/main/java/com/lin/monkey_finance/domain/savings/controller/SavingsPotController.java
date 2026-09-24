package com.lin.monkey_finance.domain.savings.controller;

import com.lin.monkey_finance.domain.savings.dto.SavingsPotCreateDto;
import com.lin.monkey_finance.domain.savings.dto.SavingsPotResponseDto;
import com.lin.monkey_finance.domain.savings.service.SavingsPotService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RequestMapping("/api/v1/ledgers/{ledgerId}/savings")
@RestController
public class SavingsPotController {

    private final SavingsPotService savingsPotService;

    public SavingsPotController(
            SavingsPotService savingsPotService
    ){
        this.savingsPotService = savingsPotService;
    }

    @PostMapping()
    public ResponseEntity<SavingsPotResponseDto> create(
            @Valid @RequestBody SavingsPotCreateDto createDto,
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID ledgerId
            ){
        UUID userId = UUID.fromString(jwt.getSubject());
        SavingsPotResponseDto responseDto = savingsPotService.create(userId, ledgerId, createDto);
        return ResponseEntity.ok(responseDto);
    }
}
