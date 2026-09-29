package com.lin.monkey_finance.domain.savings.model;

import com.lin.monkey_finance.domain.account.model.Currency;
import com.lin.monkey_finance.domain.ledger.model.Ledger;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.hibernate.annotations.Generated;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.generator.EventType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.UUID;

@Entity
@Table(schema = "dev", name = "savings_pots")
public class SavingsPot {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "id", nullable = false, updatable = false)
    private UUID id;

    @Column(nullable = false, length = 50)
    @Size(min = 2, max = 50, message = "SavingsPot name must be 2 to 50 characters long")
    private String name;

    @Column()
    @Size(min = 2, max = 255, message = "Description name must be 2 to 255 characters long")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 3)
    private Currency currency = Currency.EUR;

    @Column(name = "target_sum", nullable = false)
    private BigDecimal targetAmount;

    @Column(name = "due_date")
    private LocalDate dueDate;

    @Column(name = "percentage_rate")
    private BigDecimal percentageRate;

    @Column(name = "ledger_id", insertable = false, updatable = false)
    private UUID ledgerId;

    @JoinColumn(name = "ledger_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private Ledger ledger;

    @Generated(event = EventType.INSERT)
    @Column(name = "created_at", insertable = false, updatable = false, nullable = false)
    private OffsetDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private OffsetDateTime updatedAt;

    protected SavingsPot(){}

    public SavingsPot(
            String name,
            String description,
            Currency currency,
            BigDecimal targetAmount,
            LocalDate dueDate,
            BigDecimal percentageRate,
            Ledger ledger
    ){
        this.name = name;
        this.description = description;
        this.currency = currency;
        this.targetAmount = targetAmount;
        this.dueDate = dueDate;
        this.percentageRate = percentageRate;
        this.ledger = ledger;
    }

    public UUID getId() {
        return id;
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

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public BigDecimal getPercentageRate() {
        return percentageRate;
    }

    public void setPercentageRate(BigDecimal percentageRate) {
        this.percentageRate = percentageRate;
    }

    public Ledger getLedger() {
        return ledger;
    }

    public void setLedger(Ledger ledger) {
        this.ledger = ledger;
    }

    public OffsetDateTime getCreatedAt() {
        return createdAt;
    }

    public OffsetDateTime getUpdatedAt() {
        return updatedAt;
    }
}
