package com.lin.monkey_finance.domain.ledger.service;

import com.lin.monkey_finance.common.exception.AccessDeniedException;
import com.lin.monkey_finance.domain.ledger.dto.LedgerMembershipResponseDto;
import com.lin.monkey_finance.domain.ledger.model.AccessType;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class LedgerAccessValidator {

    private final LedgerMembershipService membershipService;

    public LedgerAccessValidator(
            LedgerMembershipService membershipService
    ){
        this.membershipService = membershipService;
    }

    public void validatePermissions(UUID ledgerId, UUID userId){
        LedgerMembershipResponseDto membershipResponseDto = membershipService.getById(ledgerId, userId);
        if (membershipResponseDto.accessType() != AccessType.ADMIN && membershipResponseDto.accessType() != AccessType.OWNER)
            throw new AccessDeniedException("User isn't permitted to perform this action");
    }

    public void validateOwnership(UUID ledgerId, UUID userId){
        LedgerMembershipResponseDto membershipResponseDto = membershipService.getById(ledgerId, userId);
        if (membershipResponseDto.accessType() != AccessType.OWNER)
            throw new AccessDeniedException("Only owners are permitted to perform this action");
    }
}
