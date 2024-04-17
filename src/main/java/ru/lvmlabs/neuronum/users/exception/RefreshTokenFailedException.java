package ru.lvmlabs.neuronum.users.exception;

public class RefreshTokenFailedException  extends RuntimeException {
    public RefreshTokenFailedException() {
        super("Token refreshing failed!");
    }
}
