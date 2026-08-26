package com.lin.monkey_finance.domain.ledger.service;

import com.lin.monkey_finance.common.exception.AccessDeniedException;
import com.lin.monkey_finance.common.exception.ResourceNotFoundException;
import com.lin.monkey_finance.domain.ledger.dto.*;
import com.lin.monkey_finance.domain.ledger.mapper.LedgerMapper;
import com.lin.monkey_finance.domain.ledger.model.*;
import com.lin.monkey_finance.domain.ledger.repository.LedgerMemberRepository;
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
    private final LedgerMemberRepository ledgerMemberRepository;
    private final UserRepository userRepository;
    private final LedgerMapper ledgerMapper;

    public LedgerService(
            LedgerRepository ledgerRepository,
            LedgerMemberRepository ledgerMemberRepository,
            UserRepository userRepository,
            LedgerMapper ledgerMapper
            ){
        this.ledgerRepository = ledgerRepository;
        this.ledgerMemberRepository = ledgerMemberRepository;
        this.userRepository = userRepository;
        this.ledgerMapper = ledgerMapper;
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
        boolean isDefault = ledgerMemberRepository.findAllById_UserId(user.getId()).isEmpty();
        Ledger savedLedger = ledgerRepository.save(ledger);

        LedgerMember member = new LedgerMember(
                savedLedger,
                user,
                user.getUsername(),
                isDefault,
                AccessType.OWNER,
                null,
                MemberStatus.ACTIVE);

        member.acceptInvitation();
        ledgerMemberRepository.save(member);
        savedLedger.addMember(member);
        return ledgerMapper.toDetailedResponseDto(savedLedger);
    }

    @Transactional
    public List<LedgerResponseDto> getCurrentUserLedgers(UUID userId){
        List<Ledger> ledgers = ledgerRepository.findAllByUserId(userId);
        List<LedgerResponseDto> ledgerResponseDtoList = new ArrayList<>();

        if (!ledgers.isEmpty()){
            ledgers.forEach(ledger ->
                    ledgerResponseDtoList.add(ledgerMapper.toResponseDto(ledger)));
        }

        return ledgerResponseDtoList;
    }

    @Transactional
    public LedgerDetailedResponseDto getLedgerById(UUID ledgerId){
        Ledger ledger = ledgerRepository.findById(ledgerId)
                .orElseThrow(() -> new ResourceNotFoundException("No ledger with such id"));
        return ledgerMapper.toDetailedResponseDto(ledger);
    }

    @Transactional
    public LedgerDetailedResponseDto edit(LedgerUpdateDto ledgerUpdateDto, UUID userId, UUID ledgerId){
        LedgerMember ledgerMember = ledgerMemberRepository.findById(new LedgerMemberId(ledgerId, userId))
                .orElseThrow(() -> new ResourceNotFoundException("Ledger member not found"));
        if (ledgerMember.getAccessType() != AccessType.ADMIN && ledgerMember.getAccessType() != AccessType.OWNER){
            throw new AccessDeniedException("User isn't permitted to edit this ledger");
        }

        ledgerMapper.updateLedgerFromDto(ledgerUpdateDto, ledgerMember.getLedger());

        return ledgerMapper.toDetailedResponseDto(ledgerMember.getLedger());

    }

    @Transactional
    public void delete(UUID userId, UUID ledgerId){
        LedgerMember ledgerMember = ledgerMemberRepository.findById(new LedgerMemberId(ledgerId, userId))
                .orElseThrow(() -> new ResourceNotFoundException("Ledger member not found"));
        if (ledgerMember.getAccessType() != AccessType.OWNER){
            throw new AccessDeniedException("User isn't permitted to delete this ledger");
        }

        ledgerRepository.delete(ledgerMember.getLedger());
    }
}
