package org.example.adeem.Repository;

import org.example.adeem.Model.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment,Long> {

    Appointment findAppointmentById(Long id);
}
