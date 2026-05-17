package repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import entities.UserEntity;

import java.util.Optional;

public interface UserRepository
        extends JpaRepository<UserEntity, Long> {

    Optional<UserEntity> findByNickIgnoreCase(String nick);

    boolean existsByNickIgnoreCase(String nick);
}
