package com.lin.monkey_finance.domain.account.mapper;

import com.lin.monkey_finance.domain.account.dto.AccountCreateDto;
import com.lin.monkey_finance.domain.account.dto.AccountEditDto;
import com.lin.monkey_finance.domain.account.dto.AccountResponseDto;
import com.lin.monkey_finance.domain.account.model.Account;
import com.lin.monkey_finance.domain.ledger.model.Ledger;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AccountMapper {

    @Mapping(target = "ledgerId", source = "account.ledger.id")
    AccountResponseDto toResponseDto(Account account);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "ledger", source = "ledger")
    Account toEntity(AccountCreateDto createDto, Ledger ledger);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "ledger", ignore = true)
    void updateFromDto(AccountEditDto accountEditDto, @MappingTarget Account account);
}
