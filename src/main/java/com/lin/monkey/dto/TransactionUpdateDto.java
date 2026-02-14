package com.lin.monkey.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.lin.monkey.model.Category;
import com.lin.monkey.model.TransactionType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class TransactionUpdateDto {

    private UUID ledgerId;
    @Size(min = 3, max = 60, message = "Title must be 3 to 60 characters long")
    private String title;
    @DecimalMin(value = "0.01", inclusive = false, message = "Amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Amount must be up to 10 digits before decimal and 2 after")
    private BigDecimal amount;
    private TransactionType type;
    @Size(max = 1000, message = "Description must be 1000 characters maximum")
    private String description;
    private UUID categoryId;
    private UUID subcategoryId;
    @PastOrPresent(message = "Transaction date cannot be in the future")
    private LocalDateTime transactionDate;


    public UUID getLedgerId() {
        return ledgerId;
    }

    public void setLedgerId(UUID ledgerId) {
        this.ledgerId = ledgerId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
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

    public void setSubcategoryId(UUID subcategoryId) {
        this.subcategoryId = subcategoryId;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }
}
