package com.lin.monkey_finance.domain.ledger.service;

import com.lin.monkey_finance.common.exception.ResourceNotFoundException;
import com.lin.monkey_finance.domain.ledger.dto.*;
import com.lin.monkey_finance.domain.ledger.event.LedgerActivityLogEvent;
import com.lin.monkey_finance.domain.ledger.mapper.LedgerMapper;
import com.lin.monkey_finance.domain.ledger.model.*;
import com.lin.monkey_finance.domain.ledger.repository.LedgerRepository;
import com.lin.monkey_finance.domain.user.dto.UserResponseDto;
import com.lin.monkey_finance.domain.user.service.UserService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class LedgerService {

    private final LedgerRepository ledgerRepository;
    private final UserService userService;
    private final LedgerMapper mapper;
    private final LedgerAccessValidator accessValidator;
    private final ApplicationEventPublisher eventPublisher;

    public LedgerService(
            LedgerRepository ledgerRepository,
            UserService userService,
            LedgerMapper mapper,
            LedgerAccessValidator accessValidator,
            ApplicationEventPublisher eventPublisher
            ){
        this.ledgerRepository = ledgerRepository;
        this.userService = userService;
        this.mapper = mapper;
        this.accessValidator = accessValidator;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public Ledger getReferenceById(UUID ledgerId){
        if (ledgerRepository.existsById(ledgerId))
            return ledgerRepository.getReferenceById(ledgerId);
        else throw new ResourceNotFoundException("No ledger with such id");
    }

    @Transactional
    public LedgerDetailedResponseDto create(LedgerRequestDto ledgerRequestDto, UUID userId){
        UserResponseDto userResponseDto = userService.getById(userId);
        return create(ledgerRequestDto, userResponseDto);
    }

    @Transactional
    public LedgerDetailedResponseDto create(LedgerRequestDto ledgerRequestDto, UserResponseDto userResponseDto){
        Ledger savedLedger = ledgerRepository.saveAndFlush(mapper.toEntity(ledgerRequestDto, userResponseDto.id()));
        eventPublisher.publishEvent(new LedgerActivityLogEvent(
                savedLedger.getId(), userResponseDto.id(), savedLedger.getId(), LedgerActionType.LEDGER_CREATED, "Ledger was created"
        ));
        return mapper.toDetailedResponseDto(savedLedger);
    }

    @Transactional(readOnly = true)
    public LedgerDetailedResponseDto getById(UUID ledgerId){
        Ledger ledger = ledgerRepository.findById(ledgerId)
                .orElseThrow(() -> new ResourceNotFoundException("No ledger with such id"));
        return mapper.toDetailedResponseDto(ledger);
    }

    @Transactional
    public LedgerDetailedResponseDto edit(LedgerUpdateDto ledgerUpdateDto, UUID userId, UUID ledgerId){
        Ledger ledger = getReferenceById(ledgerId);
        accessValidator.validatePermissions(ledgerId, userId);

        mapper.updateFromDto(ledgerUpdateDto, ledger);

        eventPublisher.publishEvent(new LedgerActivityLogEvent(
                ledgerId, userId, ledgerId, LedgerActionType.LEDGER_EDITED, "Ledger was edited"
        ));
        return mapper.toDetailedResponseDto(ledger);

    }

    @Transactional
    public void delete(UUID userId, UUID ledgerId){
        accessValidator.validateOwnership(ledgerId, userId);

        eventPublisher.publishEvent(new LedgerActivityLogEvent(
                ledgerId, userId, ledgerId, LedgerActionType.LEDGER_DELETED, "Ledger was deleted"
        ));

        ledgerRepository.delete(getReferenceById(ledgerId));
    }
}
