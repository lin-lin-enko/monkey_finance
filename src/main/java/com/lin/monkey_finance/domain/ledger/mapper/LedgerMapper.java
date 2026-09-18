package com.lin.monkey_finance.domain.ledger.mapper;

import com.lin.monkey_finance.domain.ledger.dto.LedgerDetailedResponseDto;
import com.lin.monkey_finance.domain.ledger.dto.LedgerRequestDto;
import com.lin.monkey_finance.domain.ledger.dto.LedgerResponseDto;
import com.lin.monkey_finance.domain.ledger.dto.LedgerUpdateDto;
import com.lin.monkey_finance.domain.ledger.model.Ledger;
import com.lin.monkey_finance.domain.ledger.model.LedgerMembership;
import com.lin.monkey_finance.domain.user.model.User;
import org.mapstruct.*;

import java.util.List;
import java.util.UUID;

@Mapper(componentModel = "spring", uses = {LedgerMembershipMapper.class}, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LedgerMapper {
    LedgerResponseDto toResponseDto(Ledger ledger);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "memberships", ignore = true)
    @Mapping(target = "creatorId", source = "userId")
    Ledger toEntity(LedgerRequestDto requestDto, UUID userId);

    LedgerDetailedResponseDto toDetailedResponseDto(Ledger ledger);

    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateFromDto(LedgerUpdateDto ledgerUpdateDto, @MappingTarget Ledger ledger);
}
