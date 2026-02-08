package com.lin.monkey.repository;

import com.lin.monkey.model.Transaction;
import com.lin.monkey.model.TransactionType;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {

    @EntityGraph(attributePaths = {"subcategory", "category"})
    Page<Transaction> findAllByLedgerId(UUID ledgerId, Pageable pageable);

    @EntityGraph(attributePaths = {
            "ledger",
            "subcategory",
            "category"
    })
    @Query("""
                    SELECT t FROM Transaction t
                    JOIN t.ledger l
                    WHERE l.ownerId = :userId
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
                    JOIN t.ledger l
                    WHERE l.ownerId = :userId
            """)
    List<Transaction> findAllByUserId(@NonNull @Param("userId") UUID userId);

    @EntityGraph(attributePaths = {
            "ledger",
            "subcategory",
            "category"
    })
    @NonNull Optional<Transaction> findById(@NonNull UUID id);

    boolean existsById(UUID id);

    List<Transaction> findAllByType(TransactionType type);

    List<Transaction> findAllByTypeAndLedgerId(TransactionType type, UUID ledgerId);

    @Query("SELECT t FROM Transaction t " +
            "LEFT JOIN FETCH t.category " +
            "LEFT JOIN FETCH t.subcategory " +
            "WHERE t.id = :id")
    Optional<Transaction> findByIdWithCategories(UUID id);
}