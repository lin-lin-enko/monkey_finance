package com.lin.monkey_finance.domain.savings.service;

import com.lin.monkey_finance.common.exception.ResourceNotFoundException;
import com.lin.monkey_finance.domain.account.model.Account;
import com.lin.monkey_finance.domain.account.repository.AccountRepository;
import com.lin.monkey_finance.domain.ledger.model.LedgerActionType;
import com.lin.monkey_finance.domain.ledger.model.LedgerMember;
import com.lin.monkey_finance.domain.ledger.model.LedgerMemberId;
import com.lin.monkey_finance.domain.ledger.repository.LedgerMemberRepository;
import com.lin.monkey_finance.domain.ledger.service.LedgerActivityLogService;
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
    private final LedgerMemberRepository memberRepository;
    private final SavingsPotMapper mapper;
    private final LedgerActivityLogService activityLogService;

    public SavingsPotService(
            AccountRepository accountRepository,
            SavingsPotRepository savingsPotRepository,
            LedgerMemberRepository memberRepository,
            SavingsPotMapper mapper,
            LedgerActivityLogService activityLogService
    ){
        this.accountRepository = accountRepository;
        this.savingsPotRepository = savingsPotRepository;
        this.memberRepository = memberRepository;
        this.mapper = mapper;
        this.activityLogService = activityLogService;
    }

    @Transactional(readOnly = true)
    public List<SavingsPotResponseDto> getAll(UUID userId, UUID accountId){
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("No account with such id"));

        if (!memberRepository.existsById(new LedgerMemberId(account.getLedger().getId(), userId)))
                throw new ResourceNotFoundException("Current user is not a member of this ledger or ledger/user don't exist");

        List<SavingsPotResponseDto> savingsPots = savingsPotRepository.findAllByAccountId(accountId)
                .stream().map(mapper::toResponseDto).toList();
        return savingsPots;
    }

    @Transactional
    public SavingsPotResponseDto create(UUID userId, UUID accountId, SavingsPotCreateDto createDto){
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("No account with such id"));

        LedgerMember currentMember = memberRepository.findById(new LedgerMemberId(account.getLedger().getId(), userId))
                .orElseThrow(() -> new ResourceNotFoundException("Current user is not a member of this ledger or ledger/user don't exist"));

        SavingsPot savingsPot = new SavingsPot(
                account,
                createDto.name(),
                createDto.description(),
                createDto.currency(),
                createDto.targetSum(),
                createDto.dueDate(),
                createDto.percentageRate()
        );

        SavingsPot savedSavingsPot = savingsPotRepository.saveAndFlush(savingsPot);
        activityLogService.create(currentMember.getLedger(), currentMember.getUser(), savedSavingsPot.getId(), LedgerActionType.SAVINGS_CREATED, "SavingsPot with name " + savingsPot.getName() + " was created");
        return mapper.toResponseDto(savedSavingsPot);
    }
}
