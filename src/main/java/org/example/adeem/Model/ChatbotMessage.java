package org.example.adeem.Model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.example.adeem.Enums.SenderType;

import java.time.LocalDateTime;

@Entity
@Table(name = "chatbot_messages")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatbotMessage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "conversation_id", nullable = false)
    private ChatbotConversation conversation;

    @Enumerated(EnumType.STRING)
    @Column(name = "sender_type", nullable = false, length = 10)
    private SenderType senderType;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}