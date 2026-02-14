package com.lin.monkey.service;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lin.monkey.dto.TransactionCreationDto;
import com.lin.monkey.dto.TransactionResponseDto;
import com.lin.monkey.dto.TransactionUpdateDto;
import com.lin.monkey.model.*;
import com.lin.monkey.repository.*;
import com.lin.monkey.security.CustomUserDetails;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final LedgerRepository ledgerRepository;
    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;
    private final UsersLedgersRepository usersLedgersRepository;

    public TransactionService(
            TransactionRepository transactionRepository,
            LedgerRepository ledgerRepository,
            CategoryRepository categoryRepository,
            SubcategoryRepository subcategoryRepository,
            UsersLedgersRepository usersLedgersRepository) {
        this.transactionRepository = transactionRepository;
        this.ledgerRepository = ledgerRepository;
        this.categoryRepository = categoryRepository;
        this.subcategoryRepository = subcategoryRepository;
        this.usersLedgersRepository = usersLedgersRepository;
    }

    @Transactional
    public TransactionResponseDto create(TransactionCreationDto dto, UUID ledgerId) {
        Ledger ledger = ledgerRepository.findByIdAndOwnerId(ledgerId, getCurrentUserId())
                .orElseThrow(() -> new AccessDeniedException("Ledger wasn't found or access denied"));

        Category category = categoryRepository.findById(dto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category wasn't found"));

        Subcategory subcategory = dto.getSubcategoryId() != null ? subcategoryRepository.findById(dto.getSubcategoryId()).orElse(null) : null;

        Transaction transaction = new Transaction();
        transaction.setLedger(ledger);
        transaction.setTitle(dto.getTitle());
        transaction.setAmount(dto.getAmount());
        transaction.setType(dto.getType() != null ? dto.getType() : TransactionType.EXPENSE);
        transaction.setDescription(dto.getDescription());
        transaction.setCategory(category);
        transaction.setSubcategory(subcategory);
        transaction.setTransactionDate(dto.getTransactionDate() != null ? dto.getTransactionDate() : LocalDateTime.now());

        transaction = transactionRepository.save(transaction);

        transaction = transactionRepository.findByIdWithCategories(transaction.getId())
                .orElseThrow(() -> new IllegalArgumentException("Transaction wasn't found"));

        return TransactionResponseDto.fromTransaction(transaction);
    }

    @Transactional
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public TransactionResponseDto update(TransactionUpdateDto dto, UUID transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new AccessDeniedException("Transaction wasn't found or access denied"));
        UUID currentLedgerId = transaction.getLedgerId();
        String userRole = usersLedgersRepository.findUserRoleInLedger(getCurrentUserId(), currentLedgerId)
                .orElseThrow(() -> new AccessDeniedException("You don't have access to this ledger"));

        if (!"ADMIN".equals(userRole)) {
            throw new AccessDeniedException("Only admins can edit transations");
        }

        if (dto.getLedgerId() != null) {
            Ledger targetLedger = ledgerRepository.findById(dto.getLedgerId())
                    .orElseThrow(() -> new IllegalArgumentException("Ledger not found"));
            String targetLedgerRole = usersLedgersRepository.findUserRoleInLedger(getCurrentUserId(), dto.getLedgerId())
                    .orElseThrow(() -> new AccessDeniedException("You have no rights on the target ledger"));
            if (!"ADMIN".equals(targetLedgerRole)) {
                throw new AccessDeniedException("You must be admin in the target ledger");
            }

            transaction.setLedger(targetLedger);
        }

        if (dto.getTitle() != null && !dto.getTitle().isBlank()) {
            transaction.setTitle(dto.getTitle());
        }

        if (dto.getAmount() != null) {
            transaction.setAmount(dto.getAmount());
        }

        if (dto.getDescription() != null) {
            transaction.setDescription(dto.getDescription());
        }

        if (dto.getCategoryId() != null) {
            Category category = categoryRepository.findById(dto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            transaction.setCategory(category);
        }

        if (dto.getSubcategoryId() != null) {
            if (dto.getSubcategoryId().equals(UUID.fromString("00000000-0000-0000-0000-000000000000"))) {
                transaction.setSubcategory(null);
            } else {
                Subcategory subcategory = subcategoryRepository.findById(dto.getSubcategoryId())
                        .orElseThrow(() -> new IllegalArgumentException("Subcategory wasn't found"));
                transaction.setSubcategory(subcategory);
            }
        }

        if (dto.getTransactionDate() != null) {
            transaction.setTransactionDate(dto.getTransactionDate());
        }

        Transaction updatedTransaction = transactionRepository.save(transaction);
        return TransactionResponseDto.fromTransaction(updatedTransaction);
    }


    public TransactionResponseDto getById(UUID transactionId) {
        Transaction transaction = transactionRepository.findById(transactionId).orElse(null);

        return transaction != null ? TransactionResponseDto.fromTransaction(transaction) : null;
    }

    public List<TransactionResponseDto> getFiltered(UUID ledgerId, UUID categoryId, UUID subcategoryId, TransactionType type, LocalDateTime fromDate, LocalDateTime toDate) {
        Specification<Transaction> specifications = TransactionSpecifications.withFilters(
                ledgerId, categoryId, subcategoryId, type, fromDate, toDate
        );
        List<Transaction> transactions = transactionRepository.findAll(specifications);
        return transactions.stream().map(TransactionResponseDto::fromTransaction).toList();
    }


    private UUID getCurrentUserId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getPrincipal() instanceof String) {
            throw new RuntimeException("User is not authenticated");
        }
        CustomUserDetails userDetails = (CustomUserDetails) auth.getPrincipal();
        return userDetails.getId();
    }

}
