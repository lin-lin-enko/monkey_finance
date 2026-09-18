package com.lin.monkey_finance.domain.ledger.service;

import com.lin.monkey_finance.common.exception.InvalidStateException;
import com.lin.monkey_finance.common.exception.InsufficientPermissionsException;
import com.lin.monkey_finance.common.exception.ResourceAlreadyExistsException;
import com.lin.monkey_finance.common.exception.ResourceNotFoundException;
import com.lin.monkey_finance.domain.ledger.dto.LedgerMembershipInvitationDto;
import com.lin.monkey_finance.domain.ledger.dto.LedgerMembershipResponseDto;
import com.lin.monkey_finance.domain.ledger.dto.LedgerMembershipRequestDto;
import com.lin.monkey_finance.domain.ledger.mapper.LedgerMembershipMapper;
import com.lin.monkey_finance.domain.ledger.model.*;
import com.lin.monkey_finance.domain.ledger.repository.LedgerMembershipRepository;
import com.lin.monkey_finance.domain.user.model.User;
import com.lin.monkey_finance.domain.user.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LedgerMembershipService {

    private final LedgerService ledgerService;
    private final LedgerMembershipRepository membershipRepository;
    private final LedgerMembershipMapper mapper;
    private final SimpMessagingTemplate messagingTemplate;
    private final UserService userService;
    private final LedgerActivityLogService activityLogService;

    private static final Logger log = LoggerFactory.getLogger(LedgerMembershipService.class);

    public LedgerMembershipService(
            LedgerService ledgerService,
            LedgerMembershipRepository membershipRepository,
            LedgerMembershipMapper mapper,
            SimpMessagingTemplate messagingTemplate,
            UserService userService,
            LedgerActivityLogService activityLogService
    ){
        this.ledgerService = ledgerService;
        this.membershipRepository = membershipRepository;
        this.mapper = mapper;
        this.messagingTemplate = messagingTemplate;
        this.activityLogService = activityLogService;
        this.userService = userService;
    }

    @Transactional(readOnly = true)
    public LedgerMembership getReferenceById(UUID ledgerId, UUID userId){
        if (membershipRepository.existsById(new LedgerMembershipId(ledgerId, userId)))
            return membershipRepository.getReferenceById(new LedgerMembershipId(ledgerId, userId));
        else
            throw new ResourceNotFoundException("User is not a member of this ledger or user/ledger doesn't exist");
    }

    @Transactional
    public LedgerMembershipResponseDto addByRegistration(UUID ledgerId, UUID creatorId) {
        Ledger ledger = ledgerService.getReferenceById(ledgerId);
        User user = userService.getReferenceById(creatorId);

        LedgerMembership ledgerMembership = new LedgerMembership(
                    ledger,
                    user,
                    true,
                    AccessType.OWNER,
                    null,
                    MemberStatus.ACTIVE
        );

        LedgerMembership savedLedgerMembership = membershipRepository.saveAndFlush(ledgerMembership);

        activityLogService.create(
                ledgerId,
                creatorId,
                creatorId,
                LedgerActionType.MEMBER_JOINED,
                "Creator of the ledger joined");

        return mapper.toResponseDto(savedLedgerMembership);
    }

    @Transactional
    public LedgerMembershipResponseDto add(LedgerMembershipRequestDto requestDto, UUID ledgerId, UUID authorizedUserId) {
        LedgerMembership authorizedUserMembership = getAuthorizedUserMembership(ledgerId, authorizedUserId);

        User authorizedUser = authorizedUserMembership.getUser();

        Optional<LedgerMembership> targetMemberOptional = membershipRepository.findById(new LedgerMembershipId(ledgerId, requestDto.userId()));
        LedgerMembership targetMember;
        if (targetMemberOptional.isEmpty()){
            User targetUser = userService.getReferenceById(requestDto.userId());
            targetMember = new LedgerMembership(
                    authorizedUserMembership.getLedger(),
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
        LedgerMembershipInvitationDto invitationDto = mapper.toInvitationDto(savedLedgerMembership, authorizedUserMembership.getLedger());

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

        activityLogService.create(
                ledgerId,
                authorizedUserId,
                UUID.fromString(targetUserId),
                LedgerActionType.MEMBER_INVITED,
                null);

        return mapper.toResponseDto(savedLedgerMembership);
    }

    @Transactional
    public LedgerMembershipResponseDto acceptInvitation(UUID ledgerId, UUID userId){

        LedgerMembership ledgerMembership = membershipRepository.findById(new LedgerMembershipId(ledgerId, userId))
                .orElseThrow(() -> new ResourceNotFoundException("Invitation to this ledger wasn't found or the user has declined it"));

        if (ledgerMembership.getStatus() == MemberStatus.ACTIVE) throw new InvalidStateException("The user has already accepted the invitation to this ledger");

        if (ledgerMembership.getStatus() != MemberStatus.PENDING) throw new InvalidStateException("Cannot accept the invitation with the status " + ledgerMembership.getStatus());

        ledgerMembership.acceptInvitation();
        activityLogService.create(
                ledgerId,
                userId,
                ledgerMembership.getId().getUserId(),
                LedgerActionType.MEMBER_JOINED,
                null
        );
        return mapper.toResponseDto(ledgerMembership);
    }

    @Transactional
    public void declineInvitation(UUID ledgerId, UUID userId){

        LedgerMembership ledgerMembership = membershipRepository.findById(new LedgerMembershipId(ledgerId, userId))
                .orElseThrow(() -> new ResourceNotFoundException("Invitation to this ledger wasn't found or the user has declined it"));

        if (ledgerMembership.getStatus() == MemberStatus.ACTIVE) throw new InvalidStateException("The user is already a member this ledger");
        if (ledgerMembership.getStatus() != MemberStatus.PENDING) throw new InvalidStateException("Cannot decline the invitation with the status " + ledgerMembership.getStatus());

        activityLogService.create(
                ledgerId,
                userId,
                ledgerMembership.getId().getUserId(),
                LedgerActionType.USER_DECLINED_INVITATION,
                "The user decided to decline the invitation"
        );
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

        activityLogService.create(
                ledgerId,
                userId,
                ledgerMembership.getId().getUserId(),
                LedgerActionType.MEMBER_LEFT,
                null
        );

        ledgerMembership.leaveLedger();
    }

    @Transactional
    public LedgerMembershipResponseDto block(UUID ledgerId, UUID targetUserId, UUID authorizedUserId){

        LedgerMembership authorizedUserMembership = getAuthorizedUserMembership(ledgerId, authorizedUserId);
        LedgerMembership targetUserMembership = getTargetUserMembership(ledgerId, targetUserId, authorizedUserId);


        if (targetUserMembership.getStatus() == MemberStatus.BLOCKED) {
                throw new InvalidStateException("Target user is already blocked");
            }

            targetUserMembership.blockMember(authorizedUserMembership.getUser());

            activityLogService.create(
                    ledgerId,
                    authorizedUserId,
                    targetUserMembership.getUser().getId(),
                    LedgerActionType.MEMBER_BLOCKED,
                    null
            );
            return mapper.toResponseDto(targetUserMembership);
    }

    @Transactional
    public LedgerMembershipResponseDto unblock(UUID ledgerId, UUID targetUserId, UUID authorizedUserId){
        getAuthorizedUserMembership(ledgerId, authorizedUserId);
        LedgerMembership targetUserMembership = getTargetUserMembership(ledgerId, targetUserId, authorizedUserId);

        if (targetUserMembership.getStatus() != MemberStatus.BLOCKED){
            throw new InvalidStateException("This user isn't blocked");
        }

        targetUserMembership.unblockMember();
        activityLogService.create(
                ledgerId,
                authorizedUserId,
                targetUserId,
                LedgerActionType.MEMBER_UNBLOCKED,
                null
        );

        return mapper.toResponseDto(targetUserMembership);
    }


    @Transactional
    public LedgerMembershipResponseDto changeAccess(UUID ledgerId, UUID targetUserId, UUID authorizedUserId, AccessType targetAccessType){

        getAuthorizedUserMembership(ledgerId, authorizedUserId);
        LedgerMembership targetUserMembership = getTargetUserMembership(ledgerId, targetUserId, authorizedUserId);

        if (targetUserMembership.getAccessType() == targetAccessType)
            throw new InvalidStateException("User already has " + targetAccessType + " access type");

        AccessType oldAccessType = targetUserMembership.getAccessType();
        targetUserMembership.setAccessType(targetAccessType);
        activityLogService.create(
                ledgerId,
                authorizedUserId,
                targetUserId,
                LedgerActionType.MEMBER_ACCESS_TYPE_CHANGED,
                "Member's access type was changed from " + oldAccessType + " to " + targetAccessType);
        return mapper.toResponseDto(targetUserMembership);
    }

    @Transactional
    public void revokeInvitation(UUID ledgerId, UUID targetUserId, UUID authorizedUserId){
        getAuthorizedUserMembership(ledgerId, authorizedUserId);
        LedgerMembership targetUserMembership = getTargetUserMembership(ledgerId, targetUserId, authorizedUserId);

        if (targetUserMembership.getStatus() != MemberStatus.PENDING){
            throw new InvalidStateException("Can't revoke an invitation, the user's status is " + targetUserMembership.getStatus());
        }

        membershipRepository.delete(targetUserMembership);
        activityLogService.create(
                ledgerId,
                authorizedUserId,
                targetUserId,
                LedgerActionType.MEMBER_INVITATION_REVOKED,
                "User's invitation was revoked"
        );
    }

    @Transactional
    public void deleteMember(UUID ledgerId, UUID targetUserId, UUID authorizedUserId){
        LedgerMembership authorizedUserMembership = getAuthorizedUserMembership(ledgerId, authorizedUserId);
        LedgerMembership targetUserMembership = getTargetUserMembership(ledgerId, targetUserId, authorizedUserId);

        targetUserMembership.deleteMember(authorizedUserMembership.getUser());
        activityLogService.create(
                ledgerId,
                authorizedUserId,
                targetUserId,
                LedgerActionType.MEMBER_DELETED,
                "Member was deleted from the ledger"
        );
    }

    @Transactional
    public LedgerMembershipResponseDto transferOwnership(UUID authorizedUserId, UUID targetUserId, UUID ledgerId){
        LedgerMembership authorizedUserMembership = getAuthorizedUserMembership(ledgerId, authorizedUserId);
        LedgerMembership targetUserMembership = getTargetUserMembership(ledgerId, targetUserId, authorizedUserId);

        if (authorizedUserMembership.getAccessType() != AccessType.OWNER)
            throw new InsufficientPermissionsException("Only owners can transfer ownership");

        targetUserMembership.setAccessType(AccessType.OWNER);
        authorizedUserMembership.setAccessType(AccessType.ADMIN);

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

        activityLogService.create(
                ledgerId,
                authorizedUserId,
                targetUserId,
                LedgerActionType.OWNERSHIP_TRANSFERRED,
                "The previous owner decided to transfer their ownership. By default, the user who transfers their ownership becomes an admin of the ledger"
        );

        return mapper.toResponseDto(targetUserMembership);
    }

    @Transactional(readOnly = true)
    public LedgerMembershipResponseDto getById(UUID ledgerId, UUID userId){
        LedgerMembership ledgerMembership = membershipRepository.findById(new LedgerMembershipId(ledgerId, userId))
                .orElseThrow(() -> new ResourceNotFoundException("Current user is not a member of this ledger or user/ledger don't exist"));
        return mapper.toResponseDto(ledgerMembership);
    }

    @Transactional(readOnly = true)
    public LedgerMembershipResponseDto getUserDefaultLedgerMembership(UUID userId){
        LedgerMembership ledgerMembership = membershipRepository.findDefaultLedgerMembershipByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Current user is not a member of this ledger or user/ledger don't exist"));
        return mapper.toResponseDto(ledgerMembership);
    }

    @Transactional(readOnly = true)
    public List<LedgerMembershipResponseDto> getAllByUserId(UUID userId){
        userService.getReferenceById(userId);

        return membershipRepository.findAllById_UserId(userId).stream().map(mapper::toResponseDto).toList();
    }

    private LedgerMembership getAuthorizedUserMembership(UUID ledgerId, UUID authorizedUserId){

        LedgerMembership authorizedUserMembership = membershipRepository.findById(new LedgerMembershipId(ledgerId, authorizedUserId))
                .orElseThrow(() -> new ResourceNotFoundException("Authorized user is not a member of this ledger or ledger/user don't exist"));

        if (authorizedUserMembership.getAccessType() != AccessType.OWNER && authorizedUserMembership.getAccessType() != AccessType.ADMIN)
            throw new InsufficientPermissionsException("Only owners and admins can perform this action");

        if (authorizedUserMembership.getStatus() != MemberStatus.ACTIVE)
            throw new InvalidStateException("Only active members can perform this action");

        return authorizedUserMembership;
    }

    private LedgerMembership getTargetUserMembership(UUID ledgerId, UUID targetUserId, UUID authorizedUserId){
        LedgerMembership targetUserMembership = membershipRepository.findById(new LedgerMembershipId(ledgerId, targetUserId))
                .orElseThrow(() -> new ResourceNotFoundException("Target user is not a member of this ledger or ledger/user don't exist"));

        if (targetUserMembership.getStatus() != MemberStatus.ACTIVE)
            throw new InvalidStateException("Can't perform this action to a member with a status " + targetUserMembership.getStatus());


        if (targetUserMembership.getUser().getId().equals(authorizedUserId))
            throw new InvalidStateException("The owner can't perform this action to themselves");

        return targetUserMembership;
    }
}