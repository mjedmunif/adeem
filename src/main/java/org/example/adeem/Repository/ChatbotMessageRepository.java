package org.example.adeem.Repository;

import org.example.adeem.Model.ChatbotMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
@Repository
public interface ChatbotMessageRepository extends JpaRepository<ChatbotMessage, Long> {
    List<ChatbotMessage> findByConversationIdOrderByCreatedAtAsc(Long conversationId);
}