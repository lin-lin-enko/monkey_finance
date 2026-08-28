package com.lin.monkey_finance.domain.ledger.mapper;

import com.lin.monkey_finance.domain.ledger.dto.LedgerMemberInvitationDto;
import com.lin.monkey_finance.domain.ledger.dto.LedgerMemberResponseDto;
import com.lin.monkey_finance.domain.ledger.model.Ledger;
import com.lin.monkey_finance.domain.ledger.model.LedgerMember;
import com.lin.monkey_finance.domain.user.model.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LedgerMemberMapper {

    @Mapping(target = "userId", source = "ledgerMember.id.userId")
    @Mapping(target = "status", source = "ledgerMember.status")
    @Mapping(target = "invitedByUserId", source = "ledgerMember.invitedByUser.id")
    @Mapping(target = "blockedByUserId", source = "ledgerMember.blockedByUser.id")
    @Mapping(target = "deletedByUserId", source = "ledgerMember.deletedByUser.id")
    LedgerMemberResponseDto toResponseDto(LedgerMember ledgerMember);

    @Mapping(target = "ledgerId", source = "ledgerMember.id.ledgerId")
    @Mapping(target = "ledgerName", source = "ledger.name")
    @Mapping(target = "ledgerDescription", source = "ledger.description")
    @Mapping(target = "invitedByUserId", source = "ledgerMember.invitedByUser.id")
    LedgerMemberInvitationDto toInvitationDto(LedgerMember ledgerMember, Ledger ledger);
}
