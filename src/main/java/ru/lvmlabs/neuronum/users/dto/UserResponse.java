package ru.lvmlabs.neuronum.users.dto;

import com.fasterxml.jackson.annotation.JsonUnwrapped;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import ru.lvmlabs.neuronum.users.model.Organization;

import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Модель пользователя при логине")
public class UserResponse {
    @Schema(description = "Сгенерированный ID пользователя")
    private UUID id;

    @Schema(description = "Имя", example = "Иван")
    private String name;

    @Schema(description = "Фамилия", example = "Калыван")
    private String surname;

    @Schema(description = "Почта", example = "mukhortovm2004@mail.ru")
    private String email;

    @Schema(description = "Список организаций, в которых состоит / которыми владеет пользователь")
    private List<Organization> organizations;

    @JsonUnwrapped
    private TokenResponse tokenResponse;

    public UserResponse(UUID id, String name, String surname, String email, TokenResponse tokenResponse) {
        this.id = id;
        this.name = name;
        this.surname = surname;
        this.email = email;
        this.tokenResponse = tokenResponse;
    }
}
