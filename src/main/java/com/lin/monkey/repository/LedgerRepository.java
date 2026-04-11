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

    Optional<Ledger> findByNameAndCreatorId(String name, UUID creatorId);

    boolean existsByNameAndCreatorId(String name, UUID creatorId);

    List<Ledger> findAllByCreatorId(UUID creatorId);

    Optional<Ledger> findByIdAndCreatorId(UUID id, UUID creatorId);

//    @Query("SELECT l FROM Ledger l WHERE l.creatorId = :creatorId AND l.isDefault = true")
//    Optional<Ledger> findDefaultByCreatorId(UUID creatorId);
//
//    @Query("SELECT l FROM Ledger l WHERE l.creatorId = :creatorId AND l.isDefault = true")
//    boolean existsDefaultByCreatorId(UUID creatorId);
//
//    @Modifying
//    @Query("UPDATE Ledger l SET l.isDefault = false WHERE l.creatorId = :creatorId")
//    void resetDefaultFlag(@Param("creatorId") UUID creatorId);
//
//    @Modifying
//    @Query("UPDATE Ledger l SET l.isDefault = true WHERE l.id = :ledgerId AND creatorId = :creatorId")
//    void setDefaultById(@Param("ledgerId") UUID ledgerId, @Param("creatorId") UUID creatorId);
}