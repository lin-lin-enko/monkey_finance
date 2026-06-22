package com.lin.monkey_finance.domain.ledger.repository;

import com.lin.monkey_finance.domain.ledger.model.Ledger;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LedgerRepository extends JpaRepository<Ledger, UUID> {

    List<Ledger> findAllByCreatorId(UUID creatorId);
}
