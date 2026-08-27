package com.lin.monkey_finance.domain.ledger.controller;

import com.lin.monkey_finance.domain.ledger.dto.*;
import com.lin.monkey_finance.domain.ledger.service.LedgerMemberService;
import com.lin.monkey_finance.domain.ledger.service.LedgerService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ledgers")
public class LedgerController {
    private final LedgerService ledgerService;
    private final LedgerMemberService ledgerMemberService;

    public LedgerController(
            LedgerService ledgerService,
            LedgerMemberService ledgerMemberService
    ){
        this.ledgerService = ledgerService;
        this.ledgerMemberService = ledgerMemberService;
    }

    @GetMapping()
    public ResponseEntity<List<LedgerResponseDto>> getCurrentUserLedgers(@AuthenticationPrincipal Jwt jwt){
        UUID userId = UUID.fromString(jwt.getSubject());

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

    @DeleteMapping("/{ledgerId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID ledgerId
    ){
        UUID userId = UUID.fromString(jwt.getSubject());
        ledgerService.delete(userId, ledgerId);
        return ResponseEntity.status(204).build();
    }

    @PostMapping("/{ledgerId}/members")
    public ResponseEntity<LedgerMemberResponseDto> addMember(
            @PathVariable UUID ledgerId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody LedgerMemberRequestDto requestDto
    ){
        UUID userId = UUID.fromString(jwt.getSubject());
        LedgerMemberResponseDto responseDto = ledgerMemberService.add(requestDto, ledgerId, userId);
        return ResponseEntity.status(201).body(responseDto);
    }

    @PatchMapping("/invitations/{ledgerId}/accept")
    public ResponseEntity<LedgerMemberResponseDto> acceptInvitation(
            @PathVariable UUID ledgerId,
            @AuthenticationPrincipal Jwt jwt
    ){
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(ledgerMemberService.acceptInvitation(ledgerId, userId));
    }

    @DeleteMapping("/invitations/{ledgerId}/decline")
    public ResponseEntity<String> declineInvitation(
            @PathVariable UUID ledgerId,
            @AuthenticationPrincipal Jwt jwt
    ){
        UUID userId = UUID.fromString(jwt.getSubject());
        ledgerMemberService.declineInvitation(ledgerId, userId);
        return ResponseEntity.ok("Invitation declined");
    }
}
