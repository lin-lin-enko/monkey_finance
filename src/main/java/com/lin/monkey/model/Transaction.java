package com.lin.monkey.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import jdk.jfr.Timestamp;
import org.hibernate.annotations.ColumnTransformer;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "ledger_id", nullable = false)
    private UUID ledgerId;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ledger_id", insertable = false, updatable = false, nullable = false)
    private Ledger ledger;


    @NotNull(message = "Amount is required")
    @DecimalMin(value = "0.01", inclusive = false, message = "Amount must be greater than 0")
    @Digits(integer = 10, fraction = 2, message = "Amount must be up to 10 digits before decimal and 2 after")
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    @ColumnTransformer(read = "type::text", write = "?::transaction_type")
    private TransactionType type = TransactionType.EXPENSE;

    @NotBlank(message = "Transaction must have a title")
    @Size(min = 3, max = 60, message = "Title must be 3 to 60 characters long")
    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    @NotNull
    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", insertable = false, nullable = false, updatable = false)
    private Category category;

    @Column(name = "subcategory_id", nullable = false)
    private UUID subcategoryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subcategory_id", insertable = false, nullable = false, updatable = false)
    private Subcategory subcategory;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "transaction_date", nullable = false)
    private LocalDateTime transactionDate;

    public UUID getId() {
        return id;
    }

    public UUID getLedgerId() {
        return ledgerId;
    }

    public Ledger getLedger() {
        return ledger;
    }

    public void setLedger(Ledger ledger) {
        this.ledger = ledger;
        this.ledgerId = (ledger != null) ? ledger.getId() : null;
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

    public Category getCategory() {
        return category;
    }

    public void setCategory(Category category) {
        this.category = category;
        this.categoryId = (category != null) ? category.getId() : null;
    }

    public UUID getSubcategoryId() {
        return subcategoryId;
    }

    public Subcategory getSubcategory() {
        return subcategory;
    }

    public void setSubcategory(Subcategory subcategory) {
        this.subcategory = subcategory;
        this.subcategoryId = (subcategory != null) ? subcategory.getId() : null;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getTransactionDate() {
        return transactionDate;
    }

    public void setTransactionDate(LocalDateTime transactionDate) {
        this.transactionDate = transactionDate;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Transaction transaction = (Transaction) obj;
        return id.equals(transaction.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
