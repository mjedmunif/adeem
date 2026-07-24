package org.example.adeem.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.adeem.DTO.IN.ChatbotAskDTO;
import org.example.adeem.Service.ChatbotService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/v1/chatbot")
@RequiredArgsConstructor
public class ChatbotController {

    private final ChatbotService chatbotService;

    @PostMapping("/ask")
    public ResponseEntity<?> ask(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChatbotAskDTO dto) {

        return ResponseEntity.ok(chatbotService.ask(userDetails.getUsername(), dto));
    }

    @PostMapping(value = "/ask-with-image", consumes = "multipart/form-data")
    public ResponseEntity<?> askWithImage(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long conversationId,
            @RequestParam(required = false) String content,
            @RequestParam("image") MultipartFile image) {

        return ResponseEntity.ok(
                chatbotService.askWithImage(userDetails.getUsername(), conversationId, content, image)
        );
    }

    @GetMapping("/{conversationId}")
    public ResponseEntity<?> getHistory(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long conversationId) {

        return ResponseEntity.ok(chatbotService.getConversationHistory(userDetails.getUsername(), conversationId));
    }
}