package com.lin.monkey_finance.domain.ledger.service;

import com.lin.monkey_finance.domain.ledger.dto.LedgerMemberResponseDto;
import com.lin.monkey_finance.domain.ledger.dto.LedgerResponseDto;
import com.lin.monkey_finance.domain.ledger.model.AccessType;
import com.lin.monkey_finance.domain.ledger.model.Ledger;
import com.lin.monkey_finance.domain.ledger.model.LedgerMember;
import com.lin.monkey_finance.domain.ledger.model.MemberStatus;
import com.lin.monkey_finance.domain.ledger.repository.LedgerMemberRepository;
import com.lin.monkey_finance.domain.ledger.repository.LedgerRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class LedgerService {

    private final LedgerRepository ledgerRepository;
    private final LedgerMemberRepository ledgerMemberRepository;

    public LedgerService(LedgerRepository ledgerRepository, LedgerMemberRepository ledgerMemberRepository){
        this.ledgerRepository = ledgerRepository;
        this.ledgerMemberRepository = ledgerMemberRepository;
    }

    @Transactional
    public LedgerResponseDto create(String name, String description, UUID creatorId, String username, boolean isDefaultLedger){
        Ledger ledger = new Ledger(name, description, creatorId);
        Ledger savedLedger = ledgerRepository.save(ledger);

        LedgerMember ledgerMember = new LedgerMember(
                savedLedger.getId(),
                creatorId,
                isDefaultLedger,
                AccessType.OWNER,
                MemberStatus.ACTIVE);
        LedgerMember savedLedgerMember = ledgerMemberRepository.save(ledgerMember);
        LedgerMemberResponseDto ledgerMemberResponseDto = new LedgerMemberResponseDto(
                creatorId,
                username,
                savedLedgerMember.getAccessType(),
                savedLedgerMember.getStatus(),
                savedLedgerMember.getJoinedAt()
        );

        return new LedgerResponseDto(
                savedLedger.getId(),
                savedLedger.getName(),
                savedLedger.getDescription(),
                savedLedger.getCreatorId(),
                savedLedger.getCreatedAt(),
                List.of(ledgerMemberResponseDto));
    }

    @Transactional
    public void createDefaultLedger(UUID creatorId, String username){
        create("My ledger",
                "This is your first ledger. You can change it, set another ledger as default or make other changes, which will make its usage comfortable and personalized to you",
        creatorId,
        username,
        true);
    }
}
