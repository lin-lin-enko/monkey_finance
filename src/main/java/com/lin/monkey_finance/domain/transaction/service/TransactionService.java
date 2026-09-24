package com.lin.monkey_finance.domain.transaction.service;

import com.lin.monkey_finance.common.exception.AccountStatusException;
import com.lin.monkey_finance.common.exception.IllegalArgumentException;
import com.lin.monkey_finance.common.exception.InsufficientPermissionsException;
import com.lin.monkey_finance.common.exception.ResourceNotFoundException;
import com.lin.monkey_finance.domain.account.dto.AccountResponseDto;
import com.lin.monkey_finance.domain.account.model.Account;
import com.lin.monkey_finance.domain.account.model.Currency;
import com.lin.monkey_finance.domain.account.service.AccountService;
import com.lin.monkey_finance.domain.ledger.dto.LedgerMembershipResponseDto;
import com.lin.monkey_finance.domain.ledger.model.AccessType;
import com.lin.monkey_finance.domain.ledger.model.Ledger;
import com.lin.monkey_finance.domain.ledger.model.MemberStatus;
import com.lin.monkey_finance.domain.ledger.service.LedgerMembershipService;
import com.lin.monkey_finance.domain.ledger.service.LedgerService;
import com.lin.monkey_finance.domain.savings.model.SavingsPot;
import com.lin.monkey_finance.domain.savings.service.SavingsPotService;
import com.lin.monkey_finance.domain.transaction.dto.TransactionCreateDto;
import com.lin.monkey_finance.domain.transaction.dto.TransactionResponseDto;
import com.lin.monkey_finance.domain.transaction.dto.TransactionUpdateDto;
import com.lin.monkey_finance.domain.transaction.mapper.TransactionMapper;
import com.lin.monkey_finance.domain.transaction.model.Category;
import com.lin.monkey_finance.domain.transaction.model.Transaction;
import com.lin.monkey_finance.domain.transaction.repository.TransactionRepository;
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
    private final CurrencyConverterService currencyConverterService;

    public TransactionService(
            TransactionRepository repository,
            TransactionMapper mapper,
            AccountService accountService,
            UserService userService,
            CategoryService categoryService,
            LedgerService ledgerService,
            SavingsPotService savingsPotService,
            LedgerMembershipService membershipService,
            CurrencyConverterService currencyConverterService
    ){
        this.repository = repository;
        this.mapper = mapper;
        this.accountService = accountService;
        this.userService = userService;
        this.categoryService = categoryService;
        this.ledgerService = ledgerService;
        this.savingsPotService = savingsPotService;
        this.membershipService = membershipService;
        this.currencyConverterService = currencyConverterService;
    }

    @Transactional
    public TransactionResponseDto create(UUID userId, TransactionCreateDto createDto){
        Transaction transaction = mapper.toEntity(createDto);
        User author = userService.getReferenceById(userId);
        transaction.setAuthor(author);
        Category category = categoryService.getReferenceById(createDto.categoryId());
        transaction.setCategory(category);
        transaction.setSubcategory(resolveSubcategory(createDto.subcategoryId(), createDto.categoryId()));
        transaction.setLedger(resolveLedger(createDto.ledgerId(), userId));
        transaction.setAccount(resolveAccount(transaction.getLedger().getId(), userId, createDto.accountId()));
        transaction.setSavingsPot(resolveSavingsPot(transaction.getLedger().getId(), userId, createDto.savingsPotId()));

        if (createDto.currency() != null)
            transaction.setCurrency(createDto.currency());
        else if (transaction.getAccount() != null)
            transaction.setCurrency(transaction.getAccount().getCurrency());
        else if (transaction.getSavingsPot() != null)
            transaction.setCurrency(transaction.getSavingsPot().getCurrency());
        else transaction.setCurrency(Currency.EUR);

        Transaction savedTransaction = repository.saveAndFlush(transaction);
        return mapper.toResponseDto(savedTransaction);
    }

    @Transactional
    public TransactionResponseDto edit(UUID userId, UUID ledgerId, UUID transactionId, TransactionUpdateDto updateDto){
        userService.getReferenceById(userId);
        membershipService.getById(ledgerId, userId);
        Transaction transaction = getTransaction(ledgerId, userId, transactionId);

        if (updateDto.categoryId() != null)
            transaction.setCategory(categoryService.getReferenceById(updateDto.categoryId()));

        if (updateDto.subcategoryId() != null)
            transaction.setSubcategory(resolveSubcategory(updateDto.subcategoryId(), transaction.getCategory().getId()));

        if (updateDto.currency() != null && updateDto.amount() == null)
            transaction.setAmount(currencyConverterService.convertSum(transaction.getAmount(), transaction.getCurrency(), updateDto.currency()));

        if (updateDto.ledgerId() != null)
            transaction.setLedger(resolveLedger(ledgerId, userId));

        mapper.updateFromDto(updateDto, transaction);
        return mapper.toResponseDto(transaction);
    }

    private Category resolveSubcategory(UUID subcategoryId, UUID categoryId){
        if (subcategoryId != null){
            Category subcategory = categoryService.getReferenceById(subcategoryId);
            if (subcategory.getParent() == null || !subcategory.getParent().getId().equals(categoryId))
                throw new IllegalArgumentException("This subcategory doesn't belong to this category");
            return subcategory;
        }
        else return null;
    }

    private Ledger resolveLedger(UUID ledgerId, UUID userId){
        LedgerMembershipResponseDto membershipResponseDto;
        if (ledgerId == null){
            membershipResponseDto = membershipService.getUserDefaultLedgerMembership(userId);
        }
        else {
            membershipResponseDto = membershipService.getById(ledgerId, userId);
        }

        if (membershipResponseDto.accessType() == AccessType.VIEWER)
            throw new InsufficientPermissionsException("Viewers can't create transactions");
        if (membershipResponseDto.status() != MemberStatus.ACTIVE)
            throw new AccountStatusException("Only active members can create transactions");

        return ledgerService.getReferenceById(membershipResponseDto.ledgerId());
    }

    private Account resolveAccount(UUID ledgerId, UUID userId, UUID accountId){
        AccountResponseDto accountResponseDto;
        if (accountId == null && accountService.existsByLedger(userId, ledgerId)){
            accountResponseDto = accountService.getLedgerAccount(ledgerId, userId);
            return accountService.getReferenceById(accountResponseDto.id());
        } else if (accountId != null) {
            accountResponseDto = accountService.getById(userId, ledgerId, accountId);
            return accountService.getReferenceById(accountResponseDto.id());
        }
        else return null;
    }

    private SavingsPot resolveSavingsPot(UUID ledgerId, UUID userId, UUID savingsPotId){
        if (savingsPotId != null){
            savingsPotService.getById(userId, ledgerId, savingsPotId);
            return savingsPotService.getReferenceById(savingsPotId);
        }
        else return null;
    }

    private Transaction getTransaction(UUID ledgerId, UUID userId, UUID transactionId){
        AccountResponseDto accountResponseDto = accountService.getLedgerAccount(ledgerId, userId);

        if (accountResponseDto != null && !accountResponseDto.ledgerId().equals(ledgerId))
            throw new AccountStatusException("This account doesn't belong to this ledger");

        Transaction transaction = repository.findById(transactionId)
                .orElseThrow(() -> new ResourceNotFoundException("No transaction with such id"));

        if (!transaction.getLedger().getId().equals(ledgerId))
            throw new ResourceNotFoundException("This transaction doesn't belong to this ledger");

        return transaction;
    }
}
