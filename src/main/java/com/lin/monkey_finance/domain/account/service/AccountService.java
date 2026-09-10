package com.lin.monkey_finance.domain.account.service;

import com.lin.monkey_finance.common.exception.InsufficientPermissionsException;
import com.lin.monkey_finance.common.exception.InvalidStateException;
import com.lin.monkey_finance.common.exception.ResourceNotFoundException;
import com.lin.monkey_finance.domain.account.dto.AccountCreateDto;
import com.lin.monkey_finance.domain.account.dto.AccountResponseDto;
import com.lin.monkey_finance.domain.account.mapper.AccountMapper;
import com.lin.monkey_finance.domain.account.model.Account;
import com.lin.monkey_finance.domain.account.repository.AccountRepository;
import com.lin.monkey_finance.domain.ledger.model.AccessType;
import com.lin.monkey_finance.domain.ledger.model.LedgerMember;
import com.lin.monkey_finance.domain.ledger.model.LedgerMemberId;
import com.lin.monkey_finance.domain.ledger.model.MemberStatus;
import com.lin.monkey_finance.domain.ledger.repository.LedgerMemberRepository;
import com.lin.monkey_finance.domain.ledger.repository.LedgerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final LedgerRepository ledgerRepository;
    private final LedgerMemberRepository memberRepository;
    private final AccountMapper mapper;

    public AccountService(
            AccountRepository accountRepository,
            LedgerRepository ledgerRepository,
            LedgerMemberRepository memberRepository,
            AccountMapper mapper
    ){
        this.accountRepository = accountRepository;
        this.ledgerRepository = ledgerRepository;
        this.memberRepository = memberRepository;
        this.mapper = mapper;
    }

    @Transactional
    public AccountResponseDto create(UUID userId, UUID ledgerId, AccountCreateDto createDto){
        LedgerMember currentMember = memberRepository.findById(new LedgerMemberId(ledgerId, userId))
                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of this ledger or ledger/user don't exist "));

        if (currentMember.getAccessType() != AccessType.ADMIN && currentMember.getAccessType() != AccessType.OWNER)
            throw new InsufficientPermissionsException("Only admins and owners can create accounts for ledgers");

        if (currentMember.getStatus() != MemberStatus.ACTIVE)
            throw new InvalidStateException("Only active members can create accounts for ledgers");

        Account account = new Account(
                createDto.name(),
                createDto.type(),
                createDto.institution(),
                createDto.balance(),
                createDto.currency(),
                createDto.description(),
                currentMember.getLedger()
        );

        Account savedAccount = accountRepository.saveAndFlush(account);

        return mapper.toResponseDto(savedAccount);
    }
}
