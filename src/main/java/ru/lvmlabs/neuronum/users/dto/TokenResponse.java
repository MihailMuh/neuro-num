package ru.lvmlabs.neuronum.users.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Schema(description = "Модель токенов при обновлении access token")
public class TokenResponse {
    @Schema(description = "Токен доступа", example = "aaerg3ag5gqe")
    private String token;

    @Schema(description = "Токен обновления", example = "aaerg3a")
    private String refreshToken;

    @Schema(description = "Когда токен истекает", example = "300")
    private long expiresIn;

    @Schema(description = "Когда токен обновления истекает", example = "1800")
    private long refreshExpiresIn;
}
