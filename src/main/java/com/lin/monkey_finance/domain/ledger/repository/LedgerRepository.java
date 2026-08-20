package com.lin.monkey_finance.domain.ledger.repository;

import com.lin.monkey_finance.domain.ledger.model.Ledger;
import com.lin.monkey_finance.domain.ledger.service.LedgerService;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface LedgerRepository extends JpaRepository<Ledger, UUID> {

    List<Ledger> findAllByCreatorId(UUID creatorId);

    @Query("""
        SELECT l FROM Ledger l
        JOIN LedgerMember lm ON l.id = lm.id.ledgerId
        WHERE lm.id.userId = :userId
""")
    List<Ledger> findAllByUserId(@Param("userId") UUID userId);

//    @Query("""
//        SELECT l FROM Ledger l
//        JOIN LedgerMember lm ON l.id = lm.id.ledgerId
//        WHERE lm.id.ledgerId = :ledgerId
//        AND lm.id.userId = :userId
//""")
//    Optional<Ledger> findByIdAndUserId(UUID ledgerId, UUID userId);
}
