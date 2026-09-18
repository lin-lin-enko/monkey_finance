package com.lin.monkey_finance.domain.ledger.controller;

import com.lin.monkey_finance.domain.ledger.dto.*;
import com.lin.monkey_finance.domain.ledger.model.AccessType;
import com.lin.monkey_finance.domain.ledger.service.LedgerMembershipService;
import com.lin.monkey_finance.domain.ledger.service.LedgerService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/ledgers")
public class LedgerController {
    private final LedgerService ledgerService;
    private final LedgerMembershipService ledgerMembershipService;

    public LedgerController(
            LedgerService ledgerService,
            LedgerMembershipService ledgerMembershipService
    ){
        this.ledgerService = ledgerService;
        this.ledgerMembershipService = ledgerMembershipService;
    }

    @GetMapping()
    public ResponseEntity<List<LedgerResponseDto>> getCurrentUserLedgers(@AuthenticationPrincipal Jwt jwt){
        UUID userId = UUID.fromString(jwt.getSubject());

        List<LedgerResponseDto> ledgerResponseDtoList = ledgerService.getCurrentUserLedgers(userId);
        return ResponseEntity.ok(ledgerResponseDtoList);
    }

    @GetMapping("/{ledgerId}")
    public ResponseEntity<LedgerDetailedResponseDto> getLedgerById(@PathVariable UUID ledgerId) {
        LedgerDetailedResponseDto dto = ledgerService.getById(ledgerId);
        return ResponseEntity.ok(dto);
    }

    @PostMapping()
    public ResponseEntity<LedgerDetailedResponseDto> create(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody LedgerRequestDto ledgerRequestDto
    ){
        UUID userId = UUID.fromString(jwt.getSubject());
        LedgerDetailedResponseDto ledgerDetailedResponseDto = ledgerService.create(ledgerRequestDto, userId);
        return ResponseEntity.status(HttpStatus.CREATED).body(ledgerDetailedResponseDto);
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
    public ResponseEntity<LedgerMembershipResponseDto> addMember(
            @PathVariable UUID ledgerId,
            @AuthenticationPrincipal Jwt jwt,
            @Valid @RequestBody LedgerMembershipRequestDto requestDto
    ){
        UUID userId = UUID.fromString(jwt.getSubject());
        LedgerMembershipResponseDto responseDto = ledgerMembershipService.add(requestDto, ledgerId, userId);
        return ResponseEntity.status(201).body(responseDto);
    }

    @PatchMapping("/invitations/{ledgerId}/accept")
    public ResponseEntity<LedgerMembershipResponseDto> acceptInvitation(
            @PathVariable UUID ledgerId,
            @AuthenticationPrincipal Jwt jwt
    ){
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(ledgerMembershipService.acceptInvitation(ledgerId, userId));
    }

    @DeleteMapping("/invitations/{ledgerId}/decline")
    public ResponseEntity<String> declineInvitation(
            @PathVariable UUID ledgerId,
            @AuthenticationPrincipal Jwt jwt
    ){
        UUID userId = UUID.fromString(jwt.getSubject());
        ledgerMembershipService.declineInvitation(ledgerId, userId);
        return ResponseEntity.ok("Invitation declined");
    }

    @PatchMapping("/{ledgerId}/leave")
    public ResponseEntity<String> leaveLedger(
            @PathVariable UUID ledgerId,
            @AuthenticationPrincipal Jwt jwt
    ){
        UUID userId = UUID.fromString(jwt.getSubject());
        ledgerMembershipService.leaveLedger(ledgerId, userId);
        return ResponseEntity.ok("User successfully left the ledger");
    }

    @PatchMapping("/{ledgerId}/members/{targetUserId}/block")
    public ResponseEntity<LedgerMembershipResponseDto> block(
            @PathVariable UUID ledgerId,
            @PathVariable UUID targetUserId,
            @AuthenticationPrincipal Jwt jwt
    ){
        UUID userId = UUID.fromString(jwt.getSubject());
        LedgerMembershipResponseDto responseDto = ledgerMembershipService.block(ledgerId, targetUserId, userId);
        return ResponseEntity.ok(responseDto);
    }

    @PatchMapping("/{ledgerId}/members/{targetUserId}/unblock")
    public ResponseEntity<LedgerMembershipResponseDto> unblock(
            @PathVariable UUID ledgerId,
            @PathVariable UUID targetUserId,
            @AuthenticationPrincipal Jwt jwt
    ){
        UUID userId = UUID.fromString(jwt.getSubject());
        LedgerMembershipResponseDto responseDto = ledgerMembershipService.unblock(ledgerId, targetUserId, userId);
        return ResponseEntity.ok(responseDto);
    }

    @PatchMapping("/{ledgerId}/members/{targetUserId}/change-access")
    public ResponseEntity<LedgerMembershipResponseDto> changeAccess(
            @PathVariable UUID ledgerId,
            @PathVariable UUID targetUserId,
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody Map<String, String> body
    ){
        UUID userId = UUID.fromString(jwt.getSubject());
        String accessType = body.get("accessType");
        AccessType targetAccessType = AccessType.fromString(accessType);

        LedgerMembershipResponseDto responseDto = ledgerMembershipService.changeAccess(ledgerId, targetUserId, userId, targetAccessType);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{ledgerId}/members/invitations/{targetUserId}")
    public ResponseEntity<Void> revokeInvitation(
            @PathVariable UUID ledgerId,
            @PathVariable UUID targetUserId,
            @AuthenticationPrincipal Jwt jwt
    ){
        UUID userId = UUID.fromString(jwt.getSubject());
        ledgerMembershipService.revokeInvitation(ledgerId, targetUserId, userId);
        return ResponseEntity.status(204).build();
    }

    @DeleteMapping("/{ledgerId}/members/{targetUserId}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID ledgerId,
            @PathVariable UUID targetUserId,
            @AuthenticationPrincipal Jwt jwt
    ){
        UUID userId = UUID.fromString(jwt.getSubject());
        ledgerMembershipService.deleteMember(ledgerId, targetUserId, userId);
        return ResponseEntity.status(204).build();
    }

    @PatchMapping("/{ledgerId}/members/{targetUserId}/transfer-ownership")
    public ResponseEntity<LedgerMembershipResponseDto> transferOwnership(
            @PathVariable UUID ledgerId,
            @PathVariable UUID targetUserId,
            @AuthenticationPrincipal Jwt jwt
    ){
        UUID userId = UUID.fromString(jwt.getSubject());
        LedgerMembershipResponseDto responseDto = ledgerMembershipService.transferOwnership(userId, targetUserId, ledgerId);
        return ResponseEntity.ok(responseDto);
    }
}
