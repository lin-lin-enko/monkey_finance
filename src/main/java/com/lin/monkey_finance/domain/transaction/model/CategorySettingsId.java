package com.lin.monkey_finance.domain.transaction.model;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Embeddable
public class CategorySettingsId implements Serializable {

    @Column(name = "ledger_id", nullable = false)
    private UUID ledgerId;

    @Column(name = "category_id", nullable = false)
    private UUID categoryId;

    public CategorySettingsId() {}

    public CategorySettingsId(UUID ledgerId, UUID categoryId){
        this.ledgerId = ledgerId;
        this.categoryId = categoryId;
    }

    public UUID getLedgerId() {
        return ledgerId;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        CategorySettingsId that = (CategorySettingsId) obj;
        return Objects.equals(ledgerId, that.ledgerId) && Objects.equals(categoryId, that.categoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(ledgerId, categoryId);
    }
}
