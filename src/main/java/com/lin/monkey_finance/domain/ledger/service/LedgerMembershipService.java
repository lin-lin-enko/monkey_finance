package com.lin.monkey_finance.domain.ledger.service;

import com.lin.monkey_finance.common.exception.AccessDeniedException;
import com.lin.monkey_finance.common.exception.InvalidStateException;
import com.lin.monkey_finance.common.exception.InsufficientPermissionsException;
import com.lin.monkey_finance.common.exception.ResourceAlreadyExistsException;
import com.lin.monkey_finance.common.exception.ResourceNotFoundException;
import com.lin.monkey_finance.domain.ledger.dto.LedgerMembershipInvitationDto;
import com.lin.monkey_finance.domain.ledger.dto.LedgerMembershipResponseDto;
import com.lin.monkey_finance.domain.ledger.dto.LedgerMembershipRequestDto;
import com.lin.monkey_finance.domain.ledger.mapper.LedgerMembershipMapper;
import com.lin.monkey_finance.domain.ledger.model.*;
import com.lin.monkey_finance.domain.ledger.repository.LedgerActivityLogRepository;
import com.lin.monkey_finance.domain.ledger.repository.LedgerMembershipRepository;
import com.lin.monkey_finance.domain.ledger.repository.LedgerRepository;
import com.lin.monkey_finance.domain.user.model.User;
import com.lin.monkey_finance.domain.user.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;
import java.util.UUID;

@Service
public class LedgerMembershipService {

    private final UserRepository userRepository;
    private final LedgerRepository ledgerRepository;
    private final LedgerMembershipRepository membershipRepository;
    private final LedgerMembershipMapper membershipMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final LedgerActivityLogRepository activityLogRepository;

    private static final Logger log = LoggerFactory.getLogger(LedgerMembershipService.class);

    public LedgerMembershipService(
            UserRepository userRepository,
            LedgerRepository ledgerRepository,
            LedgerMembershipRepository membershipRepository,
            LedgerMembershipMapper membershipMapper,
            SimpMessagingTemplate messagingTemplate,
            LedgerActivityLogRepository activityLogRepository
    ){
        this.userRepository = userRepository;
        this.ledgerRepository = ledgerRepository;
        this.membershipRepository = membershipRepository;
        this.membershipMapper = membershipMapper;
        this.messagingTemplate = messagingTemplate;
        this.activityLogRepository = activityLogRepository;
    }

    @Transactional(readOnly = true)
    public LedgerMembership getReferenceById(UUID ledgerId, UUID userId){
        if (membershipRepository.existsById(new LedgerMembershipId(ledgerId, userId)))
            return membershipRepository.getReferenceById(new LedgerMembershipId(ledgerId, userId));
        else
            throw new ResourceNotFoundException("User is not a member of this ledger or user/ledger doesn't exist");
    }

    @Transactional
    public LedgerMembershipResponseDto add(LedgerMembershipRequestDto requestDto, UUID ledgerId, UUID authorizedUserId) {
        if (!ledgerRepository.existsById(ledgerId)) {
            throw new ResourceNotFoundException("No ledger with such id");
        }
        Ledger ledger = ledgerRepository.getReferenceById(ledgerId);

        LedgerMembership authorizedMember = membershipRepository.findById(new LedgerMembershipId(ledgerId, authorizedUserId))
                .orElseThrow(() -> new AccessDeniedException("Authorized user is not a member of this ledger"));
        if (authorizedMember.getAccessType() != AccessType.ADMIN && authorizedMember.getAccessType() != AccessType.OWNER) {
            throw new InsufficientPermissionsException("Authorized user doesn't have permission to invite new members");
        }

        User authorizedUser = authorizedMember.getUser();

        Optional<LedgerMembership> targetMemberOptional = membershipRepository.findById(new LedgerMembershipId(ledgerId, requestDto.userId()));
        LedgerMembership targetMember;
        if (targetMemberOptional.isEmpty()){
            User targetUser = userRepository.findById(requestDto.userId())
                    .orElseThrow(() -> new ResourceNotFoundException("No user with such id"));
            targetMember = new LedgerMembership(
                    ledger,
                    targetUser,
                    false,
                    requestDto.accessType(),
                    authorizedUser,
                    MemberStatus.PENDING);
        }
        else{
            targetMember = targetMemberOptional.get();
            if (targetMember.getStatus() == MemberStatus.DELETED || targetMember.getStatus() == MemberStatus.LEFT) {
                targetMember.setStatus(MemberStatus.PENDING);
                targetMember.setAccessType(requestDto.accessType());
            }
            else throw new ResourceAlreadyExistsException("Target user is already a member of this ledger or was blocked");
        }

        LedgerMembership savedLedgerMembership = membershipRepository.saveAndFlush(targetMember);
        LedgerMembershipInvitationDto invitationDto = membershipMapper.toInvitationDto(savedLedgerMembership, ledger);

        final String targetUserId = targetMember.getUser().getId().toString();

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            try {
                                messagingTemplate.convertAndSendToUser(
                                        targetUserId,
                                        "/queue/invitations",
                                        invitationDto
                                );
                            } catch (Exception e) {
                                log.error("Message in afterCommit wasn't sent. {}", e.getMessage(), e);
                            }
                        }
                    }
            );
        }

        LedgerActivityLog activityLog = new LedgerActivityLog(
                ledger,
                authorizedUser,
                UUID.fromString(targetUserId),
                LedgerActionType.MEMBER_INVITED,
                null
        );
        activityLogRepository.save(activityLog);

        return membershipMapper.toResponseDto(savedLedgerMembership);
    }

    @Transactional
    public LedgerMembershipResponseDto acceptInvitation(UUID ledgerId, UUID userId){

        LedgerMembership ledgerMembership = membershipRepository.findById(new LedgerMembershipId(ledgerId, userId))
                .orElseThrow(() -> new ResourceNotFoundException("Invitation to this ledger wasn't found or the user has declined it"));

        if (ledgerMembership.getStatus() == MemberStatus.ACTIVE) throw new InvalidStateException("The user has already accepted the invitation to this ledger");

        if (ledgerMembership.getStatus() != MemberStatus.PENDING) throw new InvalidStateException("Cannot accept the invitation with the status " + ledgerMembership.getStatus());

        ledgerMembership.acceptInvitation();
        LedgerActivityLog activityLog = new LedgerActivityLog(
                ledgerMembership.getLedger(),
                ledgerMembership.getUser(),
                ledgerMembership.getId().getUserId(),
                LedgerActionType.MEMBER_JOINED,
                null
        );
        activityLogRepository.save(activityLog);
        return membershipMapper.toResponseDto(ledgerMembership);
    }

    @Transactional
    public void declineInvitation(UUID ledgerId, UUID userId){

        LedgerMembership ledgerMembership = membershipRepository.findById(new LedgerMembershipId(ledgerId, userId))
                .orElseThrow(() -> new ResourceNotFoundException("Invitation to this ledger wasn't found or the user has declined it"));

        if (ledgerMembership.getStatus() == MemberStatus.ACTIVE) throw new InvalidStateException("The user is already a member this ledger");
        if (ledgerMembership.getStatus() != MemberStatus.PENDING) throw new InvalidStateException("Cannot decline the invitation with the status " + ledgerMembership.getStatus());

        LedgerActivityLog activityLog = new LedgerActivityLog(
                ledgerMembership.getLedger(),
                ledgerMembership.getUser(),
                ledgerMembership.getId().getUserId(),
                LedgerActionType.USER_DECLINED_INVITATION,
                "The user decided to decline the invitation"
        );
        activityLogRepository.save(activityLog);
        membershipRepository.delete(ledgerMembership);
    }

    @Transactional
    public void leaveLedger(UUID ledgerId, UUID userId){
        LedgerMembership ledgerMembership = membershipRepository.findById(new LedgerMembershipId(ledgerId, userId))
                .orElseThrow(() -> new ResourceNotFoundException("The user is not a member of this ledger"));

        switch (ledgerMembership.getStatus()){
            case MemberStatus.LEFT -> throw new InvalidStateException("The user has already left the ledger");
            case MemberStatus.BLOCKED -> throw new InvalidStateException("The user was blocked from that ledger");
            case MemberStatus.PENDING -> throw new InvalidStateException("The user hasn't yet accepted the invitation to the ledger");
        }

        LedgerActivityLog activityLog = new LedgerActivityLog(
                ledgerMembership.getLedger(),
                ledgerMembership.getUser(),
                ledgerMembership.getId().getUserId(),
                LedgerActionType.MEMBER_LEFT,
                null
        );
        activityLogRepository.save(activityLog);

        ledgerMembership.leaveLedger();
    }

    @Transactional
    public LedgerMembershipResponseDto block(UUID ledgerId, UUID targetUserId, UUID authorizedUserId){
            LedgerMembership targetLedgerMembership = membershipRepository.findById(new LedgerMembershipId(ledgerId, targetUserId))
                    .orElseThrow(() -> new ResourceNotFoundException("Target user is not a member of this ledger"));

            LedgerMembership authorizedLedgerMembership = membershipRepository.findById(new LedgerMembershipId(ledgerId, authorizedUserId))
                    .orElseThrow(() -> new ResourceNotFoundException("Authorized user is not a member of this ledger or wasn't found"));


            if (authorizedLedgerMembership.getAccessType() != AccessType.OWNER && authorizedLedgerMembership.getAccessType() != AccessType.ADMIN) {
                throw new InsufficientPermissionsException("Only owners and admins can block users");
            }

            if (targetLedgerMembership.getAccessType() == AccessType.OWNER) {
                throw new InsufficientPermissionsException("An owner can't be blocked");
            }

            if (targetLedgerMembership.getStatus() == MemberStatus.BLOCKED) {
                throw new InvalidStateException("Target user is already blocked");
            }

            if (targetLedgerMembership.getStatus() != MemberStatus.ACTIVE) {
                throw new InvalidStateException("Target user is not a member of this ledger yet");
            }

            targetLedgerMembership.blockMember(authorizedLedgerMembership.getUser());

            LedgerActivityLog activityLog = new LedgerActivityLog(
                    targetLedgerMembership.getLedger(),
                    authorizedLedgerMembership.getUser(),
                    targetLedgerMembership.getUser().getId(),
                    LedgerActionType.MEMBER_BLOCKED,
                    null);
            activityLogRepository.save(activityLog);
            return membershipMapper.toResponseDto(targetLedgerMembership);
    }

    @Transactional
    public LedgerMembershipResponseDto unblock(UUID ledgerId, UUID targetUserId, UUID authorizedUserId){
        LedgerMembership targetLedgerMembership = membershipRepository.findById(new LedgerMembershipId(ledgerId, targetUserId))
                .orElseThrow(() -> new ResourceNotFoundException("Target user is not a member of this ledger"));

        LedgerMembership authorizedLedgerMembership = membershipRepository.findById(new LedgerMembershipId(ledgerId, authorizedUserId))
                .orElseThrow(() -> new ResourceNotFoundException("Authorized user is not a member of this ledger or wasn't found"));

        if (authorizedLedgerMembership.getAccessType() != AccessType.OWNER && authorizedLedgerMembership.getAccessType() != AccessType.ADMIN){
            throw new InsufficientPermissionsException("Only owners and admins can unblock users");
        }

        if (targetLedgerMembership.getStatus() != MemberStatus.BLOCKED){
            throw new InvalidStateException("This user isn't blocked");
        }

        User authorizedUser = authorizedLedgerMembership.getUser();
        targetLedgerMembership.unblockMember();
        LedgerActivityLog activityLog = new LedgerActivityLog(
                targetLedgerMembership.getLedger(),
                authorizedUser,
                targetUserId,
                LedgerActionType.MEMBER_UNBLOCKED,
                null);
        activityLogRepository.save(activityLog);

        return membershipMapper.toResponseDto(targetLedgerMembership);
    }


    @Transactional
    public LedgerMembershipResponseDto changeAccess(UUID ledgerId, UUID targetUserId, UUID authorizedUserId, AccessType targetAccessType){
        LedgerMembership targetLedgerMembership = membershipRepository.findById(new LedgerMembershipId(ledgerId, targetUserId))
                .orElseThrow(() -> new ResourceNotFoundException("Target user is not a member of this ledger"));

        LedgerMembership authorizedLedgerMembership = membershipRepository.findById(new LedgerMembershipId(ledgerId, authorizedUserId))
                .orElseThrow(() -> new ResourceNotFoundException("Authorized user is not a member of this ledger or wasn't found"));

        if (authorizedLedgerMembership.getAccessType() != AccessType.OWNER && authorizedLedgerMembership.getAccessType() != AccessType.ADMIN){
            throw new InsufficientPermissionsException("Only owners and admins can change user's access");
        }

        if (targetLedgerMembership.getStatus() != MemberStatus.ACTIVE){
            throw new InvalidStateException("Can't change access of the user with the status " + targetLedgerMembership.getStatus());
        }

        if (targetLedgerMembership.getAccessType() == AccessType.OWNER){
            throw new InsufficientPermissionsException("The owner's access can't be revoked this way");
        }

        if (targetLedgerMembership.getAccessType() == targetAccessType){
            throw new InvalidStateException("User already has " + targetAccessType + " access type");
        }

        AccessType oldAccessType = targetLedgerMembership.getAccessType();
        targetLedgerMembership.setAccessType(targetAccessType);
        LedgerActivityLog activityLog = new LedgerActivityLog(
                targetLedgerMembership.getLedger(),
                authorizedLedgerMembership.getUser(),
                targetLedgerMembership.getUser().getId(),
                LedgerActionType.MEMBER_ACCESS_TYPE_CHANGED,
                "Member's access type was changed from " + oldAccessType + " to " + targetAccessType);
        activityLogRepository.save(activityLog);
        return membershipMapper.toResponseDto(targetLedgerMembership);
    }

    @Transactional
    public void revokeInvitation(UUID ledgerId, UUID targetUserId, UUID authorizedUserId){
        LedgerMembership targetLedgerMembership = membershipRepository.findById(new LedgerMembershipId(ledgerId, targetUserId))
                .orElseThrow(() -> new ResourceNotFoundException("Target user wasn't invited to this ledger"));

        LedgerMembership authorizedLedgerMembership = membershipRepository.findById(new LedgerMembershipId(ledgerId, authorizedUserId))
                .orElseThrow(() -> new ResourceNotFoundException("Authorized user is not a member of this ledger or wasn't found"));

        if (authorizedLedgerMembership.getAccessType() != AccessType.OWNER && authorizedLedgerMembership.getAccessType() != AccessType.ADMIN){
            throw new InsufficientPermissionsException("Only owners and admins can revoke an invitation");
        }

        if (targetLedgerMembership.getStatus() != MemberStatus.PENDING){
            throw new InvalidStateException("Can't revoke an invitation, the user's status is " + targetLedgerMembership.getStatus());
        }

        membershipRepository.delete(targetLedgerMembership);
        LedgerActivityLog activityLog = new LedgerActivityLog(
                targetLedgerMembership.getLedger(),
                authorizedLedgerMembership.getUser(),
                targetLedgerMembership.getUser().getId(),
                LedgerActionType.MEMBER_INVITATION_REVOKED,
                "User's invitation was revoked");
        activityLogRepository.save(activityLog);
    }

    @Transactional
    public void deleteMember(UUID ledgerId, UUID targetUserId, UUID authorizedUserId){
        LedgerMembership targetLedgerMembership = membershipRepository.findById(new LedgerMembershipId(ledgerId, targetUserId))
                .orElseThrow(() -> new ResourceNotFoundException("Target user is not a member of this ledger"));

        LedgerMembership authorizedLedgerMembership = membershipRepository.findById(new LedgerMembershipId(ledgerId, authorizedUserId))
                .orElseThrow(() -> new ResourceNotFoundException("Authorized user is not a member of this ledger or wasn't found"));

        if (authorizedLedgerMembership.getAccessType() != AccessType.OWNER && authorizedLedgerMembership.getAccessType() != AccessType.ADMIN){
            throw new InsufficientPermissionsException("Only owners and admins can delete a member");
        }

        if (targetLedgerMembership.getStatus() != MemberStatus.ACTIVE){
            throw new InvalidStateException("Can't delete a member, the user's status is " + targetLedgerMembership.getStatus());
        }

        if (targetLedgerMembership.getAccessType() == AccessType.OWNER){
            throw new InsufficientPermissionsException("Can't delete an owner this way. An owner must transfer their ownership my themself");
        }

        User authorizedUser = authorizedLedgerMembership.getUser();
        targetLedgerMembership.deleteMember(authorizedUser);
        LedgerActivityLog activityLog = new LedgerActivityLog(
                targetLedgerMembership.getLedger(),
                authorizedLedgerMembership.getUser(),
                targetLedgerMembership.getUser().getId(),
                LedgerActionType.MEMBER_DELETED,
                "Member was deleted from the ledger");
        activityLogRepository.save(activityLog);
    }

    @Transactional
    public LedgerMembershipResponseDto transferOwnership(UUID authorizedUserId, UUID targetUserId, UUID ledgerId){
    Ledger ledger = ledgerRepository.findById(ledgerId)
            .orElseThrow(() -> new ResourceNotFoundException("No ledger with such id"));
        LedgerMembership authorizedMember = membershipRepository.findById(new LedgerMembershipId(ledgerId, authorizedUserId))
                .orElseThrow(() -> new ResourceNotFoundException("Authorized user is not a member of this ledger or doesn't exist"));
        if (authorizedMember.getAccessType() != AccessType.OWNER) throw new InsufficientPermissionsException("Only owners can transfer ownership");

        LedgerMembership targetMember = membershipRepository.findById(new LedgerMembershipId(ledgerId, targetUserId))
                .orElseThrow(() -> new ResourceNotFoundException("Target user is not a member of this ledger or doesn't exist"));

        if (targetMember.getStatus() != MemberStatus.ACTIVE){
            throw new InvalidStateException("Can't transfer ownership to a member with a status " + targetMember.getStatus());
        }

        if (targetMember.equals(authorizedMember)){
            throw new InvalidStateException("The owner can't transfer ownership to themself");
        }
        targetMember.setAccessType(AccessType.OWNER);
        authorizedMember.setAccessType(AccessType.ADMIN);

        if (TransactionSynchronizationManager.isActualTransactionActive()) {
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            try {
                                messagingTemplate.convertAndSendToUser(
                                        targetUserId.toString(),
                                        "/queue/notifications",
                                        "You are now the owner of this ledger " + ledgerId
                                );
                            } catch (Exception e) {
                                log.error("Message in afterCommit wasn't sent. {}", e.getMessage(), e);
                            }
                        }
                    }
            );
        }

        LedgerActivityLog log = new LedgerActivityLog(
                ledger,
                authorizedMember.getUser(),
                targetUserId,
                LedgerActionType.OWNERSHIP_TRANSFERRED,
                "The previous owner decided to transfer their ownership. By default, the user who transfers their ownership becomes an admin of the ledger"
        );
        activityLogRepository.save(log);

        return membershipMapper.toResponseDto(targetMember);
    }

    @Transactional(readOnly = true)
    public LedgerMembershipResponseDto getById(UUID ledgerId, UUID userId){
        LedgerMembership ledgerMembership = membershipRepository.findById(new LedgerMembershipId(ledgerId, userId))
                .orElseThrow(() -> new ResourceNotFoundException("Current user is not a member of this ledger or user/ledger don't exist"));
        return membershipMapper.toResponseDto(ledgerMembership);
    }

    @Transactional(readOnly = true)
    public LedgerMembershipResponseDto getUserDefaultLedgerMembership(UUID userId){
        LedgerMembership ledgerMembership = membershipRepository.findDefaultLedgerMembershipByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Current user is not a member of this ledger or user/ledger don't exist"));
        return membershipMapper.toResponseDto(ledgerMembership);
    }
}