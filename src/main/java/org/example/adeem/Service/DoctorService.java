package org.example.adeem.Service;

import lombok.RequiredArgsConstructor;
import org.example.adeem.API.APIException;
import org.example.adeem.DTO.IN.DoctorRegisterDTO;
import org.example.adeem.DTO.IN.DoctorVerificationDTO;
import org.example.adeem.DTO.OUT.DoctorPublicResponseDTO;
import org.example.adeem.DTO.OUT.DoctorResponseDTO;
import org.example.adeem.Enums.Role;
import org.example.adeem.Enums.VerificationStatus;
import org.example.adeem.Model.DoctorProfile;
import org.example.adeem.Model.User;
import org.example.adeem.Repository.DoctorProfileRepository;
import org.example.adeem.Repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class DoctorService {

    private final UserRepository userRepository;
    private final DoctorProfileRepository doctorProfileRepository;
    private final PasswordEncoder passwordEncoder;

    // ==================== تسجيل طبيب جديد ====================
    @Transactional
    public void register(DoctorRegisterDTO dto) {

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new APIException("Email is already registered");
        }

        if (doctorProfileRepository.existsByLicenseNumber(dto.getLicenseNumber())) {
            throw new APIException("License number is already registered");
        }

        // 1. أنشئ الـ User بـ role = DOCTOR
        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setFullName(dto.getFullName());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setRole(Role.DOCTOR);

        userRepository.save(user);

        // 2. أنشئ الـ DoctorProfile المرتبط فيه، حالته PENDING دايماً
        DoctorProfile profile = new DoctorProfile();
        profile.setUser(user);
        profile.setSpecialty(dto.getSpecialty());
        profile.setPricePerSession(dto.getPricePerSession());
        profile.setLicenseNumber(dto.getLicenseNumber());
        profile.setLicenseDocumentUrl(dto.getLicenseDocumentUrl());
        profile.setBio(dto.getBio());
        profile.setVerificationStatus(VerificationStatus.PENDING); // إجباري دايماً، بغض النظر عن أي حاجة

        doctorProfileRepository.save(profile);
    }

    // ==================== بروفايلي أنا (الطبيب نفسه) ====================
    @Transactional(readOnly = true)
    public DoctorResponseDTO getMyProfile(String email) {
        DoctorProfile profile = doctorProfileRepository.findByUserEmail(email)
                .orElseThrow(() -> new APIException("Doctor profile not found"));

        return toResponseDTO(profile);
    }

    // ==================== للمرضى: قائمة الأطباء الموثقين بس ====================
    @Transactional(readOnly = true)
    public List<DoctorPublicResponseDTO> getVerifiedDoctors() {
        return doctorProfileRepository.findByVerificationStatus(VerificationStatus.VERIFIED)
                .stream()
                .map(p -> new DoctorPublicResponseDTO(
                        p.getId(),
                        p.getUser().getFullName(),
                        p.getSpecialty(),
                        p.getPricePerSession(),
                        p.getBio()
                ))
                .toList();
    }

    // ==================== للأدمن: قائمة الطلبات المعلقة ====================
    @Transactional(readOnly = true)
    public List<DoctorResponseDTO> getPendingDoctors() {
        return doctorProfileRepository.findByVerificationStatus(VerificationStatus.PENDING)
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // ==================== للأدمن: قبول أو رفض طبيب ====================
    @Transactional
    public void updateVerificationStatus(Long doctorProfileId, DoctorVerificationDTO dto) {
        DoctorProfile profile = doctorProfileRepository.findById(doctorProfileId)
                .orElseThrow(() -> new APIException("Doctor profile not found"));

        profile.setVerificationStatus(dto.getStatus());
        doctorProfileRepository.save(profile);
    }

    // ==================== Helper ====================
    private DoctorResponseDTO toResponseDTO(DoctorProfile profile) {
        User user = profile.getUser();
        return new DoctorResponseDTO(
                profile.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhoneNumber(),
                profile.getSpecialty(),
                profile.getPricePerSession(),
                profile.getLicenseNumber(),
                profile.getVerificationStatus(),
                profile.getBio()
        );
    }
}