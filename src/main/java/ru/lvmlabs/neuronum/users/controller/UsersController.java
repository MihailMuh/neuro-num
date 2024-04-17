package ru.lvmlabs.neuronum.users.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Length;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.lvmlabs.neuronum.users.dto.TokenResponse;
import ru.lvmlabs.neuronum.users.dto.UserRequest;
import ru.lvmlabs.neuronum.users.dto.UserResponse;
import ru.lvmlabs.neuronum.users.service.UsersService;

import java.util.UUID;

import static org.springframework.http.MediaType.APPLICATION_JSON_VALUE;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
@CrossOrigin(originPatterns = {"https://lvmlabs.ru,http://localhost:[*]"})
@Tag(name = "Пользователи", description = "Регистрация, авторизация, аутентификация пользователей")
public class UsersController {
    private final UsersService usersService;

    @PostMapping(value = "/register", consumes = APPLICATION_JSON_VALUE, produces = APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации. JSON не корректный!", content = @Content()),
            @ApiResponse(responseCode = "422", description = "Ошибка регистрации!", content = @Content())
    })
    @Operation(summary = "Регистрация пользователя")
    public ResponseEntity<UUID> registerUser(@RequestBody @Valid UserRequest user) {
        UUID id = usersService.register(user);

        log.trace("User: {} successfully registered", user.getEmail());
        return ResponseEntity.ok(id);
    }

    @PostMapping(value = "/login", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации. Параметры не корректны!", content = @Content()),
            @ApiResponse(responseCode = "422", description = "Ошибка авторизации!", content = @Content())
    })
    @Operation(summary = "Авторизация пользователя")
    public ResponseEntity<UserResponse> login(
            @Parameter(description = "Почта пользователя", example = "mukhortovm2004@mail.ru")
            @RequestParam @NotBlank(message = "email must be non-blank") String email,

            @Parameter(description = "Пароль пользователя", example = "1234567")
            @RequestParam @Size(min = 7, max = 64, message = "password size is not valid") String password) {

        log.debug("User login in process...");
        return ResponseEntity.ok(usersService.login(email, password));
    }

    @PostMapping(value = "/refresh", produces = APPLICATION_JSON_VALUE)
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Успешно"),
            @ApiResponse(responseCode = "400", description = "Ошибка валидации. Параметры не корректны!", content = @Content()),
            @ApiResponse(responseCode = "422", description = "Ошибка обновления токена!", content = @Content()),
            @ApiResponse(responseCode = "423", description = "Превышено максимальное число переиспользования данного refresh token!", content = @Content())
    })
    @Operation(summary = "Перевыпуск access token")
    public ResponseEntity<TokenResponse> refreshToken(
            @Parameter(example = "jhgjvkbjhlgjvBHIHVJOUIHjhhvkhkljvhkj")
            @RequestParam
            @NotBlank(message = "refreshToken must be non-blank")
            @Length(min = 500)
            String refreshToken) {

        log.debug("Token refreshing...");
        return ResponseEntity.ok(usersService.getNewAccessToken(refreshToken));
    }
}
