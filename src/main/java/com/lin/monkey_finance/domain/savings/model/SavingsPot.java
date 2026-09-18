package com.lin.monkey_finance.domain.savings.model;

import com.lin.monkey_finance.domain.account.model.Account;
import com.lin.monkey_finance.domain.account.model.Currency;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Generated;
import org.hibernate.generator.EventType;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(schema = "dev", name = "savings_pots")
public class SavingsPot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @JoinColumn(name = "account_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Account account;

    @Column(nullable = false, length = 50)
    @Size(min = 2, max = 50, message = "SavingsPot name must be 2 to 50 characters long")
    private String name;

    @Column(length = 255)
    @Size(min = 2, max = 255, message = "Description name must be 2 to 255 characters long")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency = Currency.EUR;

    @Column(name = "target_sum", nullable = false)
    private BigDecimal targetAmount;

    @Column(name = "due_date")
    private OffsetDateTime dueDate;

    @Column(name = "percentage_rate")
    private BigDecimal percentageRate;

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", insertable = false, updatable = false, nullable = false)
    private OffsetDateTime createdAt;

    protected SavingsPot(){}

    public SavingsPot(
            Account account,
            String name,
            String description,
            Currency currency,
            BigDecimal targetAmount,
            OffsetDateTime dueDate,
            BigDecimal percentageRate
    ){
        this.account = account;
        this.name = name;
        this.description = description;
        this.currency = currency;
        this.targetAmount = targetAmount;
        this.dueDate = dueDate;
        this.percentageRate = percentageRate;
    }

    public UUID getId() {
        return id;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Currency getCurrency() {
        return currency;
    }

    public void setCurrency(Currency currency) {
        this.currency = currency;
    }

    public BigDecimal getTargetAmount() {
        return targetAmount;
    }

    public void setTargetAmount(BigDecimal targetAmount) {
        this.targetAmount = targetAmount;
    }

    public OffsetDateTime getDueDate() {
        return dueDate;
    }

    public void setDueDate(OffsetDateTime dueDate) {
        this.dueDate = dueDate;
    }

    public BigDecimal getPercentageRate() {
        return percentageRate;
    }

    public void setPercentageRate(BigDecimal percentageRate) {
        this.percentageRate = percentageRate;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }
}
