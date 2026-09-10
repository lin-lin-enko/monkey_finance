package com.lin.monkey_finance.domain.account.mapper;

import com.lin.monkey_finance.domain.account.dto.AccountEditDto;
import com.lin.monkey_finance.domain.account.dto.AccountResponseDto;
import com.lin.monkey_finance.domain.account.model.Account;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface AccountMapper {

    @Mapping(target = "ledgerId", source = "account.ledger.id")
    AccountResponseDto toResponseDto(Account account);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(AccountEditDto accountEditDto, @MappingTarget Account account);
}
