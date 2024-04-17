package ru.lvmlabs.neuronum.users.service.keycloak;

import jakarta.ws.rs.core.Response;
import lombok.extern.slf4j.Slf4j;
import org.keycloak.admin.client.CreatedResponseUtil;
import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.resource.RealmResource;
import org.keycloak.admin.client.resource.RolesResource;
import org.keycloak.admin.client.resource.UserResource;
import org.keycloak.admin.client.resource.UsersResource;
import org.keycloak.authorization.client.util.HttpResponseException;
import org.keycloak.representations.idm.CredentialRepresentation;
import org.keycloak.representations.idm.UserRepresentation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.lvmlabs.neuronum.users.dto.TokenResponse;
import ru.lvmlabs.neuronum.users.dto.UserRequest;
import ru.lvmlabs.neuronum.users.dto.UserResponse;
import ru.lvmlabs.neuronum.users.exception.LoginFailedException;
import ru.lvmlabs.neuronum.users.exception.RefreshTokenFailedException;
import ru.lvmlabs.neuronum.users.exception.RefreshTokenReusedException;
import ru.lvmlabs.neuronum.users.exception.RegistrationFailedException;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.StructuredTaskScope;
import java.util.function.Supplier;

@Slf4j
@Service
public class KeycloakService {
    private final KeycloakTokensClient keycloakTokensClient;

    private final UsersResource keycloakUsers;
    private final RolesResource keycloakRoles;

    public KeycloakService(Keycloak keycloakClient, KeycloakTokensClient keycloakTokensClient, @Value("${neuronum.keycloak.realm.users}") String keycloakRealm) {
        this.keycloakTokensClient = keycloakTokensClient;

        RealmResource realmResource = keycloakClient.realm(keycloakRealm);
        keycloakUsers = realmResource.users();
        keycloakRoles = realmResource.roles();
    }

    public UUID register(UserRequest userDto) {
        UserRepresentation keycloakUser = createUser(userDto.getEmail(), userDto.getName(), userDto.getSurname());

        try (Response response = keycloakUsers.create(keycloakUser); var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            String userId = CreatedResponseUtil.getCreatedId(response);
            UserResource userResource = keycloakUsers.get(userId);
            log.debug("User created");

            scope.fork(() -> {
                log.debug("Password saving...");
                userResource.resetPassword(createPasswordCredentials(userDto.getPassword()));
                log.debug("Password saved");
                return null;
            });

            scope.fork(() -> {
                log.debug("Role saving...");
                userResource.roles().realmLevel().add(List.of(keycloakRoles.get("USER").toRepresentation()));
                log.debug("Role saved");
                return null;
            });

            scope.joinUntil(Instant.now().plusSeconds(4));
            log.debug("Registration successfully!");

            return UUID.fromString(userId);

        } catch (Exception exception) {
            log.error("Can't register in keycloak!");
            exception.printStackTrace();
        }

        throw new RegistrationFailedException();
    }

    public UserResponse login(String email, String password) {
        log.debug("Try to login by email: {}", email);

        try (var scope = new StructuredTaskScope.ShutdownOnFailure()) {
            Supplier<TokenResponse> responseSupl = scope.fork(() -> keycloakTokensClient.obtainAccessToken(email, password));
            Supplier<UserRepresentation> userSupl = scope.fork(() -> keycloakUsers.searchByEmail(email, true).get(0));

            scope.joinUntil(Instant.now().plusSeconds(4));

            TokenResponse tokenResponse = responseSupl.get();
            UserRepresentation keycloakUser = userSupl.get();

            log.debug("Login successful. Returning tokens and info...");
            return new UserResponse(
                    UUID.fromString(keycloakUser.getId()),
                    keycloakUser.getFirstName(),
                    keycloakUser.getLastName(),
                    email,
                    tokenResponse
            );
        } catch (Exception exception) {
            log.error("Can't login in keycloak!");
            exception.printStackTrace();
        }

        throw new LoginFailedException();
    }

    public TokenResponse getNewAccessToken(String refreshToken) {
        log.debug("Refreshing the token...");

        try {
            return keycloakTokensClient.obtainAccessToken(refreshToken);
        } catch (Exception exception) {
            if (exception instanceof HttpResponseException) {
                byte[] keycloakServerResponse = ((HttpResponseException) exception).getBytes();
                if (keycloakServerResponse != null && new String(keycloakServerResponse).equals("{\"error\":\"invalid_grant\",\"error_description\":\"Maximum allowed refresh token reuse exceeded\"}")) {
                    throw new RefreshTokenReusedException();
                }
            }

            log.error("Can't refresh token in keycloak!");
            exception.printStackTrace();
        }

        throw new RefreshTokenFailedException();
    }

    private CredentialRepresentation createPasswordCredentials(String password) {
        CredentialRepresentation keycloakPassword = new CredentialRepresentation();
        keycloakPassword.setTemporary(false);
        keycloakPassword.setType(CredentialRepresentation.PASSWORD);
        keycloakPassword.setValue(password);

        return keycloakPassword;
    }

    private UserRepresentation createUser(String email, String name, String surname) {
        UserRepresentation keycloakUser = new UserRepresentation();
        keycloakUser.setEnabled(true);
        keycloakUser.setEmail(email);
        keycloakUser.setUsername(email);
        keycloakUser.setFirstName(name);
        keycloakUser.setLastName(surname);

        return keycloakUser;
    }
}
