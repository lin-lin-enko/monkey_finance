package com.lin.monkey_finance.domain.ledger.service;

import com.lin.monkey_finance.common.exception.BadRequestException;
import com.lin.monkey_finance.domain.ledger.dto.*;
import com.lin.monkey_finance.domain.ledger.event.LedgerActivityLogEvent;
import com.lin.monkey_finance.domain.ledger.mapper.LedgerMapper;
import com.lin.monkey_finance.domain.ledger.model.*;
import com.lin.monkey_finance.domain.ledger.repository.LedgerRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class LedgerService {

    private final LedgerRepository ledgerRepository;
    private final LedgerMapper mapper;
    private final ApplicationEventPublisher eventPublisher;
    private final LedgerMembershipService membershipService;

    public LedgerService(
            LedgerRepository ledgerRepository,
            LedgerMapper mapper,
            ApplicationEventPublisher eventPublisher,
            LedgerMembershipService membershipService
            ){
        this.ledgerRepository = ledgerRepository;
        this.mapper = mapper;
        this.eventPublisher = eventPublisher;
        this.membershipService = membershipService;
    }

    @Transactional(readOnly = true)
    public LedgerDetailedResponseDto getById(UUID ledgerId){
        return mapper.toDetailedResponseDto(validateAndGetById(ledgerId));
    }

    @Transactional(readOnly = true)
    public Ledger validateAndGetById(UUID ledgerId){
        return ledgerRepository.findById(ledgerId)
                .orElseThrow(() -> new BadRequestException("No ledger with such id"));
    }

    @Transactional
    public LedgerDetailedResponseDto create(UUID userId, LedgerCreateDto createDto){
        Ledger savedLedger = ledgerRepository.saveAndFlush(mapper.toEntity(createDto, userId));
        membershipService.addByRegistration(savedLedger.getId(), userId);
        eventPublisher.publishEvent(new LedgerActivityLogEvent(
                savedLedger.getId(), userId, savedLedger.getId(), LedgerActionType.LEDGER_CREATED, "Ledger was created"
        ));
        return mapper.toDetailedResponseDto(savedLedger);
    }

    @Transactional
    @PreAuthorize("@ledgerSecurity.isAdminOrOwner(#ledgerId, #userId)")
    public LedgerDetailedResponseDto edit(UUID ledgerId, UUID userId, LedgerUpdateDto updateDto){
        Ledger ledger = validateAndGetById(ledgerId);
        mapper.updateFromDto(updateDto, ledger);
        eventPublisher.publishEvent(new LedgerActivityLogEvent(
                ledgerId, userId, ledgerId, LedgerActionType.LEDGER_EDITED, "Ledger was edited"
        ));
        return mapper.toDetailedResponseDto(ledger);

    }

    @Transactional
    @PreAuthorize("@ledgerSecurity.isOwner(#ledgerId, #userId)")
    public void delete(UUID ledgerId, UUID userId){
        eventPublisher.publishEvent(new LedgerActivityLogEvent(
                ledgerId, userId, ledgerId, LedgerActionType.LEDGER_DELETED, "Ledger was deleted"
        ));
        ledgerRepository.delete(validateAndGetById(ledgerId));
    }
}
