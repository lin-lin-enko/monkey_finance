package com.lin.monkey_finance.domain.transaction.service;

import com.lin.monkey_finance.common.exception.BadRequestException;
import com.lin.monkey_finance.domain.account.dto.AccountResponseDto;
import com.lin.monkey_finance.domain.account.dto.CategoryValidationObject;
import com.lin.monkey_finance.domain.account.model.Account;
import com.lin.monkey_finance.domain.account.model.Currency;
import com.lin.monkey_finance.domain.account.service.AccountService;
import com.lin.monkey_finance.domain.ledger.service.LedgerMembershipService;
import com.lin.monkey_finance.domain.savings.model.SavingsPot;
import com.lin.monkey_finance.domain.savings.service.SavingsPotService;
import com.lin.monkey_finance.domain.transaction.dto.TransactionCreateDto;
import com.lin.monkey_finance.domain.transaction.dto.TransactionResponseDto;
import com.lin.monkey_finance.domain.transaction.dto.TransactionUpdateDto;
import com.lin.monkey_finance.domain.transaction.mapper.TransactionMapper;
import com.lin.monkey_finance.domain.transaction.model.Transaction;
import com.lin.monkey_finance.domain.transaction.repository.TransactionRepository;
import jakarta.persistence.EntityManager;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository repository;
    private final TransactionMapper mapper;
    private final AccountService accountService;
    private final CategoryService categoryService;
    private final SavingsPotService savingsPotService;
    private final LedgerMembershipService membershipService;
    private final CurrencyConverterService currencyConverterService;
    private final EntityManager entityManager;

    public TransactionService(
            TransactionRepository repository,
            TransactionMapper mapper,
            AccountService accountService,
            CategoryService categoryService,
            SavingsPotService savingsPotService,
            LedgerMembershipService membershipService,
            CurrencyConverterService currencyConverterService,
            EntityManager entityManager
    ){
        this.repository = repository;
        this.mapper = mapper;
        this.accountService = accountService;
        this.categoryService = categoryService;
        this.savingsPotService = savingsPotService;
        this.membershipService = membershipService;
        this.currencyConverterService = currencyConverterService;
        this.entityManager = entityManager;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@ledgerSecurity.isActiveMember(#ledgerId, #userId)")
    public TransactionResponseDto findById(UUID ledgerId, UUID userId, UUID transactionId){
        return mapper.toResponseDto(validateAndGet(ledgerId, transactionId));
    }

    @Transactional(readOnly = true)
    public Transaction validateAndGet(UUID ledgerId, UUID transactionId){
        Transaction transaction = repository.findById(transactionId)
                .orElseThrow(() -> new BadRequestException("No transaction with such id"));

        if (!transaction.getLedger().getId().equals(ledgerId))
            throw new BadRequestException("This transaction doesn't belong to this ledger");

        return transaction;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@ledgerSecurity.isActiveMember(#ledgerId, #userId)")
    public List<TransactionResponseDto> getAll(UUID ledgerId, UUID userId){
        return repository.findAllByLedgerId(ledgerId).stream().map(mapper::toResponseDto).toList();
    }

    @Transactional
    @PreAuthorize("@ledgerSecurity.isActiveMember(#ledgerId, #userId)")
    public TransactionResponseDto create(UUID ledgerId, UUID userId, TransactionCreateDto createDto){
        Transaction transaction = mapper.toEntity(createDto);
        transaction.setAuthor(membershipService.validateAndGetById(ledgerId, userId).getUser());

        CategoryValidationObject categoryValidationObject = categoryService.validateAndGet(
                ledgerId,
                createDto.categoryId(),
                createDto.subcategoryId());

        transaction.setCategory(categoryValidationObject.category());
        transaction.setSubcategory(categoryValidationObject.subcategory());
        transaction.setLedger(membershipService.validateAndGetById(ledgerId, userId).getLedger());
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
    @PreAuthorize("@ledgerSecurity.isActiveMember(#ledgerId, #userId)")
    public TransactionResponseDto edit(UUID ledgerId, UUID userId, UUID transactionId, TransactionUpdateDto updateDto){
        Transaction transaction = validateAndGet(ledgerId, transactionId);

        if (accountService.existsByLedger(ledgerId, userId)){
            AccountResponseDto accountResponseDto = accountService.getLedgerAccount(ledgerId, userId);
            if (transaction.getAccount() == null || !transaction.getAccount().getId().equals(accountResponseDto.id()))
                throw new BadRequestException("Ledger account and transaction account don't match");
        }

        if (updateDto.categoryId() != null){
            CategoryValidationObject categoryValidationObject;
            if (updateDto.subcategoryId() != null){
                categoryValidationObject = categoryService.validateAndGet(ledgerId, updateDto.categoryId(), updateDto.subcategoryId());
                transaction.setSubcategory(categoryValidationObject.subcategory());
            }
            else categoryValidationObject = categoryService.validateAndGet(ledgerId, updateDto.categoryId(), null);

            transaction.setCategory(categoryValidationObject.category());
        }

        if (updateDto.currency() != null && updateDto.amount() == null)
            transaction.setAmount(currencyConverterService.convertSum(transaction.getAmount(), transaction.getCurrency(), updateDto.currency()));

        mapper.updateFromDto(updateDto, transaction);
        return mapper.toResponseDto(transaction);
    }

    @Transactional
    @PreAuthorize("@ledgerSecurity.isAdminOrOwner(#ledgerId, #userId)")
    public void delete(UUID ledgerId, UUID userId, UUID transactionId){
        Transaction transaction = validateAndGet(ledgerId, transactionId);
        repository.delete(transaction);
    }

    private Account resolveAccount(UUID ledgerId, UUID userId, UUID accountId){
        if (accountId == null && accountService.existsByLedger(ledgerId, userId)){
            AccountResponseDto accountResponseDto = accountService.getLedgerAccount(ledgerId, userId);
            return entityManager.getReference(Account.class, accountResponseDto.id());
        } else if (accountId != null) {
            return accountService.validateAndGet(ledgerId, userId, accountId);
        }
        else return null;
    }

    private SavingsPot resolveSavingsPot(UUID ledgerId, UUID userId, UUID savingsPotId){
        if (savingsPotId != null){
            return savingsPotService.validateAndGet(ledgerId, userId, savingsPotId);
        }
        else return null;
    }
}
