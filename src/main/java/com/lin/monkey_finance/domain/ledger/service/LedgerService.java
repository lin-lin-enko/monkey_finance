package com.lin.monkey_finance.domain.ledger.service;

import com.lin.monkey_finance.common.exception.AccessDeniedException;
import com.lin.monkey_finance.common.exception.ResourceNotFoundException;
import com.lin.monkey_finance.domain.ledger.dto.*;
import com.lin.monkey_finance.domain.ledger.mapper.LedgerMapper;
import com.lin.monkey_finance.domain.ledger.model.*;
import com.lin.monkey_finance.domain.ledger.repository.LedgerRepository;
import com.lin.monkey_finance.domain.user.dto.UserResponseDto;
import com.lin.monkey_finance.domain.user.service.UserService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class LedgerService {

    private final LedgerRepository ledgerRepository;
    private final LedgerMembershipService membershipService;
    private final UserService userService;
    private final LedgerMapper mapper;
    private final LedgerActivityLogService activityLogService;

    public LedgerService(
            LedgerRepository ledgerRepository,
            LedgerMembershipService membershipService,
            UserService userService,
            LedgerMapper mapper,
            LedgerActivityLogService activityLogService
            ){
        this.ledgerRepository = ledgerRepository;
        this.membershipService = membershipService;
        this.userService = userService;
        this.mapper = mapper;
        this.activityLogService = activityLogService;
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
        LedgerMembershipResponseDto membershipResponseDto = membershipService.addByRegistration(savedLedger.getId(), userResponseDto.id());
        LedgerMembership membership = membershipService.getReferenceById(membershipResponseDto.ledgerId(), membershipResponseDto.userId());
        savedLedger.addMember(membership);
        activityLogService.create(savedLedger.getId(), userResponseDto.id(), savedLedger.getId(), LedgerActionType.LEDGER_CREATED, "Ledger was created");
        return mapper.toDetailedResponseDto(savedLedger);
    }

    @Transactional(readOnly = true)
    public List<LedgerResponseDto> getCurrentUserLedgers(UUID userId){
        List<Ledger> ledgers = ledgerRepository.findAllByUserId(userId);
        List<LedgerResponseDto> ledgerResponseDtoList = new ArrayList<>();

        if (!ledgers.isEmpty()){
            ledgers.forEach(ledger ->
                    ledgerResponseDtoList.add(mapper.toResponseDto(ledger)));
        }

        return ledgerResponseDtoList;
    }

    @Transactional(readOnly = true)
    public LedgerDetailedResponseDto getById(UUID ledgerId){
        Ledger ledger = ledgerRepository.findById(ledgerId)
                .orElseThrow(() -> new ResourceNotFoundException("No ledger with such id"));
        return mapper.toDetailedResponseDto(ledger);
    }

    @Transactional
    public LedgerDetailedResponseDto edit(LedgerUpdateDto ledgerUpdateDto, UUID userId, UUID ledgerId){
        LedgerMembershipResponseDto membershipResponseDto = membershipService.getById(ledgerId, userId);
        Ledger ledger = getReferenceById(ledgerId);

        if (membershipResponseDto.accessType() != AccessType.ADMIN && membershipResponseDto.accessType() != AccessType.OWNER){
            throw new AccessDeniedException("User isn't permitted to edit this ledger");
        }

        mapper.updateFromDto(ledgerUpdateDto, ledger);

        activityLogService.create(ledgerId, userId, ledgerId, LedgerActionType.LEDGER_EDITED, "Ledger was edited");
        return mapper.toDetailedResponseDto(ledger);

    }

    @Transactional
    public void delete(UUID userId, UUID ledgerId){
        LedgerMembershipResponseDto membershipResponseDto = membershipService.getById(ledgerId, userId);
        if (membershipResponseDto.accessType() != AccessType.OWNER || membershipResponseDto.status() != MemberStatus.ACTIVE)
            throw new AccessDeniedException("This user isn't permitted to delete this ledger");

        activityLogService.create(ledgerId, userId, ledgerId, LedgerActionType.LEDGER_DELETED, "Ledger was deleted");
        ledgerRepository.delete(getReferenceById(ledgerId));
    }
}
