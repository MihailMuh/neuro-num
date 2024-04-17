package ru.lvmlabs.neuronum.users.exception;

public class RegistrationFailedException extends RuntimeException {
    public RegistrationFailedException() {
        super("Registration failed!");
    }
}
