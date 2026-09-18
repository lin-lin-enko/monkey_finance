package com.lin.monkey_finance.domain.account.service;

import com.lin.monkey_finance.common.exception.AccessDeniedException;
import com.lin.monkey_finance.common.exception.InsufficientPermissionsException;
import com.lin.monkey_finance.common.exception.InvalidStateException;
import com.lin.monkey_finance.common.exception.ResourceNotFoundException;
import com.lin.monkey_finance.domain.account.dto.AccountCreateDto;
import com.lin.monkey_finance.domain.account.dto.AccountEditDto;
import com.lin.monkey_finance.domain.account.dto.AccountResponseDto;
import com.lin.monkey_finance.domain.account.mapper.AccountMapper;
import com.lin.monkey_finance.domain.account.model.Account;
import com.lin.monkey_finance.domain.account.repository.AccountRepository;
import com.lin.monkey_finance.domain.ledger.dto.LedgerMembershipResponseDto;
import com.lin.monkey_finance.domain.ledger.model.*;
import com.lin.monkey_finance.domain.ledger.repository.LedgerActivityLogRepository;
import com.lin.monkey_finance.domain.ledger.repository.LedgerMembershipRepository;
import com.lin.monkey_finance.domain.ledger.repository.LedgerRepository;
import com.lin.monkey_finance.domain.ledger.service.LedgerMembershipService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final LedgerRepository ledgerRepository;
    private final LedgerMembershipRepository memberRepository;
    private final AccountMapper mapper;
    private final LedgerActivityLogRepository activityLogRepository;
    private final LedgerMembershipService membershipService;

    public AccountService(
            AccountRepository accountRepository,
            LedgerRepository ledgerRepository,
            LedgerMembershipRepository memberRepository,
            AccountMapper mapper,
            LedgerActivityLogRepository activityLogRepository,
            LedgerMembershipService membershipService
    ){
        this.accountRepository = accountRepository;
        this.ledgerRepository = ledgerRepository;
        this.memberRepository = memberRepository;
        this.mapper = mapper;
        this.activityLogRepository = activityLogRepository;
        this.membershipService = membershipService;
    }

    @Transactional(readOnly = true)
    public AccountResponseDto getById(UUID userId, UUID ledgerId, UUID accountId){
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("No account with such id"));
        if (!account.getLedger().getId().equals(ledgerId))
            throw new AccessDeniedException("This account doesn't belong to this ledger");
        membershipService.getById(ledgerId, userId);
        return mapper.toResponseDto(account);
    }

    @Transactional(readOnly = true)
    public Account getReferenceById(UUID accountId){
        if (accountRepository.existsById(accountId))
            return accountRepository.getReferenceById(accountId);
        else throw new ResourceNotFoundException("No account with such id");
    }

    @Transactional(readOnly = true)
    public List<AccountResponseDto> getAll(UUID userId, UUID ledgerId){
        LedgerMembership currentMember = memberRepository.findById(new LedgerMembershipId(ledgerId, userId))
                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of this ledger or ledger/user don't exist "));

        if (currentMember.getStatus() != MemberStatus.ACTIVE)
            throw new InvalidStateException("Only active members can view ledger accounts");

        List<AccountResponseDto> accounts = accountRepository.findAllByLedgerId(ledgerId)
                .stream().map(mapper::toResponseDto).toList();

        return accounts;
    }

    @Transactional
    public AccountResponseDto create(UUID userId, UUID ledgerId, AccountCreateDto createDto){
        LedgerMembership currentMember = memberRepository.findById(new LedgerMembershipId(ledgerId, userId))
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

        LedgerActivityLog activityLog = new LedgerActivityLog(
                currentMember.getLedger(),
                currentMember.getUser(),
                savedAccount.getId(),
                LedgerActionType.ACCOUNT_ADDED,
                "User added a " + savedAccount.getType().toString().toLowerCase() + " account to this ledger"
        );
        activityLogRepository.save(activityLog);
        return mapper.toResponseDto(savedAccount);
    }

    @Transactional
    public AccountResponseDto edit(UUID userId, UUID ledgerId, UUID accountId, AccountEditDto editDto){
        LedgerMembership currentMember = memberRepository.findById(new LedgerMembershipId(ledgerId, userId))
                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of this ledger or ledger/user don't exist "));

        if (currentMember.getAccessType() != AccessType.ADMIN && currentMember.getAccessType() != AccessType.OWNER)
            throw new InsufficientPermissionsException("Only admins and owners can edit ledger accounts");

        if (currentMember.getStatus() != MemberStatus.ACTIVE)
            throw new InvalidStateException("Only active members can edit ledger accounts");

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("No account with such id"));

        if (!account.getLedger().getId().equals(ledgerId))
            throw new ResourceNotFoundException("This account doesn't belong to this ledger");

        mapper.updateFromDto(editDto, account);

        LedgerActivityLog activityLog = new LedgerActivityLog(
                currentMember.getLedger(),
                currentMember.getUser(),
                account.getId(),
                LedgerActionType.ACCOUNT_EDITED,
                "User made changes to this account"
        );
        activityLogRepository.save(activityLog);
        return mapper.toResponseDto(account);
    }

    @Transactional
    public void delete(UUID userId, UUID ledgerId, UUID accountId){
        LedgerMembership currentMember = memberRepository.findById(new LedgerMembershipId(ledgerId, userId))
                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of this ledger or ledger/user don't exist "));

        if (currentMember.getAccessType() != AccessType.ADMIN && currentMember.getAccessType() != AccessType.OWNER)
            throw new InsufficientPermissionsException("Only admins and owners can delete ledger accounts");

        if (currentMember.getStatus() != MemberStatus.ACTIVE)
            throw new InvalidStateException("Only active members can delete ledger accounts");

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("No account with such id"));

        if (!account.getLedger().getId().equals(ledgerId))
            throw new ResourceNotFoundException("This account doesn't belong to this ledger");

        accountRepository.delete(account);
        LedgerActivityLog activityLog = new LedgerActivityLog(
                currentMember.getLedger(),
                currentMember.getUser(),
                account.getId(),
                LedgerActionType.ACCOUNT_DELETED,
                "User deleted this account"
        );
        activityLogRepository.save(activityLog);
    }

    @Transactional(readOnly = true)
    public AccountResponseDto getLedgerAccount(UUID userId, UUID ledgerId){
        memberRepository.findById(new LedgerMembershipId(ledgerId, userId))
                .orElseThrow(() -> new ResourceNotFoundException("User is not a member of this ledger or ledger/user don't exist "));
        Account account = accountRepository.findByLedgerId(ledgerId)
                .orElseThrow(() -> new ResourceNotFoundException("This ledger doesn't have an account ot it wasn't found"));

        return mapper.toResponseDto(account);
    }
}
