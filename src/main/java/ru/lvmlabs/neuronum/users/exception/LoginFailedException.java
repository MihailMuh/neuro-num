package ru.lvmlabs.neuronum.users.exception;

public class LoginFailedException extends RuntimeException {
    public LoginFailedException() {
        super("Login failed!");
    }
}
