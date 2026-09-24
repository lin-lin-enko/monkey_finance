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
import com.lin.monkey_finance.domain.ledger.event.LedgerActivityLogEvent;
import com.lin.monkey_finance.domain.ledger.model.*;
import com.lin.monkey_finance.domain.ledger.service.LedgerMembershipService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class AccountService {

    private final AccountRepository accountRepository;
    private final AccountMapper mapper;
    private final LedgerMembershipService membershipService;
    private final ApplicationEventPublisher eventPublisher;

    public AccountService(
            AccountRepository accountRepository,
            AccountMapper mapper,
            LedgerMembershipService membershipService,
            ApplicationEventPublisher eventPublisher
    ){
        this.accountRepository = accountRepository;
        this.membershipService = membershipService;
        this.mapper = mapper;
        this.eventPublisher = eventPublisher;
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
        LedgerMembershipResponseDto membershipResponseDto = membershipService.getById(ledgerId, userId);

        if (membershipResponseDto.status() != MemberStatus.ACTIVE)
            throw new InvalidStateException("Only active members can view ledger accounts");

        return accountRepository.findAllByLedgerId(ledgerId)
                .stream().map(mapper::toResponseDto).toList();
    }

    @Transactional
    public AccountResponseDto create(UUID userId, UUID ledgerId, AccountCreateDto createDto){
        checkPermissions(ledgerId, userId);

        LedgerMembership membershipReference = membershipService.getReferenceById(ledgerId, userId);

        Account savedAccount = accountRepository.saveAndFlush(
                mapper.toEntity(createDto, membershipReference.getLedger())
        );

        eventPublisher.publishEvent(new LedgerActivityLogEvent(
                ledgerId,
                userId,
                savedAccount.getId(),
                LedgerActionType.ACCOUNT_ADDED,
                "User added a " + savedAccount.getType().toString().toLowerCase() + " account to this ledger"
        ));
        return mapper.toResponseDto(savedAccount);
    }

    @Transactional
    public AccountResponseDto edit(UUID userId, UUID ledgerId, UUID accountId, AccountEditDto editDto){
        checkPermissions(ledgerId, userId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("No account with such id"));
        if (!account.getLedger().getId().equals(ledgerId))
            throw new AccessDeniedException("This account doesn't belong to this ledger");

        mapper.updateFromDto(editDto, account);

        eventPublisher.publishEvent(new LedgerActivityLogEvent(
                ledgerId,
                userId,
                account.getId(),
                LedgerActionType.ACCOUNT_EDITED,
                "User made changes to this account"
        ));
        return mapper.toResponseDto(account);
    }

    @Transactional
    public void delete(UUID userId, UUID ledgerId, UUID accountId){

        checkPermissions(ledgerId, userId);

        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("No account with such id"));
        if (!account.getLedger().getId().equals(ledgerId))
            throw new AccessDeniedException(
                    "This account doesn't belong to this ledger");

        accountRepository.delete(account);
        eventPublisher.publishEvent(new LedgerActivityLogEvent(
                ledgerId,
                userId,
                account.getId(),
                LedgerActionType.ACCOUNT_DELETED,
                "User deleted this account"
        ));
    }

    @Transactional(readOnly = true)
    public AccountResponseDto getLedgerAccount(UUID ledgerId, UUID userId){
        System.out.println("USER ID MEMBERSHIP        ");
        System.out.print(userId.toString());
        System.out.println("LEDGER ID MEMBERSHIP        ");
        System.out.print(ledgerId.toString());

        membershipService.getReferenceById(ledgerId, userId);
        Account account = accountRepository.findByLedgerId(ledgerId)
                .orElseThrow(() -> new ResourceNotFoundException("This ledger doesn't have an account or it wasn't found"));

        return mapper.toResponseDto(account);
    }

    @Transactional(readOnly = true)
    public boolean existsByLedger(UUID userId, UUID ledgerId){
        membershipService.getReferenceById(ledgerId, userId);
        return accountRepository.existsByLedger(ledgerId);
    }


    private void checkPermissions(UUID ledgerId, UUID userId){
        LedgerMembershipResponseDto membershipResponseDto = membershipService.getById(ledgerId, userId);

        if (membershipResponseDto.accessType() != AccessType.ADMIN && membershipResponseDto.accessType() != AccessType.OWNER)
            throw new InsufficientPermissionsException("Only admins and owners can perform this action");

        if (membershipResponseDto.status() != MemberStatus.ACTIVE)
            throw new InvalidStateException("Only active members can perform this action");
    }
}
