package ru.lvmlabs.neuronum.users.service.keycloak;

import lombok.extern.slf4j.Slf4j;
import org.keycloak.authorization.client.AuthzClient;
import org.keycloak.authorization.client.util.Http;
import org.keycloak.representations.AccessTokenResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.lvmlabs.neuronum.users.dto.TokenResponse;

@Slf4j
@Service
public class KeycloakTokensClient {
    private final AuthzClient authzClient;

    private final Http http;

    @Value("${neuronum.keycloak.client-secret.users}")
    private String clientSecret;

    @Value("${neuronum.keycloak.client-id.users}")
    private String clientId;

    public KeycloakTokensClient(AuthzClient authzClient) {
        this.authzClient = authzClient;

        http = new Http(authzClient.getConfiguration(), authzClient.getConfiguration().getClientCredentialsProvider());
    }

    public TokenResponse obtainAccessToken(String email, String password) {
        AccessTokenResponse tokenResponse = authzClient.obtainAccessToken(email, password);
        return convertAccessTokenResponse(tokenResponse);
    }

    public TokenResponse obtainAccessToken(String refreshToken) {
        AccessTokenResponse tokenResponse =  http.<AccessTokenResponse>post(authzClient.getServerConfiguration().getTokenEndpoint())
                .authentication()
                .client()
                .form()
                .param("grant_type", "refresh_token")
                .param("refresh_token", refreshToken)
                .param("client_id", clientId)
                .param("client_secret", clientSecret)
                .response()
                .json(AccessTokenResponse.class)
                .execute();

        return convertAccessTokenResponse(tokenResponse);
    }

    private TokenResponse convertAccessTokenResponse(AccessTokenResponse tokenResponse) {
        return new TokenResponse(
                tokenResponse.getToken(),
                tokenResponse.getRefreshToken(),
                tokenResponse.getExpiresIn(),
                tokenResponse.getRefreshExpiresIn()
        );
    }
}
