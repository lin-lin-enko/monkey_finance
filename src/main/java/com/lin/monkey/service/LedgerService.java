package com.lin.monkey.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lin.monkey.dto.LedgerCreationDto;
import com.lin.monkey.dto.LedgerResponseDto;
import com.lin.monkey.dto.LedgerUpdateDto;
import com.lin.monkey.model.Ledger;
import com.lin.monkey.model.UsersLedgers;
import com.lin.monkey.repository.LedgerRepository;
import com.lin.monkey.repository.UserRepository;
import com.lin.monkey.repository.UsersLedgersRepository;
import com.lin.monkey.security.CustomUserDetails;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LedgerService {
    private final LedgerRepository ledgerRepository;
    private final UsersLedgersRepository usersLedgersRepository;

    public LedgerService(LedgerRepository ledgerRepository, UserRepository userRepository, UsersLedgersRepository usersLedgersRepository) {
        this.ledgerRepository = ledgerRepository;
        this.usersLedgersRepository = usersLedgersRepository;
    }


    @Transactional
    public LedgerResponseDto create(LedgerCreationDto dto) {
        UUID userId = getCurrentUserId();

        Ledger ledger = new Ledger();
        ledger.setName(dto.getName());
        ledger.setDescription(dto.getDescription());
        ledger.setOwnerId(userId);
        try {
            ledger = ledgerRepository.save(ledger);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("A ledger with the same name already exists");
        }
        UUID ledgerId = ledger.getId();
        UsersLedgers entry = new UsersLedgers();
        entry.setUserId(userId);
        entry.setLedgerId(ledgerId);
        entry.setRole("ADMIN");
        usersLedgersRepository.save(entry);

        return LedgerResponseDto.fromLedger(ledger);
    }

    public List<Ledger> findAllByOwnerId(UUID ownerId) {
        return ledgerRepository.findAllByOwnerId(ownerId);
    }


    @Transactional
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public LedgerResponseDto update(LedgerUpdateDto dto, UUID ledgerId) {
        Ledger ledger = ledgerRepository.findById(ledgerId)
                .orElseThrow(() -> new IllegalArgumentException("Ledger not found"));
        canAccessAndMaintain(ledgerId);
        if (dto.getName() != null && !dto.getName().isBlank()) {
            ledger.setName(dto.getName());
        }
        if (dto.getDescription() != null) {
            ledger.setDescription(dto.getDescription());
        }

        Ledger updatedLedger = ledgerRepository.save(ledger);
        return LedgerResponseDto.fromLedger(updatedLedger);
    }


    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
            throw new RuntimeException("User is not authenticated");
        }
        // getPrincipal() returns obj, so i convert it to CustomerUserDetails
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        return userDetails.getId();
    }

    private void canAccess(UUID ledgerId) {
        usersLedgersRepository.findUserRoleInLedger(getCurrentUserId(), ledgerId)
                .orElseThrow(() -> new AccessDeniedException("You don't have access to this ledger"));
    }

    private void canAccessAndMaintain(UUID ledgerId) {
        String userRole = usersLedgersRepository.findUserRoleInLedger(getCurrentUserId(), ledgerId)
                .orElseThrow(() -> new AccessDeniedException("You don't have access to this ledger"));
        if (!"ADMIN".equals(userRole)) {
            throw new AccessDeniedException("Only admins can create, delete or change transations");
        }
    }
}

