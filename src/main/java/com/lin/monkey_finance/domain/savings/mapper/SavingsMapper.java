package com.lin.monkey_finance.domain.savings.mapper;

import com.lin.monkey_finance.domain.savings.dto.SavingsResponseDto;
import com.lin.monkey_finance.domain.savings.model.Savings;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SavingsMapper {

    @Mapping(target = "accountId", source = "savings.account.id")
    SavingsResponseDto toResponseDto(Savings savings);
}
