package ru.gr0946x.net.services;

import ru.gr0946x.net.entities.MessageEntity;
import ru.gr0946x.net.repositories.MessageRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
public class MessageService {
    private final MessageRepository repository;

    public MessageService(MessageRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public MessageEntity saveMessage(ru.gr0946x.net.entities.UserEntity sender,
                                     ru.gr0946x.net.entities.UserEntity receiver,
                                     String text) {
        return repository.save(new MessageEntity(sender, receiver, text));
    }

    @Transactional(readOnly = true)
    public List<MessageEntity> getDialog(Long u1Id, Long u2Id) {
        return repository.findDialog(u1Id, u2Id);
    }

    @Transactional(readOnly = true)
    public List<MessageEntity> getGeneralChatHistory() {
        return repository.findGeneralChatHistory();
    }

    @Transactional(readOnly = true)
    public List<MessageEntity> search(Long u1Id, Long u2Id, String text) {
        return repository.searchMessages(u1Id, u2Id, text);
    }
}