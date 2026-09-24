package com.lin.monkey_finance.domain.ledger.service;

import com.lin.monkey_finance.domain.ledger.dto.LedgerActivityLogResponseDto;
import com.lin.monkey_finance.domain.ledger.event.LedgerActivityLogEvent;
import com.lin.monkey_finance.domain.ledger.mapper.LedgerActivityLogMapper;
import com.lin.monkey_finance.domain.ledger.model.Ledger;
import com.lin.monkey_finance.domain.ledger.model.LedgerActivityLog;
import com.lin.monkey_finance.domain.ledger.repository.LedgerActivityLogRepository;
import com.lin.monkey_finance.domain.user.model.User;
import jakarta.persistence.EntityManager;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class LedgerActivityLogService {
    private final LedgerActivityLogRepository logRepository;
    private final LedgerActivityLogMapper logMapper;
    private final EntityManager entityManager;

    public LedgerActivityLogService(
            LedgerActivityLogRepository logRepository,
            LedgerActivityLogMapper logMapper,
            EntityManager entityManager
    ){
        this.logRepository = logRepository;
        this.logMapper = logMapper;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    public List<LedgerActivityLogResponseDto> getLogsByLedger(UUID ledgerId){
        return logRepository.findAllByLedgerId(ledgerId)
                .stream()
                .map(logMapper::toResponseDto)
                .toList();
    }

    @EventListener
    @Transactional
    public void create(LedgerActivityLogEvent activityLogEvent){

        Ledger ledger = entityManager.getReference(Ledger.class, activityLogEvent.ledgerId());
        User actor = entityManager.getReference(User.class, activityLogEvent.actorId());
        LedgerActivityLog activityLog = new LedgerActivityLog(
                ledger,
                actor,
                activityLogEvent.targetId(),
                activityLogEvent.actionType(),
                activityLogEvent.description()
        );

        logRepository.save(activityLog);
    }
}
