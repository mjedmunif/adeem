package org.example.adeem.Repository;

import org.example.adeem.Enums.VerificationStatus;
import org.example.adeem.Model.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorProfileRepository extends JpaRepository<DoctorProfile,Long> {


    Optional<DoctorProfile> findByUserEmail(String email); // مفيدة لـ /doctors/me

    boolean existsByLicenseNumber(String licenseNumber);

    List<DoctorProfile> findByVerificationStatus(VerificationStatus status);

    List<DoctorProfile> findBySpecialtyAndVerificationStatus(String specialty, VerificationStatus status);

    Optional<DoctorProfile> findByUserId(Long userId);
}
