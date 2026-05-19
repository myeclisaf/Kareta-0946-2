package ru.gr0946x.net.repositories;

import ru.gr0946x.net.entities.MessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    @Query("""
        SELECT m FROM MessageEntity m 
        LEFT JOIN FETCH m.sender 
        WHERE m.receiver IS NULL 
        ORDER BY m.createdAt ASC
        """)
    List<MessageEntity> findGeneralChatHistory();

    @Query("""
        SELECT m FROM MessageEntity m 
        LEFT JOIN FETCH m.sender 
        LEFT JOIN FETCH m.receiver 
        WHERE (m.sender.id = :u1Id AND m.receiver.id = :u2Id) 
           OR (m.sender.id = :u2Id AND m.receiver.id = :u1Id) 
        ORDER BY m.createdAt ASC
        """)
    List<MessageEntity> findDialog(@Param("u1Id") Long u1Id, @Param("u2Id") Long u2Id);

    @Query("""
        SELECT m FROM MessageEntity m 
        LEFT JOIN FETCH m.sender 
        LEFT JOIN FETCH m.receiver 
        WHERE ((m.sender.id = :u1Id AND m.receiver.id = :u2Id) 
           OR (m.sender.id = :u2Id AND m.receiver.id = :u1Id))
           AND LOWER(m.text) LIKE LOWER(CONCAT('%', :text, '%'))
        ORDER BY m.createdAt ASC
        """)
    List<MessageEntity> searchMessages(@Param("u1Id") Long u1Id,
                                       @Param("u2Id") Long u2Id,
                                       @Param("text") String text);
}