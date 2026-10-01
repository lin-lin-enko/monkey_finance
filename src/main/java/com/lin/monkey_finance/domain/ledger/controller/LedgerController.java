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
    private final LedgerMembershipService membershipService;

    public LedgerController(
            LedgerService ledgerService,
            LedgerMembershipService membershipService
    ){
        this.ledgerService = ledgerService;
        this.membershipService = membershipService;
    }

    @GetMapping()
    public ResponseEntity<List<LedgerMembershipResponseDto>> getCurrentUserLedgers(@AuthenticationPrincipal Jwt jwt){
        UUID userId = UUID.fromString(jwt.getSubject());

        List<LedgerMembershipResponseDto> ledgerResponseDtoList = membershipService.getAllUserLedgers(userId);
        return ResponseEntity.ok(ledgerResponseDtoList);
    }

    @PostMapping()
    public ResponseEntity<LedgerDetailedResponseDto> create(
            @AuthenticationPrincipal Jwt jwt,
            @RequestBody LedgerCreateDto createDto
    ){
        UUID userId = UUID.fromString(jwt.getSubject());
        LedgerDetailedResponseDto ledgerDetailedResponseDto = ledgerService.create(userId, createDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ledgerDetailedResponseDto);
    }

    @PatchMapping("/{ledgerId}")
    public ResponseEntity<LedgerDetailedResponseDto> edit(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID ledgerId,
            @RequestBody @Valid LedgerUpdateDto updateDto
            ){
        UUID userId = UUID.fromString(jwt.getSubject());
        LedgerDetailedResponseDto ledgerDetailedResponseDto = ledgerService.edit(ledgerId, userId, updateDto);
        return ResponseEntity.ok(ledgerDetailedResponseDto);
    }

    @DeleteMapping("/{ledgerId}")
    public ResponseEntity<Void> delete(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID ledgerId
    ){
        UUID userId = UUID.fromString(jwt.getSubject());
        ledgerService.delete(ledgerId, userId);
        return ResponseEntity.status(204).build();
    }

    @PostMapping("/{ledgerId}/members")
    public ResponseEntity<LedgerMembershipResponseDto> addMember(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable UUID ledgerId,
            @Valid @RequestBody LedgerMembershipRequestDto requestDto
    ){
        UUID userId = UUID.fromString(jwt.getSubject());
        LedgerMembershipResponseDto responseDto = membershipService.add(ledgerId, userId, requestDto);
        return ResponseEntity.status(201).body(responseDto);
    }

    @PatchMapping("/invitations/{ledgerId}/accept")
    public ResponseEntity<LedgerMembershipResponseDto> acceptInvitation(
            @PathVariable UUID ledgerId,
            @AuthenticationPrincipal Jwt jwt
    ){
        UUID userId = UUID.fromString(jwt.getSubject());
        return ResponseEntity.ok(membershipService.acceptInvitation(ledgerId, userId));
    }

    @DeleteMapping("/invitations/{ledgerId}/decline")
    public ResponseEntity<String> declineInvitation(
            @PathVariable UUID ledgerId,
            @AuthenticationPrincipal Jwt jwt
    ){
        UUID userId = UUID.fromString(jwt.getSubject());
        membershipService.declineInvitation(ledgerId, userId);
        return ResponseEntity.ok("Invitation declined");
    }

    @PatchMapping("/{ledgerId}/leave")
    public ResponseEntity<String> leaveLedger(
            @PathVariable UUID ledgerId,
            @AuthenticationPrincipal Jwt jwt
    ){
        UUID userId = UUID.fromString(jwt.getSubject());
        membershipService.leaveLedger(ledgerId, userId);
        return ResponseEntity.ok("User successfully left the ledger");
    }

    @PatchMapping("/{ledgerId}/members/{targetUserId}/block")
    public ResponseEntity<LedgerMembershipResponseDto> block(
            @PathVariable UUID ledgerId,
            @PathVariable UUID targetUserId,
            @AuthenticationPrincipal Jwt jwt
    ){
        UUID userId = UUID.fromString(jwt.getSubject());
        LedgerMembershipResponseDto responseDto = membershipService.block(ledgerId, userId, targetUserId);
        return ResponseEntity.ok(responseDto);
    }

    @PatchMapping("/{ledgerId}/members/{targetUserId}/unblock")
    public ResponseEntity<LedgerMembershipResponseDto> unblock(
            @PathVariable UUID ledgerId,
            @PathVariable UUID targetUserId,
            @AuthenticationPrincipal Jwt jwt
    ){
        UUID userId = UUID.fromString(jwt.getSubject());
        LedgerMembershipResponseDto responseDto = membershipService.unblock(ledgerId, userId, targetUserId);
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

        LedgerMembershipResponseDto responseDto = membershipService.changeAccess(ledgerId, userId, targetUserId, targetAccessType);
        return ResponseEntity.ok(responseDto);
    }

    @DeleteMapping("/{ledgerId}/members/invitations/{targetUserId}")
    public ResponseEntity<Void> revokeInvitation(
            @PathVariable UUID ledgerId,
            @PathVariable UUID targetUserId,
            @AuthenticationPrincipal Jwt jwt
    ){
        UUID userId = UUID.fromString(jwt.getSubject());
        membershipService.revokeInvitation(ledgerId, userId, targetUserId);
        return ResponseEntity.status(204).build();
    }

    @DeleteMapping("/{ledgerId}/members/{targetUserId}")
    public ResponseEntity<Void> deleteMember(
            @PathVariable UUID ledgerId,
            @PathVariable UUID targetUserId,
            @AuthenticationPrincipal Jwt jwt
    ){
        UUID userId = UUID.fromString(jwt.getSubject());
        membershipService.deleteMember(ledgerId, userId, targetUserId);
        return ResponseEntity.status(204).build();
    }

    @PatchMapping("/{ledgerId}/members/{targetUserId}/transfer-ownership")
    public ResponseEntity<LedgerMembershipResponseDto> transferOwnership(
            @PathVariable UUID ledgerId,
            @PathVariable UUID targetUserId,
            @AuthenticationPrincipal Jwt jwt
    ){
        UUID userId = UUID.fromString(jwt.getSubject());
        LedgerMembershipResponseDto responseDto = membershipService.transferOwnership(ledgerId, userId, targetUserId);
        return ResponseEntity.ok(responseDto);
    }
}
