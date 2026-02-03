package com.lin.monkey.repository;

import com.lin.monkey.model.Ledger;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LedgerRepository extends JpaRepository<Ledger, UUID> {

    Optional<Ledger> findByName(String name);

    boolean existsByName(String name);

    Optional<Ledger> findByOwnerId(UUID id);

    boolean existsByOwnerId(UUID id);
}