package com.lin.monkey_finance.domain.savings.mapper;

import com.lin.monkey_finance.domain.savings.dto.SavingsPotResponseDto;
import com.lin.monkey_finance.domain.savings.model.SavingsPot;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SavingsPotMapper {

    @Mapping(target = "accountId", source = "savingsPot.account.id")
    SavingsPotResponseDto toResponseDto(SavingsPot savingsPot);
}
