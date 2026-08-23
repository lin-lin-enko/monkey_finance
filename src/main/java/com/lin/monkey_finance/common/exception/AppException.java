package com.lin.monkey_finance.common.exception;

public abstract class AppException extends RuntimeException {
    protected AppException(String message){
        super(message);
    }
}
