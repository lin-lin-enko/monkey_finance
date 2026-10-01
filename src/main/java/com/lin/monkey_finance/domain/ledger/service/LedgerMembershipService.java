package com.lin.monkey_finance.domain.ledger.service;

import com.lin.monkey_finance.common.exception.*;
import com.lin.monkey_finance.domain.ledger.dto.LedgerMembershipInvitationDto;
import com.lin.monkey_finance.domain.ledger.dto.LedgerMembershipResponseDto;
import com.lin.monkey_finance.domain.ledger.dto.LedgerMembershipRequestDto;
import com.lin.monkey_finance.domain.ledger.event.LedgerActivityLogEvent;
import com.lin.monkey_finance.domain.ledger.mapper.LedgerMembershipMapper;
import com.lin.monkey_finance.domain.ledger.model.*;
import com.lin.monkey_finance.domain.ledger.repository.LedgerMembershipRepository;
import com.lin.monkey_finance.domain.user.model.User;
import com.lin.monkey_finance.domain.user.service.UserService;
import jakarta.persistence.EntityManager;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class LedgerMembershipService {

    private final LedgerMembershipRepository repository;
    private final LedgerMembershipMapper mapper;
    private final UserService userService;
    private final ApplicationEventPublisher eventPublisher;
    private final EntityManager entityManager;
    private final LedgerSecurity ledgerSecurity;

    private final LedgerNotificationService notificationService;

    public LedgerMembershipService(
            LedgerMembershipRepository repository,
            LedgerMembershipMapper mapper,
            UserService userService,
            ApplicationEventPublisher eventPublisher,
            EntityManager entityManager,
            LedgerSecurity ledgerSecurity,
            LedgerNotificationService notificationService

    ){
        this.repository = repository;
        this.mapper = mapper;
        this.userService = userService;
        this.eventPublisher = eventPublisher;
        this.entityManager = entityManager;
        this.ledgerSecurity = ledgerSecurity;
        this.notificationService = notificationService;
    }

    @Transactional(readOnly = true)
    public LedgerMembership validateAndGetById(UUID ledgerId, UUID userId){
        return repository.findById(new LedgerMembershipId(ledgerId, userId))
                .orElseThrow(() -> new BadRequestException("User is not a member of this ledger or user/ledger doesn't exist"));
    }

    @Transactional(readOnly = true)
    public LedgerMembershipResponseDto findById(UUID ledgerId, UUID userId){
        return mapper.toResponseDto(validateAndGetById(ledgerId, userId));
    }


    @Transactional(readOnly = true)
    public List<LedgerMembershipResponseDto> getAllUserLedgers(UUID userId){
        return repository.findAllById_UserId(userId)
                .stream().map(mapper::toResponseDto).toList();
    }

    @Transactional
    public void addByRegistration(UUID ledgerId, UUID creatorId) {
        Ledger ledger = entityManager.getReference(Ledger.class, ledgerId);
        User creator = entityManager.getReference(User.class, creatorId);

        LedgerMembership ledgerMembership = new LedgerMembership(
                    ledger,
                    creator,
                    true,
                    AccessType.OWNER,
                    null,
                    MemberStatus.ACTIVE
        );

        repository.saveAndFlush(ledgerMembership);

        eventPublisher.publishEvent(new LedgerActivityLogEvent(
                ledgerId,
                creatorId,
                creatorId,
                LedgerActionType.MEMBER_JOINED,
                "Creator of the ledger joined"
        ));
    }

    @Transactional
    @PreAuthorize("@ledgerSecurity.isAdminOrOwner(#ledgerId, #authorizedUserId)")
    public LedgerMembershipResponseDto add(UUID ledgerId, UUID authorizedUserId, LedgerMembershipRequestDto requestDto) {

        Optional<LedgerMembership> targetMemberOptional = repository.findById(new LedgerMembershipId(ledgerId, requestDto.targetUserId()));
        LedgerMembership targetMember;
        if (targetMemberOptional.isEmpty()){
            User targetUser = userService.validateAndGetById(requestDto.targetUserId());
            targetMember = new LedgerMembership(
                    entityManager.getReference(Ledger.class, ledgerId),
                    targetUser,
                    false,
                    requestDto.accessType(),
                    userService.validateAndGetById(authorizedUserId),
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

        LedgerMembership savedLedgerMembership = repository.saveAndFlush(targetMember);
        LedgerMembershipInvitationDto invitationDto = mapper.toInvitationDto(savedLedgerMembership, entityManager.getReference(Ledger.class, ledgerId));

        notificationService.sendInvitationNotification(requestDto.targetUserId(), invitationDto);

        eventPublisher.publishEvent(new LedgerActivityLogEvent(
                ledgerId,
                authorizedUserId,
                requestDto.targetUserId(),
                LedgerActionType.MEMBER_INVITED,
                null
        ));

        return mapper.toResponseDto(savedLedgerMembership);
    }

    @Transactional
    public LedgerMembershipResponseDto acceptInvitation(UUID ledgerId, UUID userId){

        LedgerMembership ledgerMembership = repository.findById(new LedgerMembershipId(ledgerId, userId))
                .orElseThrow(() -> new BadRequestException("Invitation to this ledger wasn't found or the user has declined it"));

        if (ledgerMembership.getStatus() == MemberStatus.ACTIVE) throw new InvalidStateException("The user has already accepted the invitation to this ledger");

        if (ledgerMembership.getStatus() != MemberStatus.PENDING) throw new InvalidStateException("Cannot accept the invitation with the status " + ledgerMembership.getStatus());

        ledgerMembership.acceptInvitation();
        eventPublisher.publishEvent(new LedgerActivityLogEvent(
                ledgerId,
                userId,
                ledgerMembership.getId().getUserId(),
                LedgerActionType.MEMBER_JOINED,
                null
        ));
        return mapper.toResponseDto(ledgerMembership);
    }

    @Transactional
    public void declineInvitation(UUID ledgerId, UUID userId){
        LedgerMembership ledgerMembership = repository.findById(new LedgerMembershipId(ledgerId, userId))
                .orElseThrow(() -> new BadRequestException("Invitation to this ledger wasn't found or the user has declined it"));

        if (ledgerMembership.getStatus() != MemberStatus.PENDING) throw new InvalidStateException("Cannot decline the invitation with the status " + ledgerMembership.getStatus());

        eventPublisher.publishEvent(new LedgerActivityLogEvent(
                ledgerId,
                userId,
                ledgerMembership.getId().getUserId(),
                LedgerActionType.USER_DECLINED_INVITATION,
                "The user decided to decline the invitation"
        ));
        repository.delete(ledgerMembership);
    }

    @Transactional
    @PreAuthorize("@ledgerSecurity.isActiveMember(#ledgerId, #targetUserId)")
    public void leaveLedger(UUID ledgerId, UUID userId){
        LedgerMembership membership = checkAndGetMembershipEntity(ledgerId, userId);
        membership.leaveLedger();

        eventPublisher.publishEvent(new LedgerActivityLogEvent(
                ledgerId,
                userId,
                userId,
                LedgerActionType.MEMBER_LEFT,
                null
        ));
    }

    @Transactional
    @PreAuthorize("@ledgerSecurity.isAdminOrOwner(#ledgerId, #authorizedUserId)")
    public LedgerMembershipResponseDto block(UUID ledgerId, UUID authorizedUserId, UUID targetUserId){
        LedgerMembership targetUserMembership = checkAndGetMembershipEntity(ledgerId, targetUserId);
        if (targetUserMembership.getStatus() == MemberStatus.BLOCKED)
            throw new AccountStatusException("This user is already blocked");

        targetUserMembership.blockMember(entityManager.getReference(User.class, authorizedUserId));

        eventPublisher.publishEvent(new LedgerActivityLogEvent(
                ledgerId,
                authorizedUserId,
                targetUserMembership.getUser().getId(),
                LedgerActionType.MEMBER_BLOCKED,
                null
        ));
        return mapper.toResponseDto(targetUserMembership);
    }

    @Transactional
    @PreAuthorize("@ledgerSecurity.isAdminOrOwner(#ledgerId, #targetUserId)")
    public LedgerMembershipResponseDto unblock(UUID ledgerId, UUID authorizedUserId,  UUID targetUserId){
        LedgerMembership targetUserMembership = checkAndGetMembershipEntity(ledgerId, targetUserId);

        if (targetUserMembership.getStatus() != MemberStatus.BLOCKED)
            throw new InvalidStateException("This user isn't blocked");

        targetUserMembership.unblockMember();
        eventPublisher.publishEvent(new LedgerActivityLogEvent(
                ledgerId,
                authorizedUserId,
                targetUserId,
                LedgerActionType.MEMBER_UNBLOCKED,
                null
        ));

        return mapper.toResponseDto(targetUserMembership);
    }

    @Transactional
    @PreAuthorize("@ledgerSecurity.isAdminOrOwner(#ledgerId, #targetUserId)")
    public LedgerMembershipResponseDto changeAccess(UUID ledgerId, UUID authorizedUserId, UUID targetUserId, AccessType targetAccessType){
        ledgerSecurity.isActiveMember(ledgerId, targetUserId);
        LedgerMembership targetUserMembership =  checkAndGetMembershipEntity(ledgerId, targetUserId);

        if (targetUserMembership.getAccessType() == targetAccessType)
            throw new InvalidStateException("User already has " + targetAccessType.toString() + " access type");

        AccessType oldAccessType = targetUserMembership.getAccessType();
        targetUserMembership.setAccessType(targetAccessType);

        eventPublisher.publishEvent(new LedgerActivityLogEvent(
                ledgerId,
                authorizedUserId,
                targetUserId,
                LedgerActionType.MEMBER_ACCESS_TYPE_CHANGED,
                "Member's access type was changed from " + oldAccessType + " to " + targetAccessType
        ));
        return mapper.toResponseDto(targetUserMembership);
    }

    @Transactional
    @PreAuthorize("@ledgerSecurity.isAdminOrOwner(#ledgerId, #targetUserId)")
    public void revokeInvitation(UUID ledgerId, UUID authorizedUserId, UUID targetUserId){
        LedgerMembership targetUserMembership = checkAndGetMembershipEntity(ledgerId, targetUserId);

        if (targetUserMembership.getStatus() != MemberStatus.PENDING){
            throw new InvalidStateException("Can't revoke an invitation, the user's status is " + targetUserMembership.getStatus());
        }

        repository.delete(targetUserMembership);
        eventPublisher.publishEvent(new LedgerActivityLogEvent(
                ledgerId,
                authorizedUserId,
                targetUserId,
                LedgerActionType.MEMBER_INVITATION_REVOKED,
                "User's invitation was revoked"
        ));
    }

    @Transactional
    @PreAuthorize("@ledgerSecurity.isAdminOrOwner(#ledgerId, #authorizedUserId) and @ledgerSecurity.isActiveMember(#ledgerId, #targetUserId)")
    public void deleteMember(UUID ledgerId, UUID authorizedUserId, UUID targetUserId){
        LedgerMembership targetUserMembership = checkAndGetMembershipEntity(ledgerId, targetUserId);

        targetUserMembership.deleteMember(entityManager.getReference(User.class, authorizedUserId));
        eventPublisher.publishEvent(new LedgerActivityLogEvent(
                ledgerId,
                authorizedUserId,
                targetUserId,
                LedgerActionType.MEMBER_DELETED,
                "Member was deleted from the ledger"
        ));
    }

    @Transactional
    @PreAuthorize("@ledgerSecurity.isOwner(#ledgerId, #authorizedUserId)")
    public LedgerMembershipResponseDto transferOwnership(UUID ledgerId, UUID authorizedUserId, UUID targetUserId){
        LedgerMembership targetUserMembership = checkAndGetMembershipEntity(ledgerId, targetUserId);
        targetUserMembership.setAccessType(AccessType.OWNER);

        LedgerMembership authorizedUserMembership = checkAndGetMembershipEntity(ledgerId, authorizedUserId);
        authorizedUserMembership.setAccessType(AccessType.ADMIN);

        notificationService.sendOwnershipTransferNotification(ledgerId, authorizedUserId, targetUserId);

        eventPublisher.publishEvent(new LedgerActivityLogEvent(
                ledgerId,
                authorizedUserId,
                targetUserId,
                LedgerActionType.OWNERSHIP_TRANSFERRED,
                "The previous owner decided to transfer their ownership. By default, the user who transfers their ownership becomes an admin of the ledger"
        ));

        return mapper.toResponseDto(targetUserMembership);
    }

    @Transactional(readOnly = true)
    public LedgerMembershipResponseDto getUserDefaultLedgerMembership(UUID userId){
        LedgerMembership ledgerMembership = repository.findDefaultLedgerMembershipByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("This user doesn't have a default ledger"));
        return mapper.toResponseDto(ledgerMembership);
    }

    private LedgerMembership checkAndGetMembershipEntity(UUID ledgerId, UUID userId){
        return repository.findById(new LedgerMembershipId(ledgerId, userId))
                .orElseThrow(() -> new BadRequestException("User is not a member of this ledger"));
    }
}