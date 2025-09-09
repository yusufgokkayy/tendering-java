package com.tendering.exceptionHandlers;

public class WalletLockedException extends RuntimeException {
    public WalletLockedException(String message) {
        super(message);
    }
}