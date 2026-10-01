package com.lin.monkey_finance.domain.account.service;

import com.lin.monkey_finance.common.exception.AccessDeniedException;
import com.lin.monkey_finance.common.exception.InsufficientPermissionsException;
import com.lin.monkey_finance.common.exception.InvalidStateException;
import com.lin.monkey_finance.common.exception.BadRequestException;
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
        this.mapper = mapper;
        this.membershipService = membershipService;
        this.eventPublisher = eventPublisher;
    }

    @Transactional(readOnly = true)
    public AccountResponseDto getById(UUID ledgerId, UUID userId, UUID accountId){
        return mapper.toResponseDto(validateAndGet(ledgerId, userId, accountId));
    }

    @Transactional(readOnly = true)
    public Account validateAndGet(UUID ledgerId, UUID userId, UUID accountId){
        checkMembership(ledgerId, userId);
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new BadRequestException("No account with such id"));
        if (!account.getLedger().getId().equals(ledgerId))
            throw new AccessDeniedException("This account doesn't belong to this ledger");
        return account;
    }

    @Transactional(readOnly = true)
    public List<AccountResponseDto> getAll(UUID ledgerId, UUID userId){
        checkMembership(ledgerId, userId);

        return accountRepository.findAllByLedgerId(ledgerId)
                .stream().map(mapper::toResponseDto).toList();
    }

    @Transactional
    public AccountResponseDto create(UUID ledgerId, UUID userId, AccountCreateDto createDto){
        checkIsAdminOrOwner(ledgerId, userId);

        LedgerMembership membershipReference = membershipService.validateAndGetById(ledgerId, userId);

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
    public AccountResponseDto edit(UUID ledgerId, UUID userId, UUID accountId, AccountEditDto editDto){
        checkIsAdminOrOwner(ledgerId, userId);
        Account account = validateAndGet(ledgerId, userId, accountId);
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
    public void delete(UUID ledgerId, UUID userId, UUID accountId){
        checkIsAdminOrOwner(ledgerId, userId);

        Account account = validateAndGet(ledgerId, userId, accountId);
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
        membershipService.validateAndGetById(ledgerId, userId);
        Account account = accountRepository.findByLedgerId(ledgerId)
                .orElseThrow(() -> new BadRequestException("This ledger doesn't have an account or it wasn't found"));

        return mapper.toResponseDto(account);
    }

    @Transactional(readOnly = true)
    public boolean existsByLedger(UUID ledgerId, UUID userId){
        membershipService.validateAndGetById(ledgerId, userId);
        return accountRepository.existsByLedgerId(ledgerId);
    }

    public LedgerMembershipResponseDto checkMembership(UUID ledgerId, UUID userId){
        LedgerMembershipResponseDto membershipResponseDto = membershipService.findById(ledgerId, userId);
        if (membershipResponseDto.status() != MemberStatus.ACTIVE)
            throw new InvalidStateException("Only active members can perform this action");
        return membershipResponseDto;
    }


    private void checkIsAdminOrOwner(UUID ledgerId, UUID userId){
        LedgerMembershipResponseDto membershipResponseDto = checkMembership(ledgerId, userId);
        if (membershipResponseDto.accessType() != AccessType.ADMIN && membershipResponseDto.accessType() != AccessType.OWNER)
            throw new InsufficientPermissionsException("Only admins and owners can perform this action");
    }
}
