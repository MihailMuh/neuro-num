package ru.lvmlabs.neuronum.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Модель пользователя при регистрации")
public class UserRequest {
    @NotBlank(message = "email must be non-blank")
    @Email(message = "email is not valid")
    @Schema(description = "Почта", example = "mukhortovm2004@mail.ru")
    private String email;

    @NotBlank(message = "name must be non-blank")
    @Schema(description = "Имя", example = "Иван")
    private String name;

    @NotBlank(message = "surname must be non-blank")
    @Schema(description = "Фамилия", example = "Калыван")
    private String surname;

    @NotBlank(message = "password must be non-blank")
    @Size(min = 7, max = 64, message = "password size is not valid")
    @Schema(description = "Пароль", example = "1234567")
    private String password;
}
