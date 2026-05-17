package Services;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import entities.MessageEntity;
import entities.UserEntity;
import repositories.MessageRepository;

import java.util.List;

@Service
public class MessageService {

    private final MessageRepository repository;

    public MessageService(MessageRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public MessageEntity saveMessage(UserEntity sender, UserEntity receiver, String text) {
        return repository.save(new MessageEntity(sender, receiver, text));
    }

    @Transactional(readOnly = true)
    public List<MessageEntity> getDialog(UserEntity u1, UserEntity u2) {
        return repository.findDialog(u1, u2);
    }

    @Transactional(readOnly = true)
    public List<MessageEntity> search(UserEntity u1, UserEntity u2, String text) {
        return repository.searchMessages(u1, u2, text);
    }
}