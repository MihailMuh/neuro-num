package ru.lvmlabs.neuronum.baseconfigs.config.keycloak;

import org.jboss.resteasy.client.jaxrs.internal.ResteasyClientBuilderImpl;
import org.keycloak.OAuth2Constants;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.keycloak.authorization.client.AuthzClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;

@Configuration
class KeycloakAdminApiConfiguration {
    @Value("${neuronum.keycloak.url}")
    private String keycloakUrl;

    @Value("${neuronum.keycloak.realm}")
    private String keycloakRealm;

    @Value("${neuronum.keycloak.realm.users}")
    private String keycloakRealmUsers;

    @Value("${neuronum.keycloak.client-id.users}")
    private String keycloakClientIdUsers;

    @Value("${neuronum.keycloak.client-secret.users}")
    private String keycloakClientSecretUsers;

    @Value("${neuronum.keycloak.username}")
    private String keycloakClientUsername;

    @Value("${neuronum.keycloak.password}")
    private String keycloakClientPassword;

    @Value("${neuronum.keycloak.client-id}")
    private String keycloakClientId;

    @Bean
    public Keycloak keycloak(ExecutorService executorService) {
        return KeycloakBuilder.builder()
                .serverUrl(keycloakUrl)
                .realm(keycloakRealm)
                .grantType(OAuth2Constants.PASSWORD)
                .clientId(keycloakClientId)
                .username(keycloakClientUsername)
                .password(keycloakClientPassword)
                .resteasyClient(
                        new ResteasyClientBuilderImpl().executorService(executorService).connectionPoolSize(10).build()
                )
                .build();
    }

    @Bean
    public AuthzClient authzClient() {
        Map<String, Object> clientCredentials = new HashMap<>();
        clientCredentials.put("secret", keycloakClientSecretUsers);

        return AuthzClient.create(
                new org.keycloak.authorization.client.Configuration(
                        keycloakUrl, keycloakRealmUsers, keycloakClientIdUsers, clientCredentials, null
                )
        );
    }
}
