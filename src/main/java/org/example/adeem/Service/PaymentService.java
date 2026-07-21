package org.example.adeem.Service;

import lombok.RequiredArgsConstructor;
import org.example.adeem.API.APIException;
import org.example.adeem.DTO.IN.InitiatePaymentDTO;
import org.example.adeem.DTO.OUT.PaymentResponseDTO;
import org.example.adeem.Enums.AppointmentStatus;
import org.example.adeem.Enums.PaymentStatus;
import org.example.adeem.Model.Appointment;
import org.example.adeem.Model.DoctorProfile;
import org.example.adeem.Model.Payment;
import org.example.adeem.Repository.AppointmentRepository;
import org.example.adeem.Repository.DoctorProfileRepository;
import org.example.adeem.Repository.PaymentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorProfileRepository doctorProfileRepository;

    // ==================== بدء عملية الدفع ====================
    @Transactional
    public PaymentResponseDTO initiatePayment(String patientEmail, InitiatePaymentDTO dto) {

        Appointment appointment = appointmentRepository.findById(dto.getAppointmentId())
                .orElseThrow(() -> new APIException("Appointment not found"));

        // تأكد إن المريض اللي يبدأ الدفع هو صاحب الموعد فعلاً (نفس مبدأ حماية IDOR)
        if (!appointment.getPatient().getEmail().equals(patientEmail)) {
            throw new APIException("You are not authorized to pay for this appointment");
        }

        if (appointment.getStatus() != AppointmentStatus.PENDING_PAYMENT) {
            throw new APIException("This appointment is not awaiting payment");
        }

        // لو فيه محاولة دفع سابقة لنفس الموعد، منرجع نفس السجل بدل ما ننشئ وحدة جديدة
        Payment existing = paymentRepository.findByAppointmentId(appointment.getId()).orElse(null);
        if (existing != null && existing.getStatus() == PaymentStatus.PENDING) {
            return toResponseDTO(existing);
        }

        DoctorProfile doctorProfile = doctorProfileRepository.findByUserId(appointment.getDoctor().getId())
                .orElseThrow(() -> new APIException("Doctor profile not found"));

        Payment payment = new Payment();
        payment.setAppointment(appointment);
        payment.setAmount(doctorProfile.getPricePerSession());
        payment.setTransactionReference(generateTransactionReference());
        payment.setStatus(PaymentStatus.PENDING);

        paymentRepository.save(payment);

        // TODO: هنا يجي التكامل الفعلي مع Moyasar/Tap لاحقاً
        // مثال: MoyasarResponse response = moyasarClient.createPayment(payment.getAmount(), ...);
        // payment.setTransactionReference(response.getId());

        return toResponseDTO(payment);
    }

    // ==================== تأكيد الدفع (Webhook أو Callback من البوابة) ====================
    @Transactional
    public void confirmPayment(String transactionReference) {

        Payment payment = paymentRepository.findByTransactionReference(transactionReference)
                .orElseThrow(() -> new APIException("Payment not found"));

        if (payment.getStatus() == PaymentStatus.PAID) {
            throw new APIException("Payment already confirmed");
        }

        payment.setStatus(PaymentStatus.PAID);
        payment.setPaidAt(java.time.LocalDateTime.now());
        paymentRepository.save(payment);

        Appointment appointment = payment.getAppointment();
        appointment.setStatus(AppointmentStatus.CONFIRMED);

        // TODO: لو consultationType == CALL، هنا نستدعي Zoom API ونحفظ meetingLink
        // appointment.setMeetingLink(zoomService.createMeeting(...));

        appointmentRepository.save(appointment);
    }

    // ==================== محاكاة رقم مرجعي مؤقت (بديل مؤقت لحد التكامل الفعلي) ====================
    private String generateTransactionReference() {
        return "MOCK-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }

    private PaymentResponseDTO toResponseDTO(Payment payment) {
        return new PaymentResponseDTO(
                payment.getId(),
                payment.getAmount(),
                payment.getTransactionReference(),
                payment.getStatus(),
                "https://mock-checkout.adeem.sa/" + payment.getTransactionReference() // رابط وهمي للاختبار
        );
    }
}