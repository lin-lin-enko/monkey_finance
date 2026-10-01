package com.lin.monkey_finance.domain.ledger.service;

import com.lin.monkey_finance.domain.ledger.dto.LedgerMembershipInvitationDto;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.UUID;

@Service
public class LedgerNotificationService {
    private final SimpMessagingTemplate messagingTemplate;
    private static final Logger log = LoggerFactory.getLogger(LedgerNotificationService.class);

    public LedgerNotificationService(SimpMessagingTemplate messagingTemplate)
    {
        this.messagingTemplate = messagingTemplate;
    }

    public void sendInvitationNotification(UUID targetUserId, LedgerMembershipInvitationDto invitationDto){
        if(TransactionSynchronizationManager.isActualTransactionActive()){
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            try {
                                messagingTemplate.convertAndSendToUser(
                                        targetUserId.toString(),
                                        "/queue/invitations",
                                        invitationDto
                                );
                            } catch (Exception e) {
                                log.error("Invitation in afterCommit wasn't sent. {}", e.getMessage(), e);
                            }
                        }
                    }
            );
        }
    }

    public void sendOwnershipTransferNotification(UUID ledgerId, UUID oldOwnerId, UUID newOwnerId){
        if (TransactionSynchronizationManager.isActualTransactionActive()){
            TransactionSynchronizationManager.registerSynchronization(
                    new TransactionSynchronization() {
                        @Override
                        public void afterCommit() {
                            try {
                                messagingTemplate.convertAndSendToUser(
                                        oldOwnerId.toString(),
                                        "/queue/notifications",
                                        "You successfully transferred ownership and are now an admin of this ledger: " + ledgerId.toString()
                                );
                            } catch (Exception e){
                                log.error("Ownership transfer notification for an old owner in afterCommit wasn't sent. {}", e.getMessage(), e);
                            }

                            try{
                                messagingTemplate.convertAndSendToUser(
                                        newOwnerId.toString(),
                                        "/queue/notifications",
                                        "You were made an owner of this ledger: " + ledgerId.toString()
                                );
                            } catch (Exception e){
                                log.error("Ownership transfer notification for a new owner in afterCommit wasn't sent. {}", e.getMessage(), e);
                            }
                        }
                    }
            );
        }
    }
}
