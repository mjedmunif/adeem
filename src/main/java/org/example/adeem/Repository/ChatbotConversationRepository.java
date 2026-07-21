package org.example.adeem.Repository;

import org.example.adeem.Model.ChatbotConversation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatbotConversationRepository extends JpaRepository<ChatbotConversation, Long> {
    List<ChatbotConversation> findByUserId(Long userId);
}