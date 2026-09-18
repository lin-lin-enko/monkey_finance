package com.lin.monkey_finance.domain.ledger.service;

import com.lin.monkey_finance.common.exception.AccessDeniedException;
import com.lin.monkey_finance.common.exception.ResourceNotFoundException;
import com.lin.monkey_finance.domain.ledger.dto.*;
import com.lin.monkey_finance.domain.ledger.mapper.LedgerMapper;
import com.lin.monkey_finance.domain.ledger.model.*;
import com.lin.monkey_finance.domain.ledger.repository.LedgerMembershipRepository;
import com.lin.monkey_finance.domain.ledger.repository.LedgerRepository;
import com.lin.monkey_finance.domain.user.model.User;
import com.lin.monkey_finance.domain.user.repository.UserRepository;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class LedgerService {

    private final LedgerRepository ledgerRepository;
    private final LedgerMembershipRepository ledgerMembershipRepository;
    private final UserRepository userRepository;
    private final LedgerMapper ledgerMapper;
    private final LedgerActivityLogService activityLogService;

    public LedgerService(
            LedgerRepository ledgerRepository,
            LedgerMembershipRepository ledgerMembershipRepository,
            UserRepository userRepository,
            LedgerMapper ledgerMapper,
            LedgerActivityLogService activityLogService
            ){
        this.ledgerRepository = ledgerRepository;
        this.ledgerMembershipRepository = ledgerMembershipRepository;
        this.userRepository = userRepository;
        this.ledgerMapper = ledgerMapper;
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
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No user with such id"));
        return create(ledgerRequestDto, user);
    }

    @Transactional
    public LedgerDetailedResponseDto create(LedgerRequestDto ledgerRequestDto, User user){
        Ledger ledger = new Ledger(ledgerRequestDto.name(), ledgerRequestDto.description(), user.getId());
        boolean isDefault = ledgerMembershipRepository.findAllById_UserId(user.getId()).isEmpty();
        Ledger savedLedger = ledgerRepository.saveAndFlush(ledger);

        LedgerMembership member = new LedgerMembership(
                savedLedger,
                user,
                isDefault,
                AccessType.OWNER,
                null,
                MemberStatus.ACTIVE);

        member.acceptInvitation();
        ledgerMembershipRepository.save(member);
        savedLedger.addMember(member);
        activityLogService.create(ledger, user, savedLedger.getId(), LedgerActionType.LEDGER_CREATED, "Ledger was created");
        activityLogService.create(ledger, user, user.getId(), LedgerActionType.MEMBER_JOINED, "First ledger owner was created");
        return ledgerMapper.toDetailedResponseDto(savedLedger);
    }

    @Transactional(readOnly = true)
    public List<LedgerResponseDto> getCurrentUserLedgers(UUID userId){
        List<Ledger> ledgers = ledgerRepository.findAllByUserId(userId);
        List<LedgerResponseDto> ledgerResponseDtoList = new ArrayList<>();

        if (!ledgers.isEmpty()){
            ledgers.forEach(ledger ->
                    ledgerResponseDtoList.add(ledgerMapper.toResponseDto(ledger)));
        }

        return ledgerResponseDtoList;
    }

    @Transactional(readOnly = true)
    public LedgerDetailedResponseDto getById(UUID ledgerId){
        Ledger ledger = ledgerRepository.findById(ledgerId)
                .orElseThrow(() -> new ResourceNotFoundException("No ledger with such id"));
        return ledgerMapper.toDetailedResponseDto(ledger);
    }

    @Transactional
    public LedgerDetailedResponseDto edit(LedgerUpdateDto ledgerUpdateDto, UUID userId, UUID ledgerId){
        LedgerMembership ledgerMembership = ledgerMembershipRepository.findById(new LedgerMembershipId(ledgerId, userId))
                .orElseThrow(() -> new ResourceNotFoundException("Ledger member not found"));
        if (ledgerMembership.getAccessType() != AccessType.ADMIN && ledgerMembership.getAccessType() != AccessType.OWNER){
            throw new AccessDeniedException("User isn't permitted to edit this ledger");
        }

        ledgerMapper.updateFromDto(ledgerUpdateDto, ledgerMembership.getLedger());

        activityLogService.create(ledgerMembership.getLedger(), ledgerMembership.getUser(), ledgerId, LedgerActionType.LEDGER_EDITED, "Ledger was edited");
        return ledgerMapper.toDetailedResponseDto(ledgerMembership.getLedger());

    }

    @Transactional
    public void delete(UUID userId, UUID ledgerId){
        LedgerMembership ledgerMembership = ledgerMembershipRepository.findById(new LedgerMembershipId(ledgerId, userId))
                .orElseThrow(() -> new ResourceNotFoundException("Ledger member not found"));
        if (ledgerMembership.getAccessType() != AccessType.OWNER){
            throw new AccessDeniedException("User isn't permitted to delete this ledger");
        }

        activityLogService.create(ledgerMembership.getLedger(), ledgerMembership.getUser(), ledgerId, LedgerActionType.LEDGER_DELETED, "Ledger was deleted");
        ledgerRepository.delete(ledgerMembership.getLedger());
    }
}
