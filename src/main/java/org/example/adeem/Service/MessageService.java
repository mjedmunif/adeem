package org.example.adeem.Service;

import lombok.RequiredArgsConstructor;
import org.example.adeem.API.APIException;
import org.example.adeem.DTO.IN.SendMessageDTO;
import org.example.adeem.DTO.OUT.MessageResponseDTO;
import org.example.adeem.Enums.AppointmentStatus;
import org.example.adeem.Enums.ConsultationType;
import org.example.adeem.Model.Appointment;
import org.example.adeem.Model.Message;
import org.example.adeem.Model.User;
import org.example.adeem.Repository.AppointmentRepository;
import org.example.adeem.Repository.MessageRepository;
import org.example.adeem.Repository.UserRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final AppointmentRepository appointmentRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate; // يرسل الرسالة لحظياً عبر WebSocket

    // ==================== إرسال رسالة ====================
    @Transactional
    public MessageResponseDTO sendMessage(String senderEmail, SendMessageDTO dto) {

        User sender = userRepository.findByEmail(senderEmail)
                .orElseThrow(() -> new APIException("User not found"));

        Appointment appointment = appointmentRepository.findById(dto.getAppointmentId())
                .orElseThrow(() -> new APIException("Appointment not found"));

        // تحقق 1: الشات يشتغل بس لو نوع الاستشارة CHAT
        if (appointment.getConsultationType() != ConsultationType.CHAT) {
            throw new APIException("This appointment does not support text chat");
        }

        // تحقق 2: الموعد لازم يكون مؤكد (تم الدفع)
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new APIException("Chat is only available after payment confirmation");
        }

        // تحقق 3: المرسل لازم يكون المريض أو الطبيب المرتبطين بهذا الموعد فقط (حماية IDOR)
        boolean isPatient = appointment.getPatient().getId().equals(sender.getId());
        boolean isDoctor = appointment.getDoctor().getId().equals(sender.getId());

        if (!isPatient && !isDoctor) {
            throw new APIException("You are not authorized to send messages in this appointment");
        }

        Message message = new Message();
        message.setAppointment(appointment);
        message.setSender(sender);
        message.setContent(dto.getContent());
        message.setAttachmentUrl(dto.getAttachmentUrl());

        messageRepository.save(message);

        MessageResponseDTO responseDTO = toResponseDTO(message);

        // نرسلها لحظياً لكل المشتركين بهذا الموعد عبر WebSocket
        messagingTemplate.convertAndSend(
                "/topic/appointment/" + appointment.getId(),
                responseDTO
        );

        return responseDTO;
    }

    // ==================== جلب سجل المحادثة كامل ====================
    @Transactional(readOnly = true)
    public List<MessageResponseDTO> getConversation(String userEmail, Long appointmentId) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> new APIException("User not found"));

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new APIException("Appointment not found"));

        boolean isPatient = appointment.getPatient().getId().equals(user.getId());
        boolean isDoctor = appointment.getDoctor().getId().equals(user.getId());

        if (!isPatient && !isDoctor) {
            throw new APIException("You are not authorized to view this conversation");
        }

        return messageRepository.findByAppointmentIdOrderBySentAtAsc(appointmentId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    private MessageResponseDTO toResponseDTO(Message m) {
        return new MessageResponseDTO(
                m.getId(),
                m.getAppointment().getId(),
                m.getSender().getId(),
                m.getSender().getFullName(),
                m.getContent(),
                m.getAttachmentUrl(),
                m.getSentAt(),
                m.isRead()
        );
    }
}