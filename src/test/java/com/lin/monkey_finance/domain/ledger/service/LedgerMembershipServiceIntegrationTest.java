package com.lin.monkey_finance.domain.ledger.service;

import com.lin.monkey_finance.BaseIntegrationTest;
import com.lin.monkey_finance.common.exception.BadRequestException;
import com.lin.monkey_finance.common.exception.InvalidStateException;
import com.lin.monkey_finance.domain.ledger.dto.LedgerDetailedResponseDto;
import com.lin.monkey_finance.domain.ledger.dto.LedgerMembershipInvitationDto;
import com.lin.monkey_finance.domain.ledger.dto.LedgerMembershipRequestDto;
import com.lin.monkey_finance.domain.ledger.dto.LedgerMembershipResponseDto;
import com.lin.monkey_finance.domain.ledger.model.AccessType;
import com.lin.monkey_finance.domain.ledger.model.LedgerMembershipId;
import com.lin.monkey_finance.domain.ledger.model.MemberStatus;
import com.lin.monkey_finance.domain.ledger.repository.LedgerMembershipRepository;
import com.lin.monkey_finance.domain.user.dto.UserResponseDto;
import com.lin.monkey_finance.domain.user.model.User;
import com.lin.monkey_finance.domain.user.model.UserStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
public class LedgerMembershipServiceIntegrationTest extends BaseIntegrationTest {

    //when sendNotification is called, it isn't actually called. Instead, mock intercepts the call
    @MockitoBean
    private LedgerNotificationService ledgerNotificationService;

    @Autowired
    private LedgerMembershipRepository membershipRepository;

    private LedgerMembershipResponseDto ownerMembershipDto;
    private UserResponseDto targetUserDto;

    @BeforeEach
    void setUp(){
        UserResponseDto ledgerOwnerResponseDto = super.createUser();
        LedgerDetailedResponseDto ledgerDetailedResponseDto = super.createDefaultLedger(ledgerOwnerResponseDto.id());
        ownerMembershipDto = super.createMembership(ledgerDetailedResponseDto.id(), ledgerOwnerResponseDto.id());

        String randomUUIDSubstr = UUID.randomUUID().toString().substring(0, 5);
        User targetUser = new User(
                "targetusername" + randomUUIDSubstr,
                "targetemail" + randomUUIDSubstr,
                passwordEncoder.encode("password"),
                "Target User",
                LocalDate.of(2000, 12, 31),
                UserStatus.ACTIVE
        );
        targetUserDto = userMapper.toResponseDto(userRepository.saveAndFlush(targetUser));
    }

    @Test
    @DisplayName("Should send an invitation and notify the target user")
    void testUserInvitation(){

        LedgerMembershipRequestDto membershipRequestDto = new LedgerMembershipRequestDto(
                targetUserDto.id(),
                AccessType.VIEWER
        );
        membershipService.add(ownerMembershipDto.ledgerId(), ownerMembershipDto.userId(), membershipRequestDto);
        LedgerMembershipResponseDto newMembershipResponseDto = membershipService.findById(ownerMembershipDto.ledgerId(), targetUserDto.id());

        assertEquals(MemberStatus.PENDING, newMembershipResponseDto.status(), "Unexpected member status " + newMembershipResponseDto.status() + ". Expected PENDING");
        assertEquals(AccessType.VIEWER, newMembershipResponseDto.accessType(), "Unexpected member access type " + newMembershipResponseDto.accessType() + ". Expected VIEWER");

        ArgumentCaptor<LedgerMembershipInvitationDto> captor = ArgumentCaptor.forClass(LedgerMembershipInvitationDto.class);

        // checks the calls of the notification service mock and
        // says that sendInvitationNotification method should have been called
        verify(ledgerNotificationService).sendInvitationNotification(
                // checks if the first argument was targetUserDto.id()
                eq(targetUserDto.id()),
                // catches the second argument (here invitation dto)
                captor.capture()
                );

        LedgerMembershipInvitationDto invitationDto = captor.getValue();
        assertEquals(ownerMembershipDto.ledgerId(), invitationDto.ledgerId(), "Ledger id's don't match. Expected " + ownerMembershipDto.ledgerId() + " got " + invitationDto.ledgerId());
        assertEquals(ownerMembershipDto.userId(), invitationDto.invitedByUserId(), "Owner id's don't match. Expected " + ownerMembershipDto.userId()+ " got " + invitationDto.invitedByUserId());
        assertEquals(AccessType.VIEWER, invitationDto.accessType(), "Access types don't match. Expected " + AccessType.VIEWER + " got " + invitationDto.accessType());
    }

    @Test
    @DisplayName("Should prove that user becomes active after accepting the invitation")
    void testAcceptInvitation(){
        LedgerMembershipResponseDto targetUserMembershipDto = membershipService.add(
                ownerMembershipDto.ledgerId(),
                ownerMembershipDto.userId(),
                new LedgerMembershipRequestDto(
                        targetUserDto.id(),
                        AccessType.VIEWER
                ));
        assertEquals(MemberStatus.PENDING, targetUserMembershipDto.status(), "Member status should've become PENDING, not " + targetUserMembershipDto.status());
        targetUserMembershipDto = membershipService.acceptInvitation(ownerMembershipDto.ledgerId(), targetUserDto.id());
        assertEquals(MemberStatus.ACTIVE, targetUserMembershipDto.status(), "Member status should've become ACTIVE, not " + targetUserMembershipDto.status());
        assertEquals(AccessType.VIEWER, targetUserMembershipDto.accessType(), "Unexpected member access type " + targetUserMembershipDto.accessType() + ". Should still be VIEWER");
    }

    @Test
    @DisplayName("Should test if member information is deleted after user declines the invitation")
    void testDeclineInvitation(){
        membershipService.add(
                ownerMembershipDto.ledgerId(),
                ownerMembershipDto.userId(),
                new LedgerMembershipRequestDto(
                        targetUserDto.id(),
                        AccessType.VIEWER
                ));
        membershipService.declineInvitation(ownerMembershipDto.ledgerId(), targetUserDto.id());
        assertThrows(BadRequestException.class, () -> membershipService.findById(ownerMembershipDto.ledgerId(), targetUserDto.id()));
        assertFalse(membershipRepository.existsById(new LedgerMembershipId(ownerMembershipDto.ledgerId(), targetUserDto.id())));
    }

    @Test
    @DisplayName("Should delete membership information after the invitation was revoked")
    void testRevokeInvitation(){
        membershipService.add(
                ownerMembershipDto.ledgerId(),
                ownerMembershipDto.userId(),
                new LedgerMembershipRequestDto(
                        targetUserDto.id(),
                        AccessType.VIEWER
                ));
        membershipService.revokeInvitation(ownerMembershipDto.ledgerId(), ownerMembershipDto.userId(), targetUserDto.id());
        assertThrows(BadRequestException.class, () -> membershipService.findById(ownerMembershipDto.ledgerId(), targetUserDto.id()));
        assertFalse(membershipRepository.existsById(new LedgerMembershipId(ownerMembershipDto.ledgerId(), targetUserDto.id())));
    }

    @Test
    @DisplayName("Should change member status to LEFT after leaving the ledger")
    void testLeaveLedger(){
        LedgerMembershipResponseDto targetUserMembershipDto = membershipService.add(
                ownerMembershipDto.ledgerId(),
                ownerMembershipDto.userId(),
                new LedgerMembershipRequestDto(
                        targetUserDto.id(),
                        AccessType.VIEWER
                ));
        assertEquals(MemberStatus.PENDING, targetUserMembershipDto.status(), "Expected member status PENDING. Current status is " + targetUserMembershipDto.status());
        membershipService.acceptInvitation(ownerMembershipDto.ledgerId(), targetUserDto.id());
        membershipService.leaveLedger(ownerMembershipDto.ledgerId(), targetUserDto.id());
        LedgerMembershipResponseDto updatedTargetUserMembershipDto = membershipService.findById(ownerMembershipDto.ledgerId(), targetUserDto.id());
        assertEquals(MemberStatus.LEFT, updatedTargetUserMembershipDto.status(), "Member hasn't leaving the ledger. Member status is " + updatedTargetUserMembershipDto.status());
    }

    @Test
    @DisplayName("Should change member status to BLOCKED after being blocked")
    void testBlockMember(){
        membershipService.add(
                ownerMembershipDto.ledgerId(),
                ownerMembershipDto.userId(),
                new LedgerMembershipRequestDto(
                        targetUserDto.id(),
                        AccessType.VIEWER
                ));
        membershipService.acceptInvitation(ownerMembershipDto.ledgerId(), targetUserDto.id());
        membershipService.block(ownerMembershipDto.ledgerId(), ownerMembershipDto.userId(), targetUserDto.id());
        LedgerMembershipResponseDto targetUserMembershipDto = membershipService.findById(ownerMembershipDto.ledgerId(), targetUserDto.id());
        assertEquals(MemberStatus.BLOCKED, targetUserMembershipDto.status(), "Member wasn't blocked from the ledger. Member status is " + targetUserMembershipDto.status());
    }

    @Test
    @DisplayName("Should change member status back to ACTIVE after being unblocked")
    void testUnblockMember(){
        membershipService.add(
                ownerMembershipDto.ledgerId(),
                ownerMembershipDto.userId(),
                new LedgerMembershipRequestDto(
                        targetUserDto.id(),
                        AccessType.VIEWER
                ));
        membershipService.acceptInvitation(ownerMembershipDto.ledgerId(), targetUserDto.id());
        membershipService.block(ownerMembershipDto.ledgerId(), ownerMembershipDto.userId(), targetUserDto.id());
        membershipService.unblock(ownerMembershipDto.ledgerId(), ownerMembershipDto.userId(), targetUserDto.id());
        LedgerMembershipResponseDto targetUserMembershipDto = membershipService.findById(ownerMembershipDto.ledgerId(), targetUserDto.id());
        assertEquals(MemberStatus.ACTIVE, targetUserMembershipDto.status(), "Member wasn't unblocked in the ledger. Member status is " + targetUserMembershipDto.status());

    }

    @Test
    @DisplayName("Should successfully change member's access to different access modes")
    void testChangeAccess(){
        membershipService.add(
                ownerMembershipDto.ledgerId(),
                ownerMembershipDto.userId(),
                new LedgerMembershipRequestDto(
                        targetUserDto.id(),
                        AccessType.VIEWER
                ));
        membershipService.acceptInvitation(ownerMembershipDto.ledgerId(), targetUserDto.id());

        LedgerMembershipResponseDto targetUserMembershipDto = membershipService.changeAccess(ownerMembershipDto.ledgerId(), ownerMembershipDto.userId(), targetUserDto.id(), AccessType.MEMBER);
        assertEquals(AccessType.MEMBER, targetUserMembershipDto.accessType(), "Unexpected member access type " + targetUserMembershipDto.accessType() + ". Should be MEMBER");

        targetUserMembershipDto = membershipService.changeAccess(ownerMembershipDto.ledgerId(), ownerMembershipDto.userId(), targetUserDto.id(), AccessType.ADMIN);
        assertEquals(AccessType.ADMIN, targetUserMembershipDto.accessType(), "Unexpected member access type " + targetUserMembershipDto.accessType() + ". Should be ADMIN");

        assertThrows(
                InvalidStateException.class,
                () -> membershipService.changeAccess(ownerMembershipDto.ledgerId(), ownerMembershipDto.userId(), targetUserDto.id(), AccessType.OWNER),
                "Ownership should be reassigned by an ownership transfer"
        );
    }

    @Test
    @DisplayName("Should transfer an ownership from one member to another")
    void testTransferOwnership(){
        membershipService.add(
                ownerMembershipDto.ledgerId(),
                ownerMembershipDto.userId(),
                new LedgerMembershipRequestDto(
                        targetUserDto.id(),
                        AccessType.VIEWER
                ));
        membershipService.acceptInvitation(ownerMembershipDto.ledgerId(), targetUserDto.id());
        LedgerMembershipResponseDto targetUserMembershipDto = membershipService.transferOwnership(ownerMembershipDto.ledgerId(), ownerMembershipDto.userId(), targetUserDto.id());
        assertEquals(AccessType.OWNER, targetUserMembershipDto.accessType(), "Unexpected member access type " + targetUserMembershipDto.accessType() + ". Should be OWNER");
        LedgerMembershipResponseDto updatedOwnerMembershipDto = membershipService.findById(ownerMembershipDto.ledgerId(), ownerMembershipDto.userId());
        assertEquals(AccessType.ADMIN, updatedOwnerMembershipDto.accessType(), "Unexpected member access type " + updatedOwnerMembershipDto.accessType() + ". Previous owner should now be an ADMIN");
    }
}
