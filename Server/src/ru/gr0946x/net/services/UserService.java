package ru.gr0946x.net.services;

import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.gr0946x.net.entities.UserEntity;
import ru.gr0946x.net.repositories.UserRepository;

@Service
public class UserService {

    private final UserRepository repository;

    public UserService(UserRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public UserEntity register(
            String nick,
            String password
    ) {

        if (!Character.isLetter(nick.charAt(0))) {
            throw new IllegalArgumentException(
                    "Имя должно начинаться с буквы"
            );
        }

        if (repository.existsByNickIgnoreCase(nick)) {
            throw new IllegalArgumentException(
                    "Имя уже занято"
            );
        }

        var hash = BCrypt.hashpw(
                password,
                BCrypt.gensalt()
        );

        return repository.save(
                new UserEntity(nick, hash)
        );
    }

    @Transactional(readOnly = true)
    public UserEntity login(
            String nick,
            String password
    ) {

        var user = repository.findByNickIgnoreCase(nick).orElseThrow(() ->
                        new IllegalArgumentException("Неверный логин"));

        if (!BCrypt.checkpw(
                password,
                user.getPasswordHash()
        )) {
            throw new IllegalArgumentException("Неверный пароль");
        }

        return user;
    }
}
