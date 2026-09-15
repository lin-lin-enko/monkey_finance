package com.lin.monkey_finance.domain.ledger.service;

import com.lin.monkey_finance.domain.ledger.dto.LedgerActivityLogResponseDto;
import com.lin.monkey_finance.domain.ledger.mapper.LedgerActivityLogMapper;
import com.lin.monkey_finance.domain.ledger.model.Ledger;
import com.lin.monkey_finance.domain.ledger.model.LedgerActionType;
import com.lin.monkey_finance.domain.ledger.model.LedgerActivityLog;
import com.lin.monkey_finance.domain.ledger.repository.LedgerActivityLogRepository;
import com.lin.monkey_finance.domain.ledger.repository.LedgerMemberRepository;
import com.lin.monkey_finance.domain.user.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class LedgerActivityLogService {
    private final LedgerActivityLogRepository logRepository;
    private final LedgerActivityLogMapper logMapper;
    private final LedgerMemberRepository memberRepository;

    public LedgerActivityLogService(
            LedgerActivityLogRepository logRepository,
            LedgerActivityLogMapper logMapper,
            LedgerMemberRepository memberRepository
    ){
        this.logRepository = logRepository;
        this.logMapper = logMapper;
        this.memberRepository = memberRepository;
    }

    @Transactional(readOnly = true)
    public List<LedgerActivityLogResponseDto> getLogsByLedger(UUID ledgerId){
        return logRepository.findAllByLedgerId(ledgerId)
                .stream()
                .map(logMapper::toResponseDto)
                .toList();
    }

    @Transactional
    public LedgerActivityLogResponseDto create(Ledger ledger, User actor, UUID targetId, LedgerActionType actionType, String description){

        LedgerActivityLog activityLog = new LedgerActivityLog(
                ledger,
                actor,
                targetId,
                actionType,
                description
        );

        LedgerActivityLog savedActivityLog = logRepository.save(activityLog);
        return logMapper.toResponseDto(savedActivityLog);
    }
}
