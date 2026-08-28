package com.lin.monkey_finance.domain.ledger.service;

import com.lin.monkey_finance.domain.ledger.dto.LedgerActivityLogResponseDto;
import com.lin.monkey_finance.domain.ledger.mapper.LedgerActivityLogMapper;
import com.lin.monkey_finance.domain.ledger.repository.LedgerActivityLogRepository;
import com.lin.monkey_finance.domain.ledger.repository.LedgerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class LedgerActivityLogService {
    private final LedgerActivityLogRepository logRepository;
    private final LedgerActivityLogMapper logMapper;

    public LedgerActivityLogService(
            LedgerActivityLogRepository logRepository,
            LedgerActivityLogMapper logMapper
    ){
        this.logRepository = logRepository;
        this.logMapper = logMapper;
    }

    @Transactional(readOnly = true)
    public List<LedgerActivityLogResponseDto> getLogsByLedger(UUID ledgerId){
        return logRepository.findAllByLedgerId(ledgerId)
                .stream()
                .map(logMapper::toResponseDto)
                .toList();
    }
}
