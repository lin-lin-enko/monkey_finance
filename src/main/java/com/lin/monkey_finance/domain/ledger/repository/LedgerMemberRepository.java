package com.lin.monkey_finance.domain.ledger.repository;

import com.lin.monkey_finance.domain.ledger.model.LedgerMember;
import com.lin.monkey_finance.domain.ledger.model.LedgerMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LedgerMemberRepository extends JpaRepository<LedgerMember, LedgerMemberId> {

    @Query("SELECT lm FROM LedgerMember lm WHERE lm.id.userId = :userId AND lm.isDefaultLedger = true")
    Optional<LedgerMember> findDefaultLedgerByUserId(@Param("userId") UUID userId);

    List<LedgerMember> findAllById_LedgerId(UUID ledgerId);
    List<LedgerMember> findAllById_UserId(UUID userId);
}
