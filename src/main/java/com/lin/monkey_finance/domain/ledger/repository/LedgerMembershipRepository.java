package com.lin.monkey_finance.domain.ledger.repository;

import com.lin.monkey_finance.domain.ledger.model.LedgerMembership;
import com.lin.monkey_finance.domain.ledger.model.LedgerMembershipId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LedgerMembershipRepository extends JpaRepository<LedgerMembership, LedgerMembershipId> {

    @Query("SELECT lm FROM LedgerMembership lm WHERE lm.id.userId = :userId AND lm.isDefaultLedger = true")
    Optional<LedgerMembership> findDefaultLedgerMembershipByUserId(@Param("userId") UUID userId);

    List<LedgerMembership> findAllById_LedgerId(UUID ledgerId);
    List<LedgerMembership> findAllById_UserId(UUID userId);
}
