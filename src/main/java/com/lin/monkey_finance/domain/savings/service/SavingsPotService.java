package com.lin.monkey_finance.domain.savings.service;

import com.lin.monkey_finance.common.exception.AccountStatusException;
import com.lin.monkey_finance.common.exception.InsufficientPermissionsException;
import com.lin.monkey_finance.common.exception.ResourceNotFoundException;
import com.lin.monkey_finance.domain.account.dto.AccountResponseDto;
import com.lin.monkey_finance.domain.account.model.Currency;
import com.lin.monkey_finance.domain.account.service.AccountService;
import com.lin.monkey_finance.domain.ledger.dto.LedgerMembershipResponseDto;
import com.lin.monkey_finance.domain.ledger.event.LedgerActivityLogEvent;
import com.lin.monkey_finance.domain.ledger.model.AccessType;
import com.lin.monkey_finance.domain.ledger.model.Ledger;
import com.lin.monkey_finance.domain.ledger.model.LedgerActionType;
import com.lin.monkey_finance.domain.ledger.model.MemberStatus;
import com.lin.monkey_finance.domain.ledger.service.LedgerMembershipService;
import com.lin.monkey_finance.domain.savings.dto.SavingsPotCreateDto;
import com.lin.monkey_finance.domain.savings.dto.SavingsPotResponseDto;
import com.lin.monkey_finance.domain.savings.dto.SavingsPotUpdateDto;
import com.lin.monkey_finance.domain.savings.mapper.SavingsPotMapper;
import com.lin.monkey_finance.domain.savings.model.SavingsPot;
import com.lin.monkey_finance.domain.savings.repository.SavingsPotRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SavingsPotService {

    private final SavingsPotRepository savingsPotRepository;
    private final SavingsPotMapper mapper;
    private final LedgerMembershipService membershipService;
    private final ApplicationEventPublisher eventPublisher;
    private final AccountService accountService;

    public SavingsPotService(
            SavingsPotRepository savingsPotRepository,
            SavingsPotMapper mapper,
            LedgerMembershipService membershipService,
            ApplicationEventPublisher eventPublisher,
            AccountService accountService
    ){
        this.savingsPotRepository = savingsPotRepository;
        this.mapper = mapper;
        this.membershipService = membershipService;
        this.eventPublisher = eventPublisher;
        this.accountService = accountService;
    }

    @Transactional(readOnly = true)
    public SavingsPotResponseDto getById(UUID userId, UUID ledgerId, UUID savingsPotId){
        checkMembership(ledgerId, userId);
        SavingsPot savingsPot = savingsPotRepository.findById(savingsPotId)
                .orElseThrow(() -> new ResourceNotFoundException("No savings pot with such id"));
        return mapper.toResponseDto(savingsPot);
    }

    @Transactional(readOnly = true)
    public SavingsPot getReferenceById(UUID savingsPotId){
        if (savingsPotRepository.existsById(savingsPotId))
            return savingsPotRepository.getReferenceById(savingsPotId);
        else throw new ResourceNotFoundException("No savings pot with such id");
    }

    @Transactional(readOnly = true)
    public List<SavingsPotResponseDto> getAll(UUID userId, UUID ledgerId){
        checkMembership(ledgerId, userId);
        return savingsPotRepository.findAllByLedgerId(ledgerId).stream().map(mapper::toResponseDto).toList();
    }

    @Transactional
    public SavingsPotResponseDto create(UUID userId, UUID ledgerId, SavingsPotCreateDto createDto){
        checkIsAdminOrOwner(ledgerId, userId);

        boolean accountExists = accountService.existsByLedger(userId, ledgerId);
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

        Ledger ledger = membershipService.getReferenceById(ledgerId, userId).getLedger();

        SavingsPot savingsPot = new SavingsPot(
                createDto.name(),
                createDto.description(),
                currency,
                createDto.targetAmount(),
                createDto.dueDate(),
                createDto.percentageRate(),
                ledger
        );

        SavingsPot savedSavingsPot = savingsPotRepository.saveAndFlush(savingsPot);

        eventPublisher.publishEvent(
                new LedgerActivityLogEvent(
                        ledgerId, userId, savedSavingsPot.getId(), LedgerActionType.SAVINGS_POT_CREATED, "SavingsPot with name " + savingsPot.getName() + " was created"
                )
        );
        return mapper.toResponseDto(savedSavingsPot);
    }

    @Transactional
    public SavingsPotResponseDto edit(UUID userId, UUID ledgerId, UUID savingsPotId, SavingsPotUpdateDto updateDto){
        checkIsAdminOrOwner(ledgerId, userId);

        if (!savingsPotRepository.existsById(savingsPotId))
            throw new ResourceNotFoundException("No savings pot with such id");
        SavingsPot savingsPot = savingsPotRepository.getReferenceById(savingsPotId);

        Ledger ledger = membershipService.getReferenceById(ledgerId, userId).getLedger();
        mapper.updateFromDto(savingsPot, updateDto, ledger);
        return mapper.toResponseDto(savingsPot);
    }

    @Transactional
    public void delete(UUID userId, UUID ledgerId, UUID savingsPotId){
        checkIsAdminOrOwner(ledgerId, userId);

        if (!savingsPotRepository.existsById(savingsPotId))
            throw new ResourceNotFoundException("No savings pot with such id");
        savingsPotRepository.deleteById(savingsPotId);
    }

    private LedgerMembershipResponseDto checkMembership(UUID ledgerId, UUID userId){
        LedgerMembershipResponseDto membershipResponseDto = membershipService.getById(ledgerId, userId);
        if (membershipResponseDto.status() != MemberStatus.ACTIVE)
            throw new AccountStatusException("Only active users can perform this action");
        return membershipResponseDto;
    }

    private void checkIsAdminOrOwner(UUID ledgerId, UUID userId){
        LedgerMembershipResponseDto membershipResponseDto = checkMembership(ledgerId, userId);
        if (membershipResponseDto.accessType() != AccessType.ADMIN && membershipResponseDto.accessType() != AccessType.OWNER)
            throw new InsufficientPermissionsException("Only admins or owners of this ledger can perform this action");
    }
}
