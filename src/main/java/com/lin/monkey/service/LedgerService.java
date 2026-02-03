package com.lin.monkey.service;

import com.lin.monkey.dto.LedgerCreationDto;
import com.lin.monkey.dto.LedgerResponseDto;
import com.lin.monkey.model.Ledger;
import com.lin.monkey.model.UsersLedgers;
import com.lin.monkey.repository.LedgerRepository;
import com.lin.monkey.repository.UserRepository;
import com.lin.monkey.repository.UsersLedgersRepository;
import com.lin.monkey.security.CustomUserDetails;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

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

    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
            throw new RuntimeException("User is not authenticated");
        }
        // getPrincipal() returns obj, so i convert it to CustomerUserDetails
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        return userDetails.getId();
    }

    public Optional<Ledger> findByName(String name) {
        return ledgerRepository.findByName(name);
    }

    public boolean existsByName(String name) {
        return ledgerRepository.existsByName(name);
    }

    public Optional<Ledger> findByOwnerId(UUID id) {
        return ledgerRepository.findByOwnerId(id);
    }
}
