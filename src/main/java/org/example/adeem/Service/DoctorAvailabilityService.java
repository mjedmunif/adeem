package org.example.adeem.Service;

import lombok.RequiredArgsConstructor;
import org.example.adeem.API.APIException;
import org.example.adeem.DTO.IN.AvailabilityCreateDTO;
import org.example.adeem.DTO.OUT.AvailabilityResponseDTO;

import org.example.adeem.Model.DoctorAvailability;
import org.example.adeem.Model.DoctorProfile;
import org.example.adeem.Repository.DoctorAvailabilityRepository;
import org.example.adeem.Repository.DoctorProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorAvailabilityService {

    private final DoctorAvailabilityRepository availabilityRepository;
    private final DoctorProfileRepository doctorProfileRepository;

    // ==================== الطبيب يضيف يوم دوام ====================
    @Transactional
    public void addAvailability(String doctorEmail, AvailabilityCreateDTO dto) {

        DoctorProfile doctor = doctorProfileRepository.findByUserEmail(doctorEmail)
                .orElseThrow(() -> new APIException("Doctor profile not found"));

        if (!dto.getEndTime().isAfter(dto.getStartTime())) {
            throw new APIException("End time must be after start time");
        }

        if (availabilityRepository.existsByDoctorIdAndDayOfWeek(doctor.getId(), dto.getDayOfWeek())) {
            throw new APIException("Availability for this day is already set");
        }

        DoctorAvailability availability = new DoctorAvailability();
        availability.setDoctor(doctor);
        availability.setDayOfWeek(dto.getDayOfWeek());
        availability.setStartTime(dto.getStartTime());
        availability.setEndTime(dto.getEndTime());

        availabilityRepository.save(availability);
    }

    // ==================== الطبيب يشوف جدوله هو ====================
    @Transactional(readOnly = true)
    public List<AvailabilityResponseDTO> getMyAvailability(String doctorEmail) {
        DoctorProfile doctor = doctorProfileRepository.findByUserEmail(doctorEmail)
                .orElseThrow(() -> new APIException("Doctor profile not found"));

        return availabilityRepository.findByDoctorId(doctor.getId())
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // ==================== المريض يشوف جدول طبيب معين (عام) ====================
    @Transactional(readOnly = true)
    public List<AvailabilityResponseDTO> getDoctorAvailability(Long doctorId) {
        return availabilityRepository.findByDoctorId(doctorId)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // ==================== حذف يوم دوام ====================
    @Transactional
    public void deleteAvailability(String doctorEmail, Long availabilityId) {
        DoctorAvailability availability = availabilityRepository.findById(availabilityId)
                .orElseThrow(() -> new APIException("Availability not found"));

        // تأكد إن الطبيب اللي يحذف هو صاحب هذا الجدول فعلاً - نفس مبدأ حماية IDOR
        if (!availability.getDoctor().getUser().getEmail().equals(doctorEmail)) {
            throw new APIException("You are not authorized to delete this availability");
        }

        availabilityRepository.delete(availability);
    }

    private AvailabilityResponseDTO toResponseDTO(DoctorAvailability a) {
        return new AvailabilityResponseDTO(
                a.getId(),
                a.getDayOfWeek(),
                a.getStartTime(),
                a.getEndTime()
        );
    }
}