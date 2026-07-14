package org.example.adeem.Repository;

import org.example.adeem.Model.DoctorProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorProfileRepository extends JpaRepository<DoctorProfile,Long> {

    DoctorProfile findDoctorProfileById(Long id);
}
