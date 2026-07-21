package org.example.adeem.Service;

import lombok.RequiredArgsConstructor;
import org.example.adeem.API.APIException;
import org.example.adeem.DTO.OUT.AppointmentResponseDTO;
import org.example.adeem.DTO.IN.BookAppointmentDTO;
import org.example.adeem.Enums.AppointmentStatus;
import org.example.adeem.Enums.VerificationStatus;
import org.example.adeem.Model.Appointment;
import org.example.adeem.Model.DoctorAvailability;
import org.example.adeem.Model.DoctorProfile;
import org.example.adeem.Model.User;
import org.example.adeem.Repository.AppointmentRepository;
import org.example.adeem.Repository.DoctorAvailabilityRepository;
import org.example.adeem.Repository.DoctorProfileRepository;
import org.example.adeem.Repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private static final int SLOT_DURATION_MINUTES = 30;

    private final AppointmentRepository appointmentRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final DoctorAvailabilityRepository availabilityRepository;
    private final UserRepository userRepository;

    // ==================== حساب الـ Slots الفاضية ليوم معين ====================
    @Transactional(readOnly = true)
    public List<LocalTime> getAvailableSlots(Long doctorId, LocalDate date) {

        DayOfWeek day = date.getDayOfWeek();

        DoctorAvailability availability = availabilityRepository
                .findByDoctorIdAndDayOfWeek(doctorId, day)
                .orElseThrow(() -> new APIException("Doctor is not available on this day"));

        // 1. نولّد كل الـ slots الممكنة بين بداية ونهاية دوام
        List<LocalTime> allSlots = generateSlots(availability.getStartTime(), availability.getEndTime());

        // 2. نجيب المواعيد المحجوزة (غير الملغية) بهذا اليوم لهذا الطبيب
        LocalDateTime dayStart = date.atStartOfDay();
        LocalDateTime dayEnd = date.atTime(LocalTime.MAX);

        List<LocalTime> bookedSlots = appointmentRepository
                .findByDoctorIdAndAppointmentDateBetweenAndStatusNot(
                        doctorId, dayStart, dayEnd, AppointmentStatus.CANCELLED)
                .stream()
                .map(a -> a.getAppointmentDate().toLocalTime())
                .toList();

        // 3. نطرح المحجوز من الكل
        allSlots.removeAll(bookedSlots);

        return allSlots;
    }

    private List<LocalTime> generateSlots(LocalTime start, LocalTime end) {
        List<LocalTime> slots = new ArrayList<>();
        LocalTime current = start;

        while (current.plusMinutes(SLOT_DURATION_MINUTES).compareTo(end) <= 0) {
            slots.add(current);
            current = current.plusMinutes(SLOT_DURATION_MINUTES);
        }

        return slots;
    }

    // ==================== حجز موعد ====================
    @Transactional
    public void bookAppointment(String patientEmail, BookAppointmentDTO dto) {

        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new APIException("Patient not found"));

        DoctorProfile doctorProfile = doctorProfileRepository.findById(dto.getDoctorId())
                .orElseThrow(() -> new APIException("Doctor not found"));

        // تأكد الطبيب موثق
        if (doctorProfile.getVerificationStatus() != VerificationStatus.VERIFIED) {
            throw new APIException("This doctor is not verified yet");
        }

        User doctor = doctorProfile.getUser();

        // تأكد الوقت المطلوب أصلاً ضمن دوام الطبيب بهذا اليوم
        LocalDate requestedDate = dto.getAppointmentDate().toLocalDate();
        LocalTime requestedTime = dto.getAppointmentDate().toLocalTime();

        List<LocalTime> availableSlots = getAvailableSlots(dto.getDoctorId(), requestedDate);

        if (!availableSlots.contains(requestedTime)) {
            throw new APIException("This time slot is not available");
        }

        // تحقق إضافي مباشر (حماية إضافية ضد race condition بسيطة)
        boolean alreadyBooked = appointmentRepository.existsByDoctorIdAndAppointmentDateAndStatusNot(
                dto.getDoctorId(), dto.getAppointmentDate(), AppointmentStatus.CANCELLED);

        if (alreadyBooked) {
            throw new APIException("This time slot was just booked, please choose another");
        }

        Appointment appointment = new Appointment();
        appointment.setPatient(patient);
        appointment.setDoctor(doctor);
        appointment.setAppointmentDate(dto.getAppointmentDate());
        appointment.setConsultationType(dto.getConsultationType());
        appointment.setStatus(AppointmentStatus.PENDING_PAYMENT);

        appointmentRepository.save(appointment);
    }

    // ==================== مواعيدي (مريض) ====================
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getMyAppointmentsAsPatient(String patientEmail) {
        User patient = userRepository.findByEmail(patientEmail)
                .orElseThrow(() -> new APIException("Patient not found"));

        return appointmentRepository.findByPatientId(patient.getId())
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // ==================== مواعيدي (طبيب) ====================
    @Transactional(readOnly = true)
    public List<AppointmentResponseDTO> getMyAppointmentsAsDoctor(String doctorEmail) {
        User doctor = userRepository.findByEmail(doctorEmail)
                .orElseThrow(() -> new APIException("Doctor not found"));

        return appointmentRepository.findByDoctorId(doctor.getId())
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // ==================== إلغاء موعد ====================
    @Transactional
    public void cancelAppointment(String userEmail, Long appointmentId) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new APIException("Appointment not found"));

        boolean isPatient = appointment.getPatient().getEmail().equals(userEmail);
        boolean isDoctor = appointment.getDoctor().getEmail().equals(userEmail);

        if (!isPatient && !isDoctor) {
            throw new APIException("You are not authorized to cancel this appointment");
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointmentRepository.save(appointment);
    }

    private AppointmentResponseDTO toResponseDTO(Appointment a) {
        return new AppointmentResponseDTO(
                a.getId(),
                a.getDoctor().getFullName(),
                a.getPatient().getFullName(),
                a.getAppointmentDate(),
                a.getStatus(),
                a.getConsultationType(),
                a.getMeetingLink()
        );
    }
}