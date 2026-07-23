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
import org.example.adeem.Enums.ConsultationType;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final ZoomApiService zoomApiService;

    // TODO: فعّل هذا السطر يوم يجهز السجل التجاري / وثيقة العمل الحر + الحساب البنكي التجاري
    // private final MoyasarApiService moyasarApiService;

    // ==================== بدء عملية الدفع ====================
    @Transactional
    public PaymentResponseDTO initiatePayment(String patientEmail, InitiatePaymentDTO dto) {

        Appointment appointment = appointmentRepository.findById(dto.getAppointmentId())
                .orElseThrow(() -> new APIException("Appointment not found"));

        if (!appointment.getPatient().getEmail().equals(patientEmail)) {
            throw new APIException("You are not authorized to pay for this appointment");
        }

        if (appointment.getStatus() != AppointmentStatus.PENDING_PAYMENT) {
            throw new APIException("This appointment is not awaiting payment");
        }

        Payment existing = paymentRepository.findByAppointmentId(appointment.getId()).orElse(null);
        if (existing != null && existing.getStatus() == PaymentStatus.PENDING) {
            return toResponseDTO(existing);
        }

        DoctorProfile doctorProfile = doctorProfileRepository.findByUserId(appointment.getDoctor().getId())
                .orElseThrow(() -> new APIException("Doctor profile not found"));

        Payment payment = new Payment();
        payment.setAppointment(appointment);
        payment.setAmount(doctorProfile.getPricePerSession());
        payment.setStatus(PaymentStatus.PENDING);

        // ====================  النشط حالياً: Mock (بدون بوابة دفع حقيقية) ====================
        payment.setTransactionReference(generateTransactionReference());
        paymentRepository.save(payment);
        return toResponseDTO(payment);

        // ====================  معطّل مؤقتاً: Moyasar الفعلي (جاهز، ينتظر السجل التجاري) ====================
        /*
        Map<String, String> invoice = moyasarApiService.createInvoice(
                doctorProfile.getPricePerSession(),
                "استشارة طبية - " + appointment.getDoctor().getFullName(),
                "http://localhost:8080/api/v1/payments/callback"
        );

        payment.setTransactionReference(invoice.get("invoiceId"));
        paymentRepository.save(payment);

        return new PaymentResponseDTO(
                payment.getId(),
                payment.getAmount(),
                payment.getTransactionReference(),
                payment.getStatus(),
                invoice.get("checkoutUrl")
        );
        */
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

        if (appointment.getConsultationType() == ConsultationType.CALL) {
            String topic = "استشارة طبية - " + appointment.getDoctor().getFullName();
            Map<String, String> meeting = zoomApiService.createMeeting(topic, appointment.getAppointmentDate());
            appointment.setMeetingId(meeting.get("meetingId"));
            appointment.setMeetingLink(meeting.get("joinUrl"));
        }

        appointmentRepository.save(appointment);
    }

    // ==================== محاكاة رقم مرجعي مؤقت (Mock) ====================
    private String generateTransactionReference() {
        return "MOCK-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }

    private PaymentResponseDTO toResponseDTO(Payment payment) {
        return new PaymentResponseDTO(
                payment.getId(),
                payment.getAmount(),
                payment.getTransactionReference(),
                payment.getStatus(),
                "https://mock-checkout.adeem.sa/" + payment.getTransactionReference()
        );
    }
}