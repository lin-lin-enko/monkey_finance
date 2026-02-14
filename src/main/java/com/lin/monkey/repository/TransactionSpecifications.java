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
        // "root" means "Transaction" entity
        // "query" - meta-info (orderBy, limit etc.)
        // cb - factory for conditions (IS NULL, >, etc.)
        return ((root, query, cb) -> {
            // if it's not a count-request...
            if (query.getResultType() != Long.class && query.getResultType() != Long.TYPE) {
                root.fetch("ledger", JoinType.INNER);
                root.fetch("category", JoinType.INNER);
                root.fetch("subcategory", JoinType.LEFT);
            }
            // condition: check if ledger_id from Transaction equals ledgerId that we got as an arg
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
            // collecting all conditions into a list
            List<Predicate> predicates = new ArrayList<>();
            // if there's a fromDate, adding to the list if transaction_date
            // is greater than or equal fromDate that we got from the args
            if (fromDate != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("transactionDate"), fromDate));
            }
            // same for toDate
            if (toDate != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("transactionDate"), toDate));
            }
            // skip this filter if the list is empty
            // which means no date filtering was provided
            if (predicates.isEmpty()) {
                return cb.conjunction();
            }
            // combining all conditions through "AND"
            return cb.and(predicates.toArray(new Predicate[0]));
        });
    }

    // combining all filters together
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
