package com.lin.monkey_finance.domain.savings.controller;

import com.lin.monkey_finance.domain.savings.dto.SavingsPotCreateDto;
import com.lin.monkey_finance.domain.savings.dto.SavingsPotResponseDto;
import com.lin.monkey_finance.domain.savings.dto.SavingsPotUpdateDto;
import com.lin.monkey_finance.domain.savings.service.SavingsPotService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
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

    @GetMapping()
    public ResponseEntity<List<SavingsPotResponseDto>> getAll(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID ledgerId
    ){
        UUID userId = UUID.fromString(jwt.getSubject());
        List<SavingsPotResponseDto> responseDtoList = savingsPotService.getAll(userId, ledgerId);
        return ResponseEntity.ok(responseDtoList);
    }

    @PostMapping()
    public ResponseEntity<SavingsPotResponseDto> create(
            @Valid @RequestBody SavingsPotCreateDto createDto,
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID ledgerId
            ){
        UUID userId = UUID.fromString(jwt.getSubject());
        SavingsPotResponseDto responseDto = savingsPotService.create(userId, ledgerId, createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }

    @PatchMapping("/{savingsPotId}")
    public ResponseEntity<SavingsPotResponseDto> edit(
            @Valid @RequestBody SavingsPotUpdateDto updateDto,
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID ledgerId,
            @PathVariable UUID savingsPotId
    ){
        UUID userId = UUID.fromString(jwt.getSubject());
        SavingsPotResponseDto responseDto = savingsPotService.edit(userId, ledgerId, savingsPotId, updateDto);
        return ResponseEntity.ok(responseDto);
    }
}
