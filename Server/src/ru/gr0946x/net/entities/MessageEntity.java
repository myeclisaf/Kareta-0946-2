package ru.gr0946x.net.entities;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class MessageEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 🔥 Изменили на EAGER — пользователи будут загружаться сразу
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "sender_id", nullable = false)
    private UserEntity sender;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "receiver_id")
    private UserEntity receiver;

    @Column(name = "text", nullable = false, length = 2000)
    private String text;

    @Column(name = "CREATEDAT", nullable = true)
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "RECEIVED", nullable = true)
    private Boolean received = false;

    public MessageEntity() {}

    public MessageEntity(UserEntity sender, UserEntity receiver, String text) {
        this.sender = sender;
        this.receiver = receiver;
        this.text = text;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public UserEntity getSender() { return sender; }
    public void setSender(UserEntity sender) { this.sender = sender; }
    public UserEntity getReceiver() { return receiver; }
    public void setReceiver(UserEntity receiver) { this.receiver = receiver; }
    public String getText() { return text; }
    public void setText(String text) { this.text = text; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public Boolean getReceived() { return received; }
    public void setReceived(Boolean received) { this.received = received; }
}