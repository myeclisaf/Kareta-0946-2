package ru.gr0946x.net.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.gr0946x.net.entities.UserEntity;

import java.util.Optional;

public interface UserRepository
        extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByNickIgnoreCase(String nick);

    boolean existsByNickIgnoreCase(String nick);
}
