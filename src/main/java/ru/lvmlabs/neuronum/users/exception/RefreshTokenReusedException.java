package ru.lvmlabs.neuronum.users.exception;

public class RefreshTokenReusedException extends RuntimeException {
    public RefreshTokenReusedException() {
        super("Maximum allowed refresh token reuse exceeded");
    }
}
