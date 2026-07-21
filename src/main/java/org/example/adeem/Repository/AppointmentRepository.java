package org.example.adeem.Repository;

import org.example.adeem.Model.Appointment;
import org.example.adeem.Enums.AppointmentStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    List<Appointment> findByPatientId(Long patientId);

    List<Appointment> findByDoctorId(Long doctorId);

    List<Appointment> findByDoctorIdAndAppointmentDateBetween(
            Long doctorId, LocalDateTime start, LocalDateTime end);

    // يجيب كل المواعيد المحجوزة (غير الملغية) لطبيب معين بيوم معين - لحساب الـ slots الفاضية
    List<Appointment> findByDoctorIdAndAppointmentDateBetweenAndStatusNot(
            Long doctorId, LocalDateTime start, LocalDateTime end, AppointmentStatus excludedStatus);

    // التحقق الدقيق: هل فيه موعد بنفس التاريخ والوقت بالضبط لهذا الطبيب (غير ملغي)؟
    boolean existsByDoctorIdAndAppointmentDateAndStatusNot(
            Long doctorId, LocalDateTime appointmentDate, AppointmentStatus excludedStatus);
}