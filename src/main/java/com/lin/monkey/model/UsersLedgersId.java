package com.lin.monkey.model;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

public class UsersLedgersId implements Serializable {

    private UUID userId;
    private UUID ledgerId;

    public UsersLedgersId() {
    }

    public UsersLedgersId(UUID userId, UUID ledgerId) {
        this.userId = userId;
        this.ledgerId = ledgerId;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public UUID getLedgerId() {
        return ledgerId;
    }

    public void setLedgerId(UUID ledgerId) {
        this.ledgerId = ledgerId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        UsersLedgersId usersLedgersId = (UsersLedgersId) obj;
        return userId.equals(usersLedgersId.userId) && ledgerId.equals(usersLedgersId.ledgerId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, ledgerId);
    }
}
