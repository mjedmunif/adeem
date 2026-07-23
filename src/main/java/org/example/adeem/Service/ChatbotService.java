package org.example.adeem.Service;

import lombok.RequiredArgsConstructor;
import org.example.adeem.API.APIException;
import org.example.adeem.DTO.IN.ChatbotAskDTO;
import org.example.adeem.DTO.OUT.ChatbotMessageResponseDTO;
import org.example.adeem.Enums.SenderType;
import org.example.adeem.Model.ChatbotConversation;
import org.example.adeem.Model.ChatbotMessage;
import org.example.adeem.Model.User;
import org.example.adeem.Repository.ChatbotConversationRepository;
import org.example.adeem.Repository.ChatbotMessageRepository;
import org.example.adeem.Repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final ChatbotConversationRepository conversationRepository;
    private final ChatbotMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ClaudeApiService claudeApiService;

    @Transactional
    public ChatbotMessageResponseDTO ask(String userEmail, ChatbotAskDTO dto) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new APIException("User not found"));

        // 1. جيب المحادثة الموجودة أو أنشئ وحدة جديدة
        ChatbotConversation conversation;
        if (dto.getConversationId() != null) {
            conversation = conversationRepository.findById(dto.getConversationId())
                    .orElseThrow(() -> new APIException("Conversation not found"));

            if (!conversation.getUser().getId().equals(user.getId())) {
                throw new APIException("You are not authorized to access this conversation");
            }
        } else {
            conversation = new ChatbotConversation();
            conversation.setUser(user);
            conversationRepository.save(conversation);
        }

        // 2. احفظ رسالة المستخدم
        ChatbotMessage userMessage = new ChatbotMessage();
        userMessage.setConversation(conversation);
        userMessage.setSenderType(SenderType.USER);
        userMessage.setContent(dto.getContent());
        messageRepository.save(userMessage);

        // 3. جهّز تاريخ المحادثة كامل عشان يفهم البوت السياق
        List<ChatbotMessage> history = messageRepository
                .findByConversationIdOrderByCreatedAtAsc(conversation.getId());

        List<Map<String, String>> claudeMessages = new ArrayList<>();
        for (ChatbotMessage msg : history) {
            String role = msg.getSenderType() == SenderType.USER ? "user" : "assistant";
            claudeMessages.add(Map.of("role", role, "content", msg.getContent()));
        }

        // 4. استدعِ Claude API
        String botReply = claudeApiService.sendMessage(claudeMessages);

        // 5. احفظ رد البوت
        ChatbotMessage botMessage = new ChatbotMessage();
        botMessage.setConversation(conversation);
        botMessage.setSenderType(SenderType.BOT);
        botMessage.setContent(botReply);
        messageRepository.save(botMessage);

        return new ChatbotMessageResponseDTO(
                conversation.getId(),
                SenderType.BOT,
                botReply,
                botMessage.getCreatedAt()
        );
    }

    @Transactional(readOnly = true)
    public List<ChatbotMessageResponseDTO> getConversationHistory(String userEmail, Long conversationId) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new APIException("User not found"));

        ChatbotConversation conversation = conversationRepository.findById(conversationId)
                .orElseThrow(() -> new APIException("Conversation not found"));

        if (!conversation.getUser().getId().equals(user.getId())) {
            throw new APIException("You are not authorized to access this conversation");
        }

        return messageRepository.findByConversationIdOrderByCreatedAtAsc(conversationId)
                .stream()
                .map(m -> new ChatbotMessageResponseDTO(
                        conversation.getId(), m.getSenderType(), m.getContent(), m.getCreatedAt()))
                .toList();
    }
}