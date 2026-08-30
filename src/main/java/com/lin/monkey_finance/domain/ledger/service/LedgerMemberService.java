package com.lin.monkey_finance.domain.ledger.service;

import com.lin.monkey_finance.common.exception.AccessDeniedException;
import com.lin.monkey_finance.common.exception.InvalidStateException;
import com.lin.monkey_finance.common.exception.InsufficientPermissionsException;
import com.lin.monkey_finance.common.exception.ResourceAlreadyExistsException;
import com.lin.monkey_finance.common.exception.ResourceNotFoundException;
import com.lin.monkey_finance.domain.ledger.dto.LedgerMemberInvitationDto;
import com.lin.monkey_finance.domain.ledger.dto.LedgerMemberResponseDto;
import com.lin.monkey_finance.domain.ledger.dto.LedgerMemberRequestDto;
import com.lin.monkey_finance.domain.ledger.mapper.LedgerMemberMapper;
import com.lin.monkey_finance.domain.ledger.model.*;
import com.lin.monkey_finance.domain.ledger.repository.LedgerActivityLogRepository;
import com.lin.monkey_finance.domain.ledger.repository.LedgerMemberRepository;
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
public class LedgerMemberService {

    private final UserRepository userRepository;
    private final LedgerRepository ledgerRepository;
    private final LedgerMemberRepository ledgerMemberRepository;
    private final LedgerMemberMapper ledgerMemberMapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final LedgerActivityLogRepository activityLogRepository;

    private static final Logger log = LoggerFactory.getLogger(LedgerMemberService.class);

    public LedgerMemberService(
            UserRepository userRepository,
            LedgerRepository ledgerRepository,
            LedgerMemberRepository ledgerMemberRepository,
            LedgerMemberMapper ledgerMemberMapper,
            SimpMessagingTemplate messagingTemplate,
            LedgerActivityLogRepository activityLogRepository
    ){
        this.userRepository = userRepository;
        this.ledgerRepository = ledgerRepository;
        this.ledgerMemberRepository = ledgerMemberRepository;
        this.ledgerMemberMapper = ledgerMemberMapper;
        this.messagingTemplate = messagingTemplate;
        this.activityLogRepository = activityLogRepository;
    }

    @Transactional
    public LedgerMemberResponseDto add(LedgerMemberRequestDto requestDto, UUID ledgerId, UUID authorizedUserId) {
        if (!ledgerRepository.existsById(ledgerId)) {
            throw new ResourceNotFoundException("No ledger with such id");
        }
        Ledger ledger = ledgerRepository.getReferenceById(ledgerId);

        LedgerMember authorizedMember = ledgerMemberRepository.findById(new LedgerMemberId(ledgerId, authorizedUserId))
                .orElseThrow(() -> new AccessDeniedException("Authorized user is not a member of this ledger"));
        if (authorizedMember.getAccessType() != AccessType.ADMIN && authorizedMember.getAccessType() != AccessType.OWNER) {
            throw new InsufficientPermissionsException("Authorized user doesn't have permission to invite new members");
        }

        User authorizedUser = authorizedMember.getUser();

        Optional<LedgerMember> targetMemberOptional = ledgerMemberRepository.findById(new LedgerMemberId(ledgerId, requestDto.userId()));
        LedgerMember targetMember;
        if (targetMemberOptional.isEmpty()){
            User targetUser = userRepository.findById(requestDto.userId())
                    .orElseThrow(() -> new ResourceNotFoundException("No user with such id"));
            targetMember = new LedgerMember(
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

        LedgerMember savedLedgerMember = ledgerMemberRepository.saveAndFlush(targetMember);
        LedgerMemberInvitationDto invitationDto = ledgerMemberMapper.toInvitationDto(savedLedgerMember, ledger);

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

        return ledgerMemberMapper.toResponseDto(savedLedgerMember);
    }

    @Transactional
    public LedgerMemberResponseDto acceptInvitation(UUID ledgerId, UUID userId){

        LedgerMember ledgerMember = ledgerMemberRepository.findById(new LedgerMemberId(ledgerId, userId))
                .orElseThrow(() -> new ResourceNotFoundException("Invitation to this ledger wasn't found or the user has declined it"));

        if (ledgerMember.getStatus() == MemberStatus.ACTIVE) throw new InvalidStateException("The user has already accepted the invitation to this ledger");

        if (ledgerMember.getStatus() != MemberStatus.PENDING) throw new InvalidStateException("Cannot accept the invitation with the status " + ledgerMember.getStatus());

        ledgerMember.acceptInvitation();
        LedgerActivityLog activityLog = new LedgerActivityLog(
                ledgerMember.getLedger(),
                ledgerMember.getUser(),
                ledgerMember.getId().getUserId(),
                LedgerActionType.MEMBER_JOINED,
                null
        );
        activityLogRepository.save(activityLog);
        return ledgerMemberMapper.toResponseDto(ledgerMember);
    }

    @Transactional
    public void declineInvitation(UUID ledgerId, UUID userId){

        LedgerMember ledgerMember = ledgerMemberRepository.findById(new LedgerMemberId(ledgerId, userId))
                .orElseThrow(() -> new ResourceNotFoundException("Invitation to this ledger wasn't found or the user has declined it"));

        if (ledgerMember.getStatus() == MemberStatus.ACTIVE) throw new InvalidStateException("The user is already a member this ledger");
        if (ledgerMember.getStatus() != MemberStatus.PENDING) throw new InvalidStateException("Cannot decline the invitation with the status " + ledgerMember.getStatus());

        LedgerActivityLog activityLog = new LedgerActivityLog(
                ledgerMember.getLedger(),
                ledgerMember.getUser(),
                ledgerMember.getId().getUserId(),
                LedgerActionType.USER_DECLINED_INVITATION,
                "The user decided to decline the invitation"
        );
        activityLogRepository.save(activityLog);
        ledgerMemberRepository.delete(ledgerMember);
    }

    @Transactional
    public void leaveLedger(UUID ledgerId, UUID userId){
        LedgerMember ledgerMember = ledgerMemberRepository.findById(new LedgerMemberId(ledgerId, userId))
                .orElseThrow(() -> new ResourceNotFoundException("The user is not a member of this ledger"));

        switch (ledgerMember.getStatus()){
            case MemberStatus.LEFT -> throw new InvalidStateException("The user has already left the ledger");
            case MemberStatus.BLOCKED -> throw new InvalidStateException("The user was blocked from that ledger");
            case MemberStatus.PENDING -> throw new InvalidStateException("The user hasn't yet accepted the invitation to the ledger");
        }

        LedgerActivityLog activityLog = new LedgerActivityLog(
                ledgerMember.getLedger(),
                ledgerMember.getUser(),
                ledgerMember.getId().getUserId(),
                LedgerActionType.MEMBER_LEFT,
                null
        );
        activityLogRepository.save(activityLog);

        ledgerMember.leaveLedger();
    }

    @Transactional
    public LedgerMemberResponseDto block(UUID ledgerId, UUID targetUserId, UUID authorizedUserId){
            LedgerMember targetLedgerMember = ledgerMemberRepository.findById(new LedgerMemberId(ledgerId, targetUserId))
                    .orElseThrow(() -> new ResourceNotFoundException("Target user is not a member of this ledger"));

            LedgerMember authorizedLedgerMember = ledgerMemberRepository.findById(new LedgerMemberId(ledgerId, authorizedUserId))
                    .orElseThrow(() -> new ResourceNotFoundException("Authorized user is not a member of this ledger or wasn't found"));


            if (authorizedLedgerMember.getAccessType() != AccessType.OWNER && authorizedLedgerMember.getAccessType() != AccessType.ADMIN) {
                throw new InsufficientPermissionsException("Only owners and admins can block users");
            }

            if (targetLedgerMember.getAccessType() == AccessType.OWNER) {
                throw new InsufficientPermissionsException("An owner can't be blocked");
            }

            if (targetLedgerMember.getStatus() == MemberStatus.BLOCKED) {
                throw new InvalidStateException("Target user is already blocked");
            }

            if (targetLedgerMember.getStatus() != MemberStatus.ACTIVE) {
                throw new InvalidStateException("Target user is not a member of this ledger yet");
            }

            targetLedgerMember.blockMember(authorizedLedgerMember.getUser());

            LedgerActivityLog activityLog = new LedgerActivityLog(
                    targetLedgerMember.getLedger(),
                    authorizedLedgerMember.getUser(),
                    targetLedgerMember.getUser().getId(),
                    LedgerActionType.MEMBER_BLOCKED,
                    null);
            activityLogRepository.save(activityLog);
            return ledgerMemberMapper.toResponseDto(targetLedgerMember);
    }

    @Transactional
    public LedgerMemberResponseDto unblock(UUID ledgerId, UUID targetUserId, UUID authorizedUserId){
        LedgerMember targetLedgerMember = ledgerMemberRepository.findById(new LedgerMemberId(ledgerId, targetUserId))
                .orElseThrow(() -> new ResourceNotFoundException("Target user is not a member of this ledger"));

        LedgerMember authorizedLedgerMember = ledgerMemberRepository.findById(new LedgerMemberId(ledgerId, authorizedUserId))
                .orElseThrow(() -> new ResourceNotFoundException("Authorized user is not a member of this ledger or wasn't found"));

        if (authorizedLedgerMember.getAccessType() != AccessType.OWNER && authorizedLedgerMember.getAccessType() != AccessType.ADMIN){
            throw new InsufficientPermissionsException("Only owners and admins can unblock users");
        }

        if (targetLedgerMember.getStatus() != MemberStatus.BLOCKED){
            throw new InvalidStateException("This user isn't blocked");
        }

        User authorizedUser = authorizedLedgerMember.getUser();
        targetLedgerMember.unblockMember();
        LedgerActivityLog activityLog = new LedgerActivityLog(
                targetLedgerMember.getLedger(),
                authorizedUser,
                targetUserId,
                LedgerActionType.MEMBER_UNBLOCKED,
                null);
        activityLogRepository.save(activityLog);

        return ledgerMemberMapper.toResponseDto(targetLedgerMember);
    }


    @Transactional
    public LedgerMemberResponseDto changeAccess(UUID ledgerId, UUID targetUserId, UUID authorizedUserId, AccessType targetAccessType){
        LedgerMember targetLedgerMember = ledgerMemberRepository.findById(new LedgerMemberId(ledgerId, targetUserId))
                .orElseThrow(() -> new ResourceNotFoundException("Target user is not a member of this ledger"));

        LedgerMember authorizedLedgerMember = ledgerMemberRepository.findById(new LedgerMemberId(ledgerId, authorizedUserId))
                .orElseThrow(() -> new ResourceNotFoundException("Authorized user is not a member of this ledger or wasn't found"));

        if (authorizedLedgerMember.getAccessType() != AccessType.OWNER && authorizedLedgerMember.getAccessType() != AccessType.ADMIN){
            throw new InsufficientPermissionsException("Only owners and admins can change user's access");
        }

        if (targetLedgerMember.getStatus() != MemberStatus.ACTIVE){
            throw new InvalidStateException("Can't change access of the user with the status " + targetLedgerMember.getStatus());
        }

        if (targetLedgerMember.getAccessType() == AccessType.OWNER){
            throw new InsufficientPermissionsException("The owner's access can't be revoked this way");
        }

        if (targetLedgerMember.getAccessType() == targetAccessType){
            throw new InvalidStateException("User already has " + targetAccessType + " access type");
        }

        AccessType oldAccessType = targetLedgerMember.getAccessType();
        targetLedgerMember.setAccessType(targetAccessType);
        LedgerActivityLog activityLog = new LedgerActivityLog(
                targetLedgerMember.getLedger(),
                authorizedLedgerMember.getUser(),
                targetLedgerMember.getUser().getId(),
                LedgerActionType.MEMBER_ACCESS_TYPE_CHANGED,
                "Member's access type was changed from " + oldAccessType + " to " + targetAccessType);
        activityLogRepository.save(activityLog);
        return ledgerMemberMapper.toResponseDto(targetLedgerMember);
    }

    @Transactional
    public void revokeInvitation(UUID ledgerId, UUID targetUserId, UUID authorizedUserId){
        LedgerMember targetLedgerMember = ledgerMemberRepository.findById(new LedgerMemberId(ledgerId, targetUserId))
                .orElseThrow(() -> new ResourceNotFoundException("Target user wasn't invited to this ledger"));

        LedgerMember authorizedLedgerMember = ledgerMemberRepository.findById(new LedgerMemberId(ledgerId, authorizedUserId))
                .orElseThrow(() -> new ResourceNotFoundException("Authorized user is not a member of this ledger or wasn't found"));

        if (authorizedLedgerMember.getAccessType() != AccessType.OWNER && authorizedLedgerMember.getAccessType() != AccessType.ADMIN){
            throw new InsufficientPermissionsException("Only owners and admins can revoke an invitation");
        }

        if (targetLedgerMember.getStatus() != MemberStatus.PENDING){
            throw new InvalidStateException("Can't revoke an invitation, the user's status is " + targetLedgerMember.getStatus());
        }

        ledgerMemberRepository.delete(targetLedgerMember);
        LedgerActivityLog activityLog = new LedgerActivityLog(
                targetLedgerMember.getLedger(),
                authorizedLedgerMember.getUser(),
                targetLedgerMember.getUser().getId(),
                LedgerActionType.MEMBER_INVITATION_REVOKED,
                "User's invitation was revoked");
        activityLogRepository.save(activityLog);
    }

    @Transactional
    public void deleteMember(UUID ledgerId, UUID targetUserId, UUID authorizedUserId){
        LedgerMember targetLedgerMember = ledgerMemberRepository.findById(new LedgerMemberId(ledgerId, targetUserId))
                .orElseThrow(() -> new ResourceNotFoundException("Target user is not a member of this ledger"));

        LedgerMember authorizedLedgerMember = ledgerMemberRepository.findById(new LedgerMemberId(ledgerId, authorizedUserId))
                .orElseThrow(() -> new ResourceNotFoundException("Authorized user is not a member of this ledger or wasn't found"));

        if (authorizedLedgerMember.getAccessType() != AccessType.OWNER && authorizedLedgerMember.getAccessType() != AccessType.ADMIN){
            throw new InsufficientPermissionsException("Only owners and admins can delete a member");
        }

        if (targetLedgerMember.getStatus() != MemberStatus.ACTIVE){
            throw new InvalidStateException("Can't delete a member, the user's status is " + targetLedgerMember.getStatus());
        }

        if (targetLedgerMember.getAccessType() == AccessType.OWNER){
            throw new InsufficientPermissionsException("Can't delete an owner this way. An owner must transfer their ownership my themself");
        }

        User authorizedUser = authorizedLedgerMember.getUser();
        targetLedgerMember.deleteMember(authorizedUser);
        LedgerActivityLog activityLog = new LedgerActivityLog(
                targetLedgerMember.getLedger(),
                authorizedLedgerMember.getUser(),
                targetLedgerMember.getUser().getId(),
                LedgerActionType.MEMBER_DELETED,
                "Member was deleted from the ledger");
        activityLogRepository.save(activityLog);
    }

        @Transactional
        public LedgerMemberResponseDto transferOwnership(UUID authorizedUserId, UUID targetUserId, UUID ledgerId){
        Ledger ledger = ledgerRepository.findById(ledgerId)
                .orElseThrow(() -> new ResourceNotFoundException("No ledger with such id"));
            LedgerMember authorizedMember = ledgerMemberRepository.findById(new LedgerMemberId(ledgerId, authorizedUserId))
                    .orElseThrow(() -> new ResourceNotFoundException("Authorized user is not a member of this ledger or doesn't exist"));
            if (authorizedMember.getAccessType() != AccessType.OWNER) throw new InsufficientPermissionsException("Only owners can transfer ownership");

            LedgerMember targetMember = ledgerMemberRepository.findById(new LedgerMemberId(ledgerId, targetUserId))
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

            return ledgerMemberMapper.toResponseDto(targetMember);
        }
}