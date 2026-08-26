package com.lin.monkey_finance.domain.ledger.mapper;

import com.lin.monkey_finance.domain.ledger.dto.LedgerDetailedResponseDto;
import com.lin.monkey_finance.domain.ledger.dto.LedgerResponseDto;
import com.lin.monkey_finance.domain.ledger.dto.LedgerUpdateDto;
import com.lin.monkey_finance.domain.ledger.model.Ledger;
import org.mapstruct.*;

@Mapper(componentModel = "spring", uses = {LedgerMemberMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LedgerMapper {
    LedgerResponseDto toResponseDto(Ledger ledger);

    LedgerDetailedResponseDto toDetailedResponseDto(Ledger ledger);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateLedgerFromDto(LedgerUpdateDto ledgerUpdateDto, @MappingTarget Ledger ledger);
}
