package org.example.adeem.Repository;

import org.example.adeem.Model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MessageRepository extends JpaRepository<Message, Long> {

    List<Message> findByAppointmentIdOrderBySentAtAsc(Long appointmentId);

    long countByAppointmentIdAndIsReadFalse(Long appointmentId);
}