package com.lin.monkey.repository;

import com.lin.monkey.model.Transaction;
import com.lin.monkey.model.TransactionType;
import jakarta.validation.constraints.Null;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID>, JpaSpecificationExecutor<Transaction> {

    @EntityGraph(attributePaths = {"subcategory", "category"})
    Page<Transaction> findAllByLedgerId(UUID ledgerId, Pageable pageable);

    @EntityGraph(attributePaths = {
            "ledger",
            "subcategory",
            "category"
    })
    @Query("""
                    SELECT t FROM Transaction t
                    JOIN FETCH t.ledger l
                    WHERE l.creatorId = :userId
            """)
    Page<Transaction> findAllByUserId(@NonNull @Param("userId") UUID userId, Pageable pageable);

    @Query("""
                    SELECT t FROM Transaction t
                    JOIN FETCH t.ledger
                    JOIN FETCH t.category
                    LEFT JOIN FETCH t.subcategory
                    WHERE t.ledger.id = :ledgerId
            """)
    List<Transaction> findAllByLedgerId(@Param("ledgerId") UUID ledgerId);

    @Query("""
                    SELECT t FROM Transaction t
                    JOIN FETCH t.ledger l
                    WHERE l.creatorId = :userId
            """)
    List<Transaction> findAllByUserId(@NonNull @Param("userId") UUID userId);

    @Query("""
                    SELECT t FROM Transaction t
                    JOIN FETCH t.ledger
                    JOIN FETCH t.category
                    LEFT JOIN FETCH t.subcategory
                    WHERE t.id = :id
            """)
    @NonNull Optional<Transaction> findById(@NonNull UUID id);

    boolean existsById(UUID id);

    List<Transaction> findAllByType(TransactionType type);

    List<Transaction> findAllByTypeAndLedgerId(TransactionType type, UUID ledgerId);

    @Query("SELECT t FROM Transaction t " +
            "LEFT JOIN FETCH t.category " +
            "LEFT JOIN FETCH t.subcategory " +
            "WHERE t.id = :id")
    Optional<Transaction> findByIdWithCategories(UUID id);

    @Query("""
                        SELECT t FROM Transaction t
                        JOIN FETCH t.ledger
                        JOIN FETCH t.category
                        LEFT JOIN FETCH t.subcategory
                        WHERE t.ledger.id = :ledgerId
                        AND (:categoryId IS NULL OR t.category.id = :categoryId)
                        AND (:subcategoryId IS NULL OR t.subcategory.id = :subcategoryId)
                        AND (:fromDate IS NULL OR t.transactionDate >= :fromDate)
                        AND (:toDate IS NULL OR t.transactionDate <= :toDate)
            
            """)
    List<Transaction> findByFilters(
            @Param("ledgerId") UUID ledgerId,
            @Param("categoryId") @Nullable UUID categoryId,
            @Param("subcategoryId") @Nullable UUID subcaregoryId,
            @Param("type") @Nullable TransactionType type,
            @Param("fromDate") @Nullable LocalDateTime fromDate,
            @Param("toDate") @Nullable LocalDateTime toDate);
}