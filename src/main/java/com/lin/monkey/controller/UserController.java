package com.lin.monkey.controller;

import com.lin.monkey.dto.ChangeDefaultLedgerDto;
import com.lin.monkey.dto.LedgerResponseDto;
import com.lin.monkey.dto.UserResponseDto;
import com.lin.monkey.exception.UserNotFoundException;
import com.lin.monkey.model.Ledger;
import com.lin.monkey.model.User;
import com.lin.monkey.security.CustomUserDetails;
import com.lin.monkey.service.LedgerService;
import com.lin.monkey.service.UserService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;
    private final LedgerService ledgerService;

    public UserController(UserService userService, LedgerService ledgerService) {
        this.userService = userService;
        this.ledgerService = ledgerService;
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponseDto> getCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UUID userId = userDetails.getId();
        User user = userService.findById(userId)
                .orElseThrow(
                        () -> new UserNotFoundException("User not found with such id: " + userId));

        UserResponseDto dto = UserResponseDto.fromUser(user);

        return ResponseEntity.ok(dto);
    }

    @PatchMapping("/me/default-ledger")
    public ResponseEntity<UserResponseDto> changeDefaultLedger(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody ChangeDefaultLedgerDto changeDefaultLedgerDto
    ) {
        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        UserResponseDto userResponseDto = userService.changeDefaultLedger(userDetails.getId(), changeDefaultLedgerDto.ledgerId());

        return ResponseEntity.ok(userResponseDto);
    }

    @GetMapping("/me/ledgers")
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
}
