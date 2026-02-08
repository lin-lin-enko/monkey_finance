package com.lin.monkey.model;

public enum TransactionType {
    EXPENSE,
    INCOME,
    SAVINGS,
    TRANSFER;

    public String getDisplayName() {
        return switch (this) {
            case EXPENSE -> "Expense";
            case INCOME -> "Income";
            case SAVINGS -> "Savings";
            case TRANSFER -> "Transfer";
        };
    }
}
