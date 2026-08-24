package com.lin.monkey_finance.domain.ledger.mapper;

import com.lin.monkey_finance.domain.ledger.dto.LedgerMemberResponseDto;
import com.lin.monkey_finance.domain.ledger.model.LedgerMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LedgerMemberMapper {

    @Mapping(target = "userId", source = "id.userId")
    LedgerMemberResponseDto toResponseDto(LedgerMember ledgerMember);
}
