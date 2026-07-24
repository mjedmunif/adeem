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
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ChatbotService {

    private final ChatbotConversationRepository conversationRepository;
    private final ChatbotMessageRepository messageRepository;
    private final UserRepository userRepository;
    private final ClaudeApiService claudeApiService;

    private static final Set<String> ALLOWED_IMAGE_TYPES = Set.of(
            "image/jpeg", "image/png", "image/webp", "image/gif"
    );
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024;

    @Transactional
    public ChatbotMessageResponseDTO ask(String userEmail, ChatbotAskDTO dto) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new APIException("User not found"));

        ChatbotConversation conversation = resolveConversation(user, dto.getConversationId());

        ChatbotMessage userMessage = new ChatbotMessage();
        userMessage.setConversation(conversation);
        userMessage.setSenderType(SenderType.USER);
        userMessage.setContent(dto.getContent());
        userMessage.setHasAttachment(false);
        messageRepository.save(userMessage);

        List<ChatbotMessage> history = messageRepository
                .findByConversationIdOrderByCreatedAtAsc(conversation.getId());

        List<Map<String, Object>> claudeMessages = buildClaudeMessages(history, null, null);

        String botReply = claudeApiService.sendMessage(claudeMessages);

        ChatbotMessage botMessage = saveBotReply(conversation, botReply);

        return toResponseDTO(conversation.getId(), botMessage);
    }

    @Transactional
    public ChatbotMessageResponseDTO askWithImage(
            String userEmail, Long conversationId, String content, MultipartFile image) {

        if (image == null || image.isEmpty()) {
            throw new APIException("الرجاء إرفاق صورة صالحة");
        }

        String contentType = image.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new APIException("صيغة الملف غير مدعومة. الرجاء رفع صورة بصيغة JPEG أو PNG أو WEBP");
        }

        if (image.getSize() > MAX_FILE_SIZE) {
            throw new APIException("حجم الصورة كبير جداً. الحد الأقصى المسموح 5 ميجابايت");
        }

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new APIException("User not found"));

        ChatbotConversation conversation = resolveConversation(user, conversationId);

        String imageBase64;
        try {
            imageBase64 = Base64.getEncoder().encodeToString(image.getBytes());
        } catch (Exception e) {
            throw new APIException("تعذر معالجة الصورة، حاول رفعها مرة أخرى");
        }

        ChatbotMessage userMessage = new ChatbotMessage();
        userMessage.setConversation(conversation);
        userMessage.setSenderType(SenderType.USER);
        userMessage.setContent(content != null && !content.isBlank() ? content : "[صورة مرفقة]");
        userMessage.setHasAttachment(true);
        messageRepository.save(userMessage);

        List<ChatbotMessage> history = messageRepository
                .findByConversationIdOrderByCreatedAtAsc(conversation.getId());

        List<Map<String, Object>> claudeMessages = buildClaudeMessages(history, imageBase64, contentType);

        String botReply = claudeApiService.sendMessage(claudeMessages);

        ChatbotMessage botMessage = saveBotReply(conversation, botReply);

        return toResponseDTO(conversation.getId(), botMessage);
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

    private ChatbotConversation resolveConversation(User user, Long conversationId) {
        if (conversationId != null) {
            ChatbotConversation conversation = conversationRepository.findById(conversationId)
                    .orElseThrow(() -> new APIException("Conversation not found"));

            if (!conversation.getUser().getId().equals(user.getId())) {
                throw new APIException("You are not authorized to access this conversation");
            }
            return conversation;
        }

        ChatbotConversation conversation = new ChatbotConversation();
        conversation.setUser(user);
        return conversationRepository.save(conversation);
    }

    private List<Map<String, Object>> buildClaudeMessages(
            List<ChatbotMessage> history, String imageBase64, String imageMediaType) {

        List<Map<String, Object>> claudeMessages = new ArrayList<>();

        for (int i = 0; i < history.size(); i++) {
            ChatbotMessage msg = history.get(i);
            String role = msg.getSenderType() == SenderType.USER ? "user" : "assistant";
            boolean isLastMessage = (i == history.size() - 1);

            if (isLastMessage && imageBase64 != null) {
                List<Map<String, Object>> contentBlocks = new ArrayList<>();

                contentBlocks.add(Map.of(
                        "type", "image",
                        "source", Map.of(
                                "type", "base64",
                                "media_type", imageMediaType,
                                "data", imageBase64
                        )
                ));

                contentBlocks.add(Map.of(
                        "type", "text",
                        "text", msg.getContent()
                ));

                claudeMessages.add(Map.of("role", role, "content", contentBlocks));
            } else {
                claudeMessages.add(Map.of("role", role, "content", msg.getContent()));
            }
        }

        return claudeMessages;
    }

    private ChatbotMessage saveBotReply(ChatbotConversation conversation, String botReply) {
        ChatbotMessage botMessage = new ChatbotMessage();
        botMessage.setConversation(conversation);
        botMessage.setSenderType(SenderType.BOT);
        botMessage.setContent(botReply);
        botMessage.setHasAttachment(false);
        messageRepository.save(botMessage);
        return botMessage;
    }

    private ChatbotMessageResponseDTO toResponseDTO(Long conversationId, ChatbotMessage botMessage) {
        return new ChatbotMessageResponseDTO(
                conversationId,
                SenderType.BOT,
                botMessage.getContent(),
                botMessage.getCreatedAt()
        );
    }
}