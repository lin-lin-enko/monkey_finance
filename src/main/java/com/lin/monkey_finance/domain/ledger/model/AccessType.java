package com.lin.monkey_finance.domain.ledger.model;

import com.lin.monkey_finance.common.exception.InvalidStateException;

public enum AccessType {
    OWNER,
    ADMIN,
    MEMBER,
    VIEWER;

    public static AccessType fromString(String value){
        if (value == null){
            throw new InvalidStateException("Access type can't be null");
        }

        try {
            return AccessType.valueOf(value.toUpperCase().trim());
        } catch (IllegalArgumentException e){
            throw new InvalidStateException("Invalid access type: " + value);
        }
    }
}
