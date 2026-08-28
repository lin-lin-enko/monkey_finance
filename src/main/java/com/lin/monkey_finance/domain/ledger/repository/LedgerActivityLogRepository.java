package com.lin.monkey_finance.domain.ledger.repository;

import com.lin.monkey_finance.domain.ledger.model.LedgerActivityLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LedgerActivityLogRepository extends JpaRepository<LedgerActivityLog, UUID> {

    List<LedgerActivityLog> findAllByLedgerId(UUID ledgerId);
    List<LedgerActivityLog> findAllByActorId(UUID actorId);
    List<LedgerActivityLog> findAllByTargetId(UUID targetId);
}
