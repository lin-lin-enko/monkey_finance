package com.lin.monkey_finance.domain.ledger.mapper;

import com.lin.monkey_finance.domain.ledger.dto.LedgerActivityLogResponseDto;
import com.lin.monkey_finance.domain.ledger.model.LedgerActivityLog;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LedgerActivityLogMapper {

    LedgerActivityLogResponseDto toResponseDto(LedgerActivityLog activityLog);
}
