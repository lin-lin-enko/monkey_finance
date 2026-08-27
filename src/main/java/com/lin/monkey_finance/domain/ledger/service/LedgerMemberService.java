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
import com.lin.monkey_finance.domain.ledger.repository.LedgerMemberRepository;
import com.lin.monkey_finance.domain.ledger.repository.LedgerRepository;
import com.lin.monkey_finance.domain.user.model.User;
import com.lin.monkey_finance.domain.user.repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

@Service
public class LedgerMemberService {

    private final UserRepository userRepository;
    private final LedgerRepository ledgerRepository;
    private final LedgerMemberRepository ledgerMemberRepository;
    private final LedgerMemberMapper ledgerMemberMapper;
    private final SimpMessagingTemplate messagingTemplate;

    public LedgerMemberService(
            UserRepository userRepository,
            LedgerRepository ledgerRepository,
            LedgerMemberRepository ledgerMemberRepository,
            LedgerMemberMapper ledgerMemberMapper,
            SimpMessagingTemplate messagingTemplate
    ){
        this.userRepository = userRepository;
        this.ledgerRepository = ledgerRepository;
        this.ledgerMemberRepository = ledgerMemberRepository;
        this.ledgerMemberMapper = ledgerMemberMapper;
        this.messagingTemplate = messagingTemplate;
    }

    @Transactional
    public LedgerMemberResponseDto add(LedgerMemberRequestDto requestDto, UUID ledgerId, UUID authorizedUserId){
        if (!ledgerRepository.existsById(ledgerId)){
            throw new ResourceNotFoundException("No ledger with such id");
        }

        LedgerMember userMembership = ledgerMemberRepository.findById(new LedgerMemberId(ledgerId, authorizedUserId))
                .orElseThrow(() -> new AccessDeniedException("Authorized user is not a member of this ledger"));
        if (userMembership.getAccessType() != AccessType.ADMIN && userMembership.getAccessType() != AccessType.OWNER){
            throw new InsufficientPermissionsException("Authorized user doesn't have permission to add new members");
        }

        User authorizedUser = userRepository.findById(authorizedUserId)
                .orElseThrow(() -> new ResourceNotFoundException("No user with such id"));

        if (ledgerMemberRepository.existsById(new LedgerMemberId(ledgerId, requestDto.userId()))){
            throw new ResourceAlreadyExistsException("Target user is already a member of this ledger");
        }

        User targetUser = userRepository.findById(requestDto.userId())
                .orElseThrow(() -> new ResourceNotFoundException("No user with such id"));

        Ledger ledger = ledgerRepository.getReferenceById(ledgerId);

        LedgerMember ledgerMember = new LedgerMember(
                ledger,
                targetUser,
                false,
                requestDto.accessType(),
                authorizedUser,
                MemberStatus.PENDING);

        LedgerMember savedLedgerMember = ledgerMemberRepository.saveAndFlush(ledgerMember);

        LedgerMemberInvitationDto invitationDto = ledgerMemberMapper.toInvitationDto(savedLedgerMember, ledger);

        final String targetUserId = targetUser.getId().toString();

        if (TransactionSynchronizationManager.isActualTransactionActive()){
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit(){
                            try{
                                messagingTemplate.convertAndSendToUser(
                                        targetUserId,
                                        "/queue/invitations",
                                        invitationDto
                                );
                            }
                            catch (Exception e){
                                System.err.println("Message in afterCommit wasn't sent: " + e.getMessage());
                                e.printStackTrace();
                            }
                        }
                    }
            );
        }

        return ledgerMemberMapper.toResponseDto(savedLedgerMember);
    }

    @Transactional
    public LedgerMemberResponseDto acceptInvitation(UUID ledgerId, UUID userId){

        LedgerMember ledgerMember = ledgerMemberRepository.findById(new LedgerMemberId(ledgerId, userId))
                .orElseThrow(() -> new ResourceNotFoundException("Invitation to this ledger wasn't found or the user has declined it"));

        if (ledgerMember.getStatus() == MemberStatus.ACTIVE) throw new InvalidStateException("The user has already accepted the invitation to this ledger");

        if (ledgerMember.getStatus() != MemberStatus.PENDING) throw new InvalidStateException("Cannot accept the invitation with the status " + ledgerMember.getStatus());

        ledgerMember.acceptInvitation();
        return ledgerMemberMapper.toResponseDto(ledgerMember);
    }

    @Transactional
    public void declineInvitation(UUID ledgerId, UUID userId){

        LedgerMember ledgerMember = ledgerMemberRepository.findById(new LedgerMemberId(ledgerId, userId))
                .orElseThrow(() -> new ResourceNotFoundException("Invitation to this ledger wasn't found or the user has declined it"));

        if (ledgerMember.getStatus() == MemberStatus.ACTIVE) throw new InvalidStateException("The user is already a member this ledger");
        if (ledgerMember.getStatus() != MemberStatus.PENDING) throw new InvalidStateException("Cannot decline the invitation with the status " + ledgerMember.getStatus());

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

        ledgerMember.leaveLedger();
    }

    @Transactional
    public LedgerMemberResponseDto block(UUID ledgerId, UUID targetUserId, UUID authorizedUserId){
        LedgerMember targetLedgerMember = ledgerMemberRepository.findById(new LedgerMemberId(ledgerId, targetUserId))
                .orElseThrow(() -> new ResourceNotFoundException("Target user is not a member of this ledger"));
        if (!userRepository.existsById(authorizedUserId)){
            throw new ResourceNotFoundException("Authorized user not found");
        }
        User authorizedUser = userRepository.getReferenceById(authorizedUserId);
        LedgerMember authorizedLedgerMember = ledgerMemberRepository.findById(new LedgerMemberId(ledgerId, authorizedUserId))
                .orElseThrow(() -> new ResourceNotFoundException("Authorized user is not a member of this ledger"));


        if (authorizedLedgerMember.getAccessType() != AccessType.OWNER && authorizedLedgerMember.getAccessType() != AccessType.ADMIN){
            throw new AccessDeniedException("Only owners and admins can block users");
        }

        if (targetLedgerMember.getAccessType() == AccessType.OWNER){
            throw new AccessDeniedException("An owner can't be blocked");
        }

        if (targetLedgerMember.getStatus() == MemberStatus.BLOCKED){
            throw new InvalidStateException("Target user is already blocked");
        }

        targetLedgerMember.blockMember(authorizedUser);
        return ledgerMemberMapper.toResponseDto(targetLedgerMember);
    }
}
