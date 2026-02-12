package com.lin.monkey.repository;

import com.lin.monkey.model.Transaction;
import com.lin.monkey.model.TransactionType;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.criteria.Predicate;

public class TransactionSpecifications {

    private TransactionSpecifications() {
        // private constructor to forbid instance creation
    }

    public static Specification<Transaction> byLedger(UUID ledgerId) {
        return ((root, query, cb) -> {
            if (query.getResultType() != Long.class && query.getResultType() != Long.TYPE) {
                root.fetch("ledger", JoinType.INNER);
                root.fetch("category", JoinType.INNER);
                root.fetch("subcategory", JoinType.LEFT);
            }

            return cb.equal(root.get("ledger").get("id"), ledgerId);
        });
    }

    public static Specification<Transaction> byCategory(UUID categoryId) {
        return ((root, query, cb) -> categoryId == null ? cb.conjunction() : cb.equal(root.get("category").get("id"), categoryId));
    }

    public static Specification<Transaction> bySubcategory(UUID subcategoryId) {
        return ((root, query, cb) -> subcategoryId == null ? cb.conjunction() : cb.equal(root.get("subcategory").get("id"), subcategoryId));
    }

    public static Specification<Transaction> byType(TransactionType type) {
        // if paramether is null, it won't be in "WHERE"
        return (root, query, cb) -> type == null ? cb.conjunction() : cb.equal(root.get("type"), type);
    }

    public static Specification<Transaction> byDateRange(LocalDateTime fromDate, LocalDateTime toDate) {
        return ((root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), fromDate));
            }
            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), toDate));
            }
            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        });
    }

    public static Specification<Transaction> withFilters(
            UUID ledgerId,
            UUID categoryId,
            UUID subcategoryId,
            TransactionType type,
            LocalDateTime fromDate,
            LocalDateTime toDate
    ) {
        return Specification.where(byLedger(ledgerId))
                .and(byCategory(categoryId))
                .and(bySubcategory(subcategoryId))
                .and(byType(type))
                .and(byDateRange(fromDate, toDate));
    }
}
