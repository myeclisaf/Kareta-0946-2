package repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import entities.MessageEntity;
import entities.UserEntity;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<MessageEntity, Long> {

    @Query("""
        select m from MessageEntity m
        where
        (m.sender = :u1 and m.receiver = :u2)
        or
        (m.sender = :u2 and m.receiver = :u1)
        order by m.createdAt asc
    """)
    List<MessageEntity> findDialog(
            @Param("u1") UserEntity u1,
            @Param("u2") UserEntity u2
    );

    @Query("""
        select m from MessageEntity m
        where
        (
            (m.sender = :u1 and m.receiver = :u2)
            or
            (m.sender = :u2 and m.receiver = :u1)
        )
        and lower(m.text) like lower(concat('%', :text, '%'))
        order by m.createdAt asc
    """)
    List<MessageEntity> searchMessages(
            @Param("u1") UserEntity u1,
            @Param("u2") UserEntity u2,
            @Param("text") String text
    );
}