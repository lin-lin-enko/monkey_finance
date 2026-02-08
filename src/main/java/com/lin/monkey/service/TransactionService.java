package com.lin.monkey.service;

import com.lin.monkey.dto.TransactionCreationDto;
import com.lin.monkey.dto.TransactionResponseDto;
import com.lin.monkey.model.*;
import com.lin.monkey.repository.CategoryRepository;
import com.lin.monkey.repository.LedgerRepository;
import com.lin.monkey.repository.SubcategoryRepository;
import com.lin.monkey.repository.TransactionRepository;
import com.lin.monkey.security.CustomUserDetails;
import jakarta.transaction.Transactional;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;


import java.time.LocalDateTime;
import java.util.UUID;

@Service
public class TransactionService {
    private final TransactionRepository transactionRepository;
    private final LedgerRepository ledgerRepository;
    private final CategoryRepository categoryRepository;
    private final SubcategoryRepository subcategoryRepository;

    public TransactionService(
            TransactionRepository transactionRepository,
            LedgerRepository ledgerRepository,
            CategoryRepository categoryRepository,
            SubcategoryRepository subcategoryRepository) {
        this.transactionRepository = transactionRepository;
        this.ledgerRepository = ledgerRepository;
        this.categoryRepository = categoryRepository;
        this.subcategoryRepository = subcategoryRepository;
    }

    @Transactional
    public TransactionResponseDto create(TransactionCreationDto creationDto, UUID ledgerId) {
        Ledger ledger = ledgerRepository.findByIdAndOwnerId(ledgerId, getCurrentUserId())
                .orElseThrow(() -> new AccessDeniedException("Ledger wasn't found or access denied"));

        Category category = categoryRepository.findById(creationDto.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category wasn't found"));

        Subcategory subcategory = creationDto.getSubcategoryId() != null ? subcategoryRepository.findById(creationDto.getSubcategoryId()).orElse(null) : null;

        Transaction transaction = new Transaction();
        transaction.setLedger(ledger);
        transaction.setTitle(creationDto.getTitle());
        transaction.setAmount(creationDto.getAmount());
        transaction.setType(creationDto.getType() != null ? creationDto.getType() : TransactionType.EXPENSE);
        transaction.setDescription(creationDto.getDescription());
        transaction.setCategory(category);
        transaction.setSubcategory(subcategory);
        transaction.setTransactionDate(creationDto.getTransactionDate() != null ? creationDto.getTransactionDate() : LocalDateTime.now());

        transaction = transactionRepository.save(transaction);

        transaction = transactionRepository.findByIdWithCategories(transaction.getId())
                .orElseThrow(() -> new IllegalArgumentException("Transaction wasn't found"));

        return TransactionResponseDto.fromTransaction(transaction);
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
