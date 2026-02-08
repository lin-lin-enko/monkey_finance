package com.lin.monkey.dto;

import com.lin.monkey.model.TransactionType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class TransactionCreationDto {

    @NotBlank(message = "Transaction must have a title")
    @Size(min = 3, max = 60, message = "Title must be 3 to 60 characters long")
    private String title;

    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", inclusive = false, message = "Amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Amount must be up to 10 digits before decimal and 2 after")
    private BigDecimal amount;

    @NotNull(message = "Transaction must have a type")
    private TransactionType type;

    @Size(max = 1000, message = "Description must be 1000 characters maximum")
    private String description;

    @NotNull(message = "Category id is required")
    private UUID categoryId;

    private UUID subcategoryId;

    @NotNull(message = "Transaction date is required")
    @PastOrPresent(message = "Transaction date cannot be in the future")
    private LocalDateTime transactionDate;

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(UUID categoryId) {
        this.categoryId = categoryId;
    }

    public UUID getSubcategoryId() {
        return subcategoryId;
    }

    public void setSubcategoryId(UUID subcaregoryId) {
        this.subcategoryId = subcaregoryId;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public TransactionType getType() {
        return type;
    }

    public void setType(TransactionType type) {
        this.type = type;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }
}
