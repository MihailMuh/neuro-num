package ru.lvmlabs.neuronum.calls.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.lvmlabs.neuronum.baseconfigs.advice.BaseAdviceController;
import ru.lvmlabs.neuronum.calls.controller.CallsController;
import ru.lvmlabs.neuronum.calls.exceptions.AudioDownloadException;
import ru.lvmlabs.neuronum.calls.exceptions.ClinicNotFoundException;

import java.util.Map;
import java.util.NoSuchElementException;

import static org.springframework.http.HttpStatus.*;

@Slf4j
@ControllerAdvice(assignableTypes = CallsController.class)
public class CallsAdviceController extends BaseAdviceController {
    @ExceptionHandler(ClinicNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleClinicNotFoundException(ClinicNotFoundException exception) {
        log.error(exception.getMessage());
        return ResponseEntity.status(I_AM_A_TEAPOT).body(wrapException(exception));
    }

    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<Map<String, Object>> handleNoSuchElementException(NoSuchElementException exception) {
        log.error(exception.getMessage());
        return ResponseEntity.status(NOT_FOUND).body(wrapException(exception));
    }

    @ExceptionHandler(AudioDownloadException.class)
    public ResponseEntity<Map<String, Object>> handleAudioDownloadException(AudioDownloadException exception) {
        log.error(exception.getMessage());
        return ResponseEntity.status(UNPROCESSABLE_ENTITY).body(wrapException(exception));
    }
}
