package org.example.adeem.Controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.adeem.DTO.IN.SendMessageDTO;
import org.example.adeem.DTO.OUT.MessageResponseDTO;
import org.example.adeem.Service.MessageService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageController {

    private final MessageService messageService;

    // ==================== إرسال رسالة (REST - يحفظ وينشر عبر WebSocket بنفس الوقت) ====================
    @PostMapping
    public ResponseEntity<MessageResponseDTO> sendMessage(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SendMessageDTO dto) {

        MessageResponseDTO response = messageService.sendMessage(userDetails.getUsername(), dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // ==================== جلب سجل المحادثة ====================
    @GetMapping("/{appointmentId}")
    public ResponseEntity<List<MessageResponseDTO>> getConversation(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long appointmentId) {

        return ResponseEntity.ok(messageService.getConversation(userDetails.getUsername(), appointmentId));
    }
}