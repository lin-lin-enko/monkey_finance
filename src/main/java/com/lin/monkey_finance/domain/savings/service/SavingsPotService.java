package com.lin.monkey_finance.domain.savings.service;

import com.lin.monkey_finance.common.exception.ResourceNotFoundException;
import com.lin.monkey_finance.domain.account.dto.AccountResponseDto;
import com.lin.monkey_finance.domain.account.model.Account;
import com.lin.monkey_finance.domain.account.repository.AccountRepository;
import com.lin.monkey_finance.domain.account.service.AccountService;
import com.lin.monkey_finance.domain.ledger.model.LedgerActionType;
import com.lin.monkey_finance.domain.ledger.model.LedgerMembership;
import com.lin.monkey_finance.domain.ledger.model.LedgerMembershipId;
import com.lin.monkey_finance.domain.ledger.repository.LedgerMembershipRepository;
import com.lin.monkey_finance.domain.ledger.service.LedgerActivityLogService;
import com.lin.monkey_finance.domain.ledger.service.LedgerMembershipService;
import com.lin.monkey_finance.domain.savings.dto.SavingsPotCreateDto;
import com.lin.monkey_finance.domain.savings.dto.SavingsPotResponseDto;
import com.lin.monkey_finance.domain.savings.mapper.SavingsPotMapper;
import com.lin.monkey_finance.domain.savings.model.SavingsPot;
import com.lin.monkey_finance.domain.savings.repository.SavingsPotRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SavingsPotService {

    private final AccountRepository accountRepository;
    private final SavingsPotRepository savingsPotRepository;
    private final LedgerMembershipRepository memberRepository;
    private final SavingsPotMapper mapper;
    private final LedgerActivityLogService activityLogService;
    private final AccountService accountService;
    private final LedgerMembershipService membershipService;

    public SavingsPotService(
            AccountRepository accountRepository,
            SavingsPotRepository savingsPotRepository,
            LedgerMembershipRepository memberRepository,
            SavingsPotMapper mapper,
            LedgerActivityLogService activityLogService,
            AccountService accountService,
            LedgerMembershipService membershipService
    ){
        this.accountRepository = accountRepository;
        this.savingsPotRepository = savingsPotRepository;
        this.memberRepository = memberRepository;
        this.mapper = mapper;
        this.activityLogService = activityLogService;
        this.accountService = accountService;
        this.membershipService = membershipService;
    }

    @Transactional(readOnly = true)
    public SavingsPotResponseDto getById(UUID userId, UUID ledgerId, UUID savingsPotId){
        SavingsPot savingsPot = savingsPotRepository.findById(savingsPotId)
                .orElseThrow(() -> new ResourceNotFoundException("No savings pot with such id"));
        accountService.getById(userId, ledgerId, savingsPot.getAccount().getId());
        return mapper.toResponseDto(savingsPot);
    }

    @Transactional(readOnly = true)
    public SavingsPot getReferenceById(UUID savingsPotId){
        if (savingsPotRepository.existsById(savingsPotId))
            return savingsPotRepository.getReferenceById(savingsPotId);
        else throw new ResourceNotFoundException("No savings pot with such id");
    }

    @Transactional(readOnly = true)
    public List<SavingsPotResponseDto> getAll(UUID userId, UUID accountId){
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("No account with such id"));

        if (!memberRepository.existsById(new LedgerMembershipId(account.getLedger().getId(), userId)))
                throw new ResourceNotFoundException("Current user is not a member of this ledger or ledger/user don't exist");

        List<SavingsPotResponseDto> savingsPots = savingsPotRepository.findAllByAccountId(accountId)
                .stream().map(mapper::toResponseDto).toList();
        return savingsPots;
    }

    @Transactional
    public SavingsPotResponseDto create(UUID userId, UUID accountId, SavingsPotCreateDto createDto){
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("No account with such id"));

        LedgerMembership currentMember = memberRepository.findById(new LedgerMembershipId(account.getLedger().getId(), userId))
                .orElseThrow(() -> new ResourceNotFoundException("Current user is not a member of this ledger or ledger/user don't exist"));

        SavingsPot savingsPot = new SavingsPot(
                account,
                createDto.name(),
                createDto.description(),
                createDto.currency(),
                createDto.targetAmount(),
                createDto.dueDate(),
                createDto.percentageRate()
        );

        SavingsPot savedSavingsPot = savingsPotRepository.saveAndFlush(savingsPot);
        activityLogService.create(currentMember.getLedger(), currentMember.getUser(), savedSavingsPot.getId(), LedgerActionType.SAVINGS_CREATED, "SavingsPot with name " + savingsPot.getName() + " was created");
        return mapper.toResponseDto(savedSavingsPot);
    }
}
