package com.lin.monkey.repository;

import com.lin.monkey.model.Ledger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface LedgerRepository extends JpaRepository<Ledger, UUID> {

    Optional<Ledger> findByNameAndOwnerId(String name, UUID ownerId);

    boolean existsByNameAndOwnerId(String name, UUID ownerId);

    List<Ledger> findAllByOwnerId(UUID ownerId);

    Optional<Ledger> findByIdAndOwnerId(UUID id, UUID ownerId);

    @Query("SELECT l FROM Ledger l WHERE l.ownerId = :ownerId AND l.isDefault = true")
    Optional<Ledger> findDefaultByOwnerId(UUID ownerId);

    @Modifying
    @Query("UPDATE Ledger l SET l.isDefault = false WHERE l.ownerId = :ownerId")
    void resetDefaultFlag(@Param("ownerId") UUID ownerId);

    @Modifying
    @Query("UPDATE Ledger l SET l.isDefault = true WHERE l.id = :ledgerId AND ownerId = :ownerId")
    void setDefaultById(@Param("ledgerId") UUID ledgerId, @Param("ownerId") UUID ownerId);
}