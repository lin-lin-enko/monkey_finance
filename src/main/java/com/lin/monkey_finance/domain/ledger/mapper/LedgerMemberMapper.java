package com.lin.monkey_finance.domain.ledger.mapper;

import com.lin.monkey_finance.domain.ledger.dto.LedgerMemberInvitationDto;
import com.lin.monkey_finance.domain.ledger.dto.LedgerMemberResponseDto;
import com.lin.monkey_finance.domain.ledger.model.Ledger;
import com.lin.monkey_finance.domain.ledger.model.LedgerMember;
import com.lin.monkey_finance.domain.user.model.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LedgerMemberMapper {

    @Mapping(target = "userId", source = "ledgerMember.id.userId")
    @Mapping(target = "invitedByUsername", source = "authorizedUser.username")
    @Mapping(target = "invitedByUserId", source = "authorizedUser.id")
    @Mapping(target = "status", source = "ledgerMember.status")
    @Mapping(target = "username", source = "ledgerMember.username")
    LedgerMemberResponseDto toResponseDto(LedgerMember ledgerMember, User authorizedUser);

    @Mapping(target = "ledgerId", source = "ledgerMember.id.ledgerId")
    @Mapping(target = "ledgerName", source = "ledger.name")
    @Mapping(target = "ledgerDescription", source = "ledger.description")
    @Mapping(target = "invitedByUsername", source = "authorizedUser.username")
    @Mapping(target = "invitedByUserId", source = "authorizedUser.id")
    LedgerMemberInvitationDto toInvitationDto(LedgerMember ledgerMember, Ledger ledger, User authorizedUser);
}
