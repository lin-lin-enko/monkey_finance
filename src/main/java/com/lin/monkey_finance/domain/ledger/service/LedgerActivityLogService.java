package com.lin.monkey_finance.domain.ledger.service;

import com.lin.monkey_finance.domain.ledger.dto.LedgerActivityLogResponseDto;
import com.lin.monkey_finance.domain.ledger.mapper.LedgerActivityLogMapper;
import com.lin.monkey_finance.domain.ledger.model.LedgerActionType;
import com.lin.monkey_finance.domain.ledger.model.LedgerActivityLog;
import com.lin.monkey_finance.domain.ledger.model.LedgerMembership;
import com.lin.monkey_finance.domain.ledger.repository.LedgerActivityLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class LedgerActivityLogService {
    private final LedgerActivityLogRepository logRepository;
    private final LedgerActivityLogMapper logMapper;
    private final LedgerMembershipService membershipService;

    public LedgerActivityLogService(
            LedgerActivityLogRepository logRepository,
            LedgerActivityLogMapper logMapper,
            LedgerMembershipService membershipService
    ){
        this.logRepository = logRepository;
        this.logMapper = logMapper;
        this.membershipService = membershipService;
    }

    @Transactional(readOnly = true)
    public List<LedgerActivityLogResponseDto> getLogsByLedger(UUID ledgerId){
        return logRepository.findAllByLedgerId(ledgerId)
                .stream()
                .map(logMapper::toResponseDto)
                .toList();
    }

    @Transactional
    public LedgerActivityLogResponseDto create(UUID ledgerId, UUID actorId, UUID targetId, LedgerActionType actionType, String description){

        LedgerMembership membershipReference = membershipService.getReferenceById(ledgerId, actorId);

        LedgerActivityLog activityLog = new LedgerActivityLog(
                membershipReference.getLedger(),
                membershipReference.getUser(),
                targetId,
                actionType,
                description
        );

        LedgerActivityLog savedActivityLog = logRepository.save(activityLog);
        return logMapper.toResponseDto(savedActivityLog);
    }
}
