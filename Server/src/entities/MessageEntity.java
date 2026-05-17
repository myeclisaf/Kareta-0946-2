package entities;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class MessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private UserEntity sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id")
    private UserEntity receiver;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String text;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private boolean received;

    public MessageEntity() {
    }

    public MessageEntity(
            UserEntity sender,
            UserEntity receiver,
            String text
    ) {
        this.sender = sender;
        this.receiver = receiver;
        this.text = text;
        this.createdAt = LocalDateTime.now();
        this.received = false;
    }

    public Long getId() {
        return id;
    }

    public UserEntity getSender() {
        return sender;
    }

    public void setSender(UserEntity sender) {
        this.sender = sender;
    }

    public UserEntity getReceiver() {
        return receiver;
    }

    public void setReceiver(UserEntity receiver) {
        this.receiver = receiver;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public boolean isReceived() {
        return received;
    }

    public void setReceived(boolean received) {
        this.received = received;
    }
}
