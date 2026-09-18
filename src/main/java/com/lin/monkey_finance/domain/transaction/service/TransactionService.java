package com.lin.monkey_finance.domain.transaction.service;

import com.lin.monkey_finance.common.exception.AccountStatusException;
import com.lin.monkey_finance.common.exception.InsufficientPermissionsException;
import com.lin.monkey_finance.domain.account.dto.AccountResponseDto;
import com.lin.monkey_finance.domain.account.model.Account;
import com.lin.monkey_finance.domain.account.service.AccountService;
import com.lin.monkey_finance.domain.ledger.dto.LedgerMembershipResponseDto;
import com.lin.monkey_finance.domain.ledger.model.AccessType;
import com.lin.monkey_finance.domain.ledger.model.Ledger;
import com.lin.monkey_finance.domain.ledger.model.LedgerMembership;
import com.lin.monkey_finance.domain.ledger.model.MemberStatus;
import com.lin.monkey_finance.domain.ledger.service.LedgerMembershipService;
import com.lin.monkey_finance.domain.ledger.service.LedgerService;
import com.lin.monkey_finance.domain.savings.model.SavingsPot;
import com.lin.monkey_finance.domain.savings.service.SavingsPotService;
import com.lin.monkey_finance.domain.transaction.dto.TransactionCreateDto;
import com.lin.monkey_finance.domain.transaction.dto.TransactionResponseDto;
import com.lin.monkey_finance.domain.transaction.mapper.TransactionMapper;
import com.lin.monkey_finance.domain.transaction.model.Category;
import com.lin.monkey_finance.domain.transaction.model.Transaction;
import com.lin.monkey_finance.domain.transaction.repository.TransactionRepository;
import com.lin.monkey_finance.domain.user.dto.UserResponseDto;
import com.lin.monkey_finance.domain.user.model.User;
import com.lin.monkey_finance.domain.user.service.UserService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository repository;
    private final TransactionMapper mapper;
    private final AccountService accountService;
    private final UserService userService;
    private final CategoryService categoryService;
    private final LedgerService ledgerService;
    private final SavingsPotService savingsPotService;
    private final LedgerMembershipService membershipService;

    public TransactionService(
            TransactionRepository repository,
            TransactionMapper mapper,
            AccountService accountService,
            UserService userService,
            CategoryService categoryService,
            LedgerService ledgerService,
            SavingsPotService savingsPotService,
            LedgerMembershipService membershipService
    ){
        this.repository = repository;
        this.mapper = mapper;
        this.accountService = accountService;
        this.userService = userService;
        this.categoryService = categoryService;
        this.ledgerService = ledgerService;
        this.savingsPotService = savingsPotService;
        this.membershipService = membershipService;
    }

    @Transactional
    public TransactionResponseDto create(UUID userId, TransactionCreateDto createDto){
        User author = userService.getReferenceById(userId);
        Category category = categoryService.getReferenceById(createDto.categoryId());
        Ledger ledger = ledgerService.getReferenceById(createDto.ledgerId());
        LedgerMembershipResponseDto membershipResponseDto = membershipService.getById(createDto.ledgerId(), userId);
        if (membershipResponseDto.accessType() == AccessType.VIEWER)
            throw new InsufficientPermissionsException("Viewers can't create transactions");
        if (membershipResponseDto.status() != MemberStatus.ACTIVE)
            throw new AccountStatusException("Only active members can create transactions");

        if (createDto.accountId() != null){
            accountService.getById(userId, createDto.ledgerId(), createDto.accountId());
        }

        if (createDto.savingsPotId() != null){
            savingsPotService.getById(userId, createDto.ledgerId(), createDto.savingsPotId());
        }

        Transaction transaction = new Transaction(
                createDto.name(),
                createDto.description(),
                createDto.amount(),
                createDto.type(),
                author,
                createDto.occurredAt(),
                category,
                createDto.subcategoryId() != null ? categoryService.getReferenceById(createDto.subcategoryId()) : null,
                ledger,
                createDto.accountId() != null ? accountService.getReferenceById(createDto.accountId()) : null,
                createDto.savingsPotId() != null ? savingsPotService.getReferenceById(createDto.savingsPotId()) : null
        );

        Transaction savedTransaction = repository.saveAndFlush(transaction);

        return mapper.toResponseDto(savedTransaction);
    }
}
