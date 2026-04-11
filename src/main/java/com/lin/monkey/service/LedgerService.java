package com.lin.monkey.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lin.monkey.dto.LedgerAccessDto;
import com.lin.monkey.dto.LedgerCreationDto;
import com.lin.monkey.dto.LedgerResponseDto;
import com.lin.monkey.dto.LedgerUpdateDto;
import com.lin.monkey.model.Ledger;
import com.lin.monkey.model.UserRoleInLedger;
import com.lin.monkey.model.UsersLedgers;
import com.lin.monkey.repository.LedgerRepository;
import com.lin.monkey.repository.UsersLedgersRepository;
import com.lin.monkey.security.CustomUserDetails;
import jakarta.transaction.Transactional;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

@Service
public class LedgerService {
    private final LedgerRepository ledgerRepository;
    private final UsersLedgersRepository usersLedgersRepository;

    public LedgerService(LedgerRepository ledgerRepository, UsersLedgersRepository usersLedgersRepository) {
        this.ledgerRepository = ledgerRepository;
        this.usersLedgersRepository = usersLedgersRepository;
    }


    @Transactional
    public LedgerResponseDto create(LedgerCreationDto dto) {
        UUID userId = getCurrentUserId();

        Ledger ledger = new Ledger();
        ledger.setName(dto.getName());
        ledger.setDescription(dto.getDescription());
        ledger.setCreatorId(userId);
        try {
            ledger = ledgerRepository.save(ledger);
        } catch (DataIntegrityViolationException e) {
            throw new IllegalArgumentException("A ledger with the same name already exists");
        }
        UUID ledgerId = ledger.getId();
        UsersLedgers entry = new UsersLedgers();
        entry.setUserId(userId);
        entry.setLedgerId(ledgerId);
        entry.setRole(UserRoleInLedger.OWNER);
        usersLedgersRepository.save(entry);

        return LedgerResponseDto.fromLedger(ledger);
    }

    public List<Ledger> findAllByCreatorId(UUID creatorId) {
        return ledgerRepository.findAllByCreatorId(creatorId);
    }

    public List<Ledger> findAllAccessedByUser(UUID userId) {

        List<UsersLedgers> usersLedgers = usersLedgersRepository.findByUserId(userId);
        return usersLedgersRepository.findLedgersAccessedByUser(userId);
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

    @Transactional
    public LedgerAccessDto addLedgerAccess(LedgerAccessDto accessDto) {
        ledgerRepository.findById(accessDto.ledgerId())
                .orElseThrow(() -> new IllegalArgumentException("Ledger not found"));
        canAccessAndMaintain(accessDto.ledgerId());
        UserRoleInLedger currentUserRole = usersLedgersRepository.findUserRoleInLedger(getCurrentUserId(), accessDto.ledgerId())
                .orElseThrow(() -> new AccessDeniedException("User doesn't have access to this ledger"));

        switch (accessDto.role()) {
            case UserRoleInLedger.OWNER: {
                if (!currentUserRole.equals(UserRoleInLedger.OWNER)) {
                    throw new AccessDeniedException("User must own the ledger to make someone an owner");
                }
            }
            case UserRoleInLedger.ADMIN: {
                if (!currentUserRole.equals(UserRoleInLedger.OWNER) && !currentUserRole.equals(UserRoleInLedger.ADMIN)) {
                    throw new AccessDeniedException("User must own the ledger or be an admin to make someone an admin");
                }
            }
            default: {
                if (!currentUserRole.equals(UserRoleInLedger.OWNER) && !currentUserRole.equals(UserRoleInLedger.ADMIN)) {
                    throw new AccessDeniedException("User must be an owner or an admin to give someone access to the ledger");
                }
            }
        }

        UsersLedgers usersLedgers = new UsersLedgers();
        usersLedgers.setRole(accessDto.role());
        usersLedgers.setLedgerId(accessDto.ledgerId());
        usersLedgers.setUserId(accessDto.userId());

        UsersLedgers savedAccess = usersLedgersRepository.save(usersLedgers);
        return new LedgerAccessDto(savedAccess.getUserId(), savedAccess.getLedgerId(), savedAccess.getRole());
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

//    private void canAccess(UUID ledgerId) {
//        usersLedgersRepository.findUserRoleInLedger(getCurrentUserId(), ledgerId)
//                .orElseThrow(() -> new AccessDeniedException("You don't have access to this ledger"));
//    }

    private void canAccessAndMaintain(UUID ledgerId) {
        UserRoleInLedger userRole = usersLedgersRepository.findUserRoleInLedger(getCurrentUserId(), ledgerId)
                .orElseThrow(() -> new AccessDeniedException("You don't have access to this ledger"));
        if (userRole != UserRoleInLedger.ADMIN && userRole != UserRoleInLedger.OWNER) {
            throw new AccessDeniedException("Only admins and owners can create, delete or change ledgers");
        }
    }
}

