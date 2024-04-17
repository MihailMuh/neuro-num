package ru.lvmlabs.neuronum.users.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.lvmlabs.neuronum.users.model.User;

import java.util.UUID;

public interface UsersRepository extends JpaRepository<User, UUID> {
}
