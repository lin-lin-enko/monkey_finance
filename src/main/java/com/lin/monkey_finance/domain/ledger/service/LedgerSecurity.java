package com.lin.monkey_finance.domain.ledger.service;

import com.lin.monkey_finance.domain.ledger.model.AccessType;
import com.lin.monkey_finance.domain.ledger.model.LedgerMembershipId;
import com.lin.monkey_finance.domain.ledger.model.MemberStatus;
import com.lin.monkey_finance.domain.ledger.repository.LedgerMembershipRepository;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class LedgerSecurity {
    private final LedgerMembershipRepository repository;

    public LedgerSecurity(
            LedgerMembershipRepository repository
    ){
        this.repository = repository;
    }

    public boolean isActiveMember(UUID ledgerId, UUID userId){
        return repository.findById(new LedgerMembershipId(ledgerId, userId))
                .map(membership -> membership.getStatus() == MemberStatus.ACTIVE)
                .orElse(false);
    }

    public boolean isAdminOrOwner(UUID ledgerId, UUID userId){
        return repository.findById(new LedgerMembershipId(ledgerId, userId))
                .map(membership ->
                        (membership.getAccessType() == AccessType.ADMIN || membership.getAccessType() == AccessType.OWNER)
                                && membership.getStatus() == MemberStatus.ACTIVE)
                .orElse(false);
    }

    public boolean isOwner(UUID ledgerId, UUID userId){
        return repository.findById(new LedgerMembershipId(ledgerId, userId))
                .map(membership -> membership.getAccessType() == AccessType.OWNER && membership.getStatus() == MemberStatus.ACTIVE)
                .orElse(false);
    }
}
