package com.lin.monkey_finance.domain.ledger.service;

import com.lin.monkey_finance.common.exception.ResourceNotFoundException;
import com.lin.monkey_finance.domain.ledger.dto.LedgerDetailedResponseDto;
import com.lin.monkey_finance.domain.ledger.dto.LedgerMemberResponseDto;
import com.lin.monkey_finance.domain.ledger.dto.LedgerRequestDto;
import com.lin.monkey_finance.domain.ledger.dto.LedgerResponseDto;
import com.lin.monkey_finance.domain.ledger.mapper.LedgerMapper;
import com.lin.monkey_finance.domain.ledger.mapper.LedgerMemberMapper;
import com.lin.monkey_finance.domain.ledger.model.AccessType;
import com.lin.monkey_finance.domain.ledger.model.Ledger;
import com.lin.monkey_finance.domain.ledger.model.LedgerMember;
import com.lin.monkey_finance.domain.ledger.model.MemberStatus;
import com.lin.monkey_finance.domain.ledger.repository.LedgerMemberRepository;
import com.lin.monkey_finance.domain.ledger.repository.LedgerRepository;
import com.lin.monkey_finance.domain.user.model.User;
import com.lin.monkey_finance.domain.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
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
    private final EntityManager entityManager;
    private final LedgerMapper ledgerMapper;
    private final LedgerMemberMapper ledgerMemberMapper;

    public LedgerService(
            LedgerRepository ledgerRepository,
            LedgerMemberRepository ledgerMemberRepository,
            UserRepository userRepository,
            EntityManager entityManager,
            LedgerMapper ledgerMapper,
            LedgerMemberMapper ledgerMemberMapper
            ){
        this.ledgerRepository = ledgerRepository;
        this.ledgerMemberRepository = ledgerMemberRepository;
        this.userRepository = userRepository;
        this.entityManager = entityManager;
        this.ledgerMapper = ledgerMapper;
        this.ledgerMemberMapper = ledgerMemberMapper;
    }

    @Transactional
    public void createDefaultLedger(UUID creatorId){
        create(
                new LedgerRequestDto(
                        "My ledger",
                        "This is your first ledger. You can change it, set another ledger as default or make other changes, which will make its usage comfortable and personalized to you"
                ), creatorId);
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
        List<LedgerMember> ledgerMembers = ledgerMemberRepository.findAllById_LedgerId(ledgerId);
        List<LedgerMemberResponseDto> ledgerMemberResponseDtoList = ledgerMembers.stream().map(ledgerMemberMapper::toResponseDto).toList();
        return ledgerMapper.toDetailedResponseDto(ledger, ledgerMemberResponseDtoList);
    }

    @Transactional
    public LedgerDetailedResponseDto create(LedgerRequestDto ledgerRequestDto, UUID userId){
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("No user with such id"));

        List<LedgerMember> userMemberships = ledgerMemberRepository.findAllById_UserId(userId);

        Ledger ledger = new Ledger(ledgerRequestDto.name(), ledgerRequestDto.description(), userId);
        Ledger savedLedger = ledgerRepository.save(ledger);

        LedgerMember ledgerMember = new LedgerMember(savedLedger.getId(), savedLedger.getCreatorId(), user.getUsername(), userMemberships.isEmpty(), AccessType.OWNER, MemberStatus.ACTIVE);
        LedgerMember savedLedgerMember = ledgerMemberRepository.save(ledgerMember);

        entityManager.flush();
        entityManager.refresh(savedLedger);
        entityManager.refresh(savedLedgerMember);

        return ledgerMapper.toDetailedResponseDto(savedLedger, List.of(ledgerMemberMapper.toResponseDto(savedLedgerMember)));
    }
}
