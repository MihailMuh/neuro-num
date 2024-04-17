package ru.lvmlabs.neuronum.calls.exceptions;

public class AudioDownloadException extends RuntimeException {
    public AudioDownloadException() {
        super("Can't download required audio record!");
    }
}
