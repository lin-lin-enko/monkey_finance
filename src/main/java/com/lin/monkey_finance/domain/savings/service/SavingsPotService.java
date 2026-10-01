package com.lin.monkey_finance.domain.savings.service;

import com.lin.monkey_finance.common.exception.BadRequestException;
import com.lin.monkey_finance.domain.account.dto.AccountResponseDto;
import com.lin.monkey_finance.domain.account.model.Currency;
import com.lin.monkey_finance.domain.account.service.AccountService;
import com.lin.monkey_finance.domain.ledger.event.LedgerActivityLogEvent;
import com.lin.monkey_finance.domain.ledger.model.Ledger;
import com.lin.monkey_finance.domain.ledger.model.LedgerActionType;
import com.lin.monkey_finance.domain.ledger.service.LedgerMembershipService;
import com.lin.monkey_finance.domain.savings.dto.SavingsPotCreateDto;
import com.lin.monkey_finance.domain.savings.dto.SavingsPotResponseDto;
import com.lin.monkey_finance.domain.savings.dto.SavingsPotUpdateDto;
import com.lin.monkey_finance.domain.savings.mapper.SavingsPotMapper;
import com.lin.monkey_finance.domain.savings.model.SavingsPot;
import com.lin.monkey_finance.domain.savings.repository.SavingsPotRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SavingsPotService {

    private final SavingsPotRepository repository;
    private final SavingsPotMapper mapper;
    private final LedgerMembershipService membershipService;
    private final ApplicationEventPublisher eventPublisher;
    private final AccountService accountService;

    public SavingsPotService(
            SavingsPotRepository repository,
            SavingsPotMapper mapper,
            LedgerMembershipService membershipService,
            ApplicationEventPublisher eventPublisher,
            AccountService accountService
    ){
        this.repository = repository;
        this.mapper = mapper;
        this.membershipService = membershipService;
        this.eventPublisher = eventPublisher;
        this.accountService = accountService;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@ledgerSecurity.isActiveMember(#ledgerId, #userId)")
    public SavingsPotResponseDto getById(UUID ledgerId, UUID userId, UUID savingsPotId){
        return mapper.toResponseDto(validateAndGet(userId, ledgerId, savingsPotId));
    }

    @Transactional(readOnly = true)
    public SavingsPot validateAndGet(UUID ledgerId, UUID userId, UUID savingsPotId){
        SavingsPot savingsPot = repository.findById(savingsPotId)
                .orElseThrow(() -> new BadRequestException("No savings pot with such id"));

        if (!savingsPot.getLedger().getId().equals(ledgerId))
            throw new BadRequestException("This savings pot doesn't belong to this ledger");
        return savingsPot;
    }

    @Transactional(readOnly = true)
    @PreAuthorize("@ledgerSecurity.isActiveMember(#ledgerId, #userId)")
    public List<SavingsPotResponseDto> getAll(UUID ledgerId, UUID userId){
        return repository.findAllByLedgerId(ledgerId).stream().map(mapper::toResponseDto).toList();
    }

    @Transactional
    @PreAuthorize("@ledgerSecurity.isAdminOrOwner(#ledgerId, #userId)")
    public SavingsPotResponseDto create(UUID ledgerId, UUID userId, SavingsPotCreateDto createDto){
        boolean accountExists = accountService.existsByLedger(ledgerId, userId);
        Currency currency;

        if (createDto.currency() != null)
            currency = createDto.currency();
        else {
            if (accountExists) {
                AccountResponseDto accountResponseDto = accountService.getLedgerAccount(ledgerId, userId);
                currency = accountResponseDto.currency();
            }
            else currency = Currency.EUR;
        }

        Ledger ledger = membershipService.validateAndGetById(ledgerId, userId).getLedger();

        SavingsPot savingsPot = new SavingsPot(
                createDto.name(),
                createDto.description(),
                currency,
                createDto.targetAmount(),
                createDto.dueDate(),
                createDto.percentageRate(),
                ledger
        );

        SavingsPot savedSavingsPot = repository.saveAndFlush(savingsPot);

        eventPublisher.publishEvent(
                new LedgerActivityLogEvent(
                        ledgerId, userId, savedSavingsPot.getId(), LedgerActionType.SAVINGS_POT_CREATED, "SavingsPot with name " + savingsPot.getName() + " was created"
                )
        );
        return mapper.toResponseDto(savedSavingsPot);
    }

    @Transactional
    @PreAuthorize("@ledgerSecurity.isAdminOrOwner(#ledgerId, #userId)")
    public SavingsPotResponseDto edit(UUID ledgerId, UUID userId, UUID savingsPotId, SavingsPotUpdateDto updateDto){
        SavingsPot savingsPot = validateAndGet(ledgerId, userId, savingsPotId);
        Ledger ledger = membershipService.validateAndGetById(ledgerId, userId).getLedger();
        mapper.updateFromDto(savingsPot, updateDto, ledger);
        return mapper.toResponseDto(savingsPot);
    }

    @Transactional
    @PreAuthorize("@ledgerSecurity.isAdminOrOwner(#ledgerId, #userId)")
    public void delete(UUID ledgerId, UUID userId, UUID savingsPotId){
        validateAndGet(ledgerId, userId, savingsPotId);
        repository.deleteById(savingsPotId);
    }

}
