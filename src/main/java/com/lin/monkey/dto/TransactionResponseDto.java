package com.lin.monkey.dto;

import com.lin.monkey.model.Transaction;
import com.lin.monkey.model.TransactionType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TransactionResponseDto(

        UUID id,
        UUID ledgerId,
        String ledgerName,
        BigDecimal amount,
        TransactionType type,
        String title,
        String description,
        UUID categoryId,
        String categoryName,
        UUID subcategoryId,
        String subcategoryName,
        LocalDateTime createdAt,
        LocalDateTime transactionDate
) {
    public static TransactionResponseDto fromTransaction(Transaction transaction) {
        return new TransactionResponseDto(
                transaction.getId(),
                transaction.getLedgerId(),
                transaction.getLedger() != null ? transaction.getLedger().getName() : null,
                transaction.getAmount(),
                transaction.getType(),
                transaction.getTitle(),
                transaction.getDescription(),
                transaction.getCategoryId(),
                transaction.getCategory() != null ? transaction.getCategory().getName() : null,
                transaction.getSubcategoryId(),
                transaction.getSubcategory() != null ? transaction.getSubcategory().getName() : null,
                transaction.getCreatedAt(),
                transaction.getTransactionDate()
        );
    }
}
