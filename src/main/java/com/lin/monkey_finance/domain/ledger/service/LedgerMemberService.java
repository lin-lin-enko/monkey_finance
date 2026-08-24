package com.lin.monkey_finance.domain.ledger.service;

import com.lin.monkey_finance.common.exception.AccessDeniedException;
import com.lin.monkey_finance.common.exception.InsufficientPermissionsException;
import com.lin.monkey_finance.common.exception.ResourceAlreadyExistsException;
import com.lin.monkey_finance.common.exception.ResourceNotFoundException;
import com.lin.monkey_finance.domain.ledger.dto.LedgerMemberRequestDto;
import com.lin.monkey_finance.domain.ledger.dto.LedgerMemberResponseDto;
import com.lin.monkey_finance.domain.ledger.mapper.LedgerMemberMapper;
import com.lin.monkey_finance.domain.ledger.model.*;
import com.lin.monkey_finance.domain.ledger.repository.LedgerMemberRepository;
import com.lin.monkey_finance.domain.ledger.repository.LedgerRepository;
import com.lin.monkey_finance.domain.user.model.User;
import com.lin.monkey_finance.domain.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class LedgerMemberService {

    private final UserRepository userRepository;
    private final LedgerRepository ledgerRepository;
    private final LedgerMemberRepository ledgerMemberRepository;
    private final LedgerMemberMapper ledgerMemberMapper;

    public LedgerMemberService(
            UserRepository userRepository,
            LedgerRepository ledgerRepository,
            LedgerMemberRepository ledgerMemberRepository,
            LedgerMemberMapper ledgerMemberMapper
    ){
        this.userRepository = userRepository;
        this.ledgerRepository = ledgerRepository;
        this.ledgerMemberRepository = ledgerMemberRepository;
        this.ledgerMemberMapper = ledgerMemberMapper;
    }

    public LedgerMemberResponseDto add(LedgerMemberRequestDto requestDto, UUID ledgerId, UUID userId){
        if (!ledgerRepository.existsById(ledgerId)){
            throw new ResourceNotFoundException("No ledger with such id");
        }
        LedgerMember userMembership = ledgerMemberRepository.findById(new LedgerMemberId(ledgerId, userId))
                .orElseThrow(() -> new AccessDeniedException("Authorized user is not a member of this ledger"));
        if (userMembership.getAccessType() != AccessType.ADMIN && userMembership.getAccessType() != AccessType.OWNER){
            throw new InsufficientPermissionsException("Authorized user doesn't have permission to add new members");
        }

        if (!userRepository.existsById(requestDto.userId())) {
            throw  new ResourceNotFoundException("No target user with such id");
        }

        if (ledgerMemberRepository.existsById(new LedgerMemberId(ledgerId, requestDto.userId()))){
            throw new ResourceAlreadyExistsException("Target user is already a member of this ledger");
        }

        User targetUser = userRepository.getReferenceById(requestDto.userId());
        Ledger ledger = ledgerRepository.getReferenceById(ledgerId);

        LedgerMember ledgerMember = new LedgerMember(ledgerId, requestDto.userId(), ledger, targetUser, requestDto.username(), false, requestDto.accessType(), MemberStatus.PENDING);
        LedgerMember savedLedgerMember = ledgerMemberRepository.save(ledgerMember);

        return ledgerMemberMapper.toResponseDto(savedLedgerMember);
    }
}
