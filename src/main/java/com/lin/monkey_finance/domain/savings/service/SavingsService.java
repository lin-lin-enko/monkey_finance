package com.lin.monkey_finance.domain.savings.service;

import com.lin.monkey_finance.common.exception.ResourceNotFoundException;
import com.lin.monkey_finance.domain.account.model.Account;
import com.lin.monkey_finance.domain.account.repository.AccountRepository;
import com.lin.monkey_finance.domain.ledger.model.LedgerActionType;
import com.lin.monkey_finance.domain.ledger.model.LedgerMember;
import com.lin.monkey_finance.domain.ledger.model.LedgerMemberId;
import com.lin.monkey_finance.domain.ledger.repository.LedgerMemberRepository;
import com.lin.monkey_finance.domain.ledger.service.LedgerActivityLogService;
import com.lin.monkey_finance.domain.savings.dto.SavingsCreateDto;
import com.lin.monkey_finance.domain.savings.dto.SavingsResponseDto;
import com.lin.monkey_finance.domain.savings.mapper.SavingsMapper;
import com.lin.monkey_finance.domain.savings.model.Savings;
import com.lin.monkey_finance.domain.savings.repository.SavingsRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class SavingsService {

    private final AccountRepository accountRepository;
    private final SavingsRepository savingsRepository;
    private final LedgerMemberRepository memberRepository;
    private final SavingsMapper mapper;
    private final LedgerActivityLogService activityLogService;

    public SavingsService(
            AccountRepository accountRepository,
            SavingsRepository savingsRepository,
            LedgerMemberRepository memberRepository,
            SavingsMapper mapper,
            LedgerActivityLogService activityLogService
    ){
        this.accountRepository = accountRepository;
        this.savingsRepository = savingsRepository;
        this.memberRepository = memberRepository;
        this.mapper = mapper;
        this.activityLogService = activityLogService;
    }

    @Transactional(readOnly = true)
    public List<SavingsResponseDto> getAll(UUID userId, UUID accountId){
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("No account with such id"));

        if (!memberRepository.existsById(new LedgerMemberId(account.getLedger().getId(), userId)))
                throw new ResourceNotFoundException("Current user is not a member of this ledger or ledger/user don't exist");

        List<SavingsResponseDto> savings = savingsRepository.findAllByAccountId(accountId)
                .stream().map(mapper::toResponseDto).toList();
        return savings;
    }

    @Transactional
    public SavingsResponseDto create(UUID userId, UUID accountId, SavingsCreateDto createDto){
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new ResourceNotFoundException("No account with such id"));

        LedgerMember currentMember = memberRepository.findById(new LedgerMemberId(account.getLedger().getId(), userId))
                .orElseThrow(() -> new ResourceNotFoundException("Current user is not a member of this ledger or ledger/user don't exist"));

        Savings savings = new Savings(
                account,
                createDto.name(),
                createDto.description(),
                createDto.currency(),
                createDto.targetSum(),
                createDto.dueDate(),
                createDto.percentageRate()
        );

        Savings savedSavings = savingsRepository.saveAndFlush(savings);
        activityLogService.create(currentMember.getLedger(), currentMember.getUser(), savedSavings.getId(), LedgerActionType.SAVINGS_CREATED, "Savings with name " + savings.getName() + " was created");
        return mapper.toResponseDto(savedSavings);
    }
}
