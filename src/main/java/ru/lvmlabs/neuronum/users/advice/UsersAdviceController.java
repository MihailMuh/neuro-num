package ru.lvmlabs.neuronum.users.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.lvmlabs.neuronum.baseconfigs.advice.BaseAdviceController;
import ru.lvmlabs.neuronum.users.controller.UsersController;
import ru.lvmlabs.neuronum.users.exception.LoginFailedException;
import ru.lvmlabs.neuronum.users.exception.RefreshTokenFailedException;
import ru.lvmlabs.neuronum.users.exception.RefreshTokenReusedException;
import ru.lvmlabs.neuronum.users.exception.RegistrationFailedException;

import java.util.Map;

import static org.springframework.http.HttpStatus.LOCKED;
import static org.springframework.http.HttpStatus.UNPROCESSABLE_ENTITY;

@Slf4j
@ControllerAdvice(assignableTypes = UsersController.class)
public class UsersAdviceController extends BaseAdviceController {
    @ExceptionHandler(LoginFailedException.class)
    public ResponseEntity<Map<String, Object>> handleLoginFailed(LoginFailedException exception) {
        log.error(exception.getMessage());
        return ResponseEntity.status(UNPROCESSABLE_ENTITY).body(wrapException(exception));
    }

    @ExceptionHandler(RegistrationFailedException.class)
    public ResponseEntity<Map<String, Object>> handleRegistrationFailed(RegistrationFailedException exception) {
        log.error(exception.getMessage());
        return ResponseEntity.status(UNPROCESSABLE_ENTITY).body(wrapException(exception));
    }

    @ExceptionHandler(RefreshTokenFailedException.class)
    public ResponseEntity<Map<String, Object>> handleRefreshToken(RefreshTokenFailedException exception) {
        log.error(exception.getMessage());
        return ResponseEntity.status(UNPROCESSABLE_ENTITY).body(wrapException(exception));
    }

    @ExceptionHandler(RefreshTokenReusedException.class)
    public ResponseEntity<Map<String, Object>> handleRefreshTokenReused(RefreshTokenReusedException exception) {
        log.error(exception.getMessage());
        return ResponseEntity.status(LOCKED).body(wrapException(exception));
    }
}
