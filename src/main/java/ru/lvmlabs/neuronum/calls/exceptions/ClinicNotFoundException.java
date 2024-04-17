package ru.lvmlabs.neuronum.calls.exceptions;

public class ClinicNotFoundException extends RuntimeException {
    public ClinicNotFoundException() {
        super("Can't find required clinic!");
    }
}
