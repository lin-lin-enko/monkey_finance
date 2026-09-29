package com.lin.monkey_finance.domain.savings.mapper;

import com.lin.monkey_finance.domain.ledger.model.Ledger;
import com.lin.monkey_finance.domain.savings.dto.SavingsPotResponseDto;
import com.lin.monkey_finance.domain.savings.dto.SavingsPotUpdateDto;
import com.lin.monkey_finance.domain.savings.model.SavingsPot;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface SavingsPotMapper {

    SavingsPotResponseDto toResponseDto(SavingsPot savingsPot);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "ledger", source = "ledger")
    @Mapping(target = "name", source = "updateDto.name")
    @Mapping(target = "description", source = "updateDto.description")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateFromDto(@MappingTarget SavingsPot savingsPot, SavingsPotUpdateDto updateDto, Ledger ledger);
}
