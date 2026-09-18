package com.lin.monkey_finance.domain.ledger.mapper;

import com.lin.monkey_finance.domain.ledger.dto.LedgerMembershipInvitationDto;
import com.lin.monkey_finance.domain.ledger.dto.LedgerMembershipResponseDto;
import com.lin.monkey_finance.domain.ledger.model.Ledger;
import com.lin.monkey_finance.domain.ledger.model.LedgerMembership;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LedgerMembershipMapper {

    @Mapping(target = "userId", source = "ledgerMembership.id.userId")
    @Mapping(target = "ledgerId", source = "ledgerMembership.id.ledgerId")
    @Mapping(target = "status", source = "ledgerMembership.status")
    @Mapping(target = "invitedByUserId", source = "ledgerMembership.invitedByUser.id")
    @Mapping(target = "blockedByUserId", source = "ledgerMembership.blockedByUser.id")
    @Mapping(target = "deletedByUserId", source = "ledgerMembership.deletedByUser.id")
    LedgerMembershipResponseDto toResponseDto(LedgerMembership ledgerMembership);

    @Mapping(target = "ledgerId", source = "ledgerMembership.id.ledgerId")
    @Mapping(target = "ledgerName", source = "ledger.name")
    @Mapping(target = "ledgerDescription", source = "ledger.description")
    @Mapping(target = "invitedByUserId", source = "ledgerMembership.invitedByUser.id")
    LedgerMembershipInvitationDto toInvitationDto(LedgerMembership ledgerMembership, Ledger ledger);
}
