package ru.lvmlabs.neuronum.users.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.lvmlabs.neuronum.users.dto.TokenResponse;
import ru.lvmlabs.neuronum.users.dto.UserRequest;
import ru.lvmlabs.neuronum.users.dto.UserResponse;
import ru.lvmlabs.neuronum.users.exception.LoginFailedException;
import ru.lvmlabs.neuronum.users.model.User;
import ru.lvmlabs.neuronum.users.repository.UsersRepository;
import ru.lvmlabs.neuronum.users.service.keycloak.KeycloakService;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsersService {
    private final UsersRepository usersRepository;

    private final KeycloakService keycloakService;

    public UUID register(UserRequest userDto) {
        log.debug("User registration...");

        UUID userId = keycloakService.register(userDto);
        usersRepository.save(new User(userId));

        log.debug("User fully registered!");
        return userId;
    }

    public UserResponse login(String email, String password) {
        log.debug("User login...");

        UserResponse userResponse = keycloakService.login(email, password);
        Optional<User> userWithOrg =  usersRepository.findById(userResponse.getId());

        if (userWithOrg.isEmpty()) {
            log.error("Can't find user in project db!");
            throw new LoginFailedException();
        }

        userResponse.setOrganizations(userWithOrg.get().getOrganizations());
        log.debug("User login!");
        return userResponse;
    }

    public TokenResponse getNewAccessToken(String refreshToken) {
        return keycloakService.getNewAccessToken(refreshToken);
    }
}
