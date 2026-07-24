package org.example.adeem.Model;

import jakarta.persistence.*;
import lombok.*;
import org.example.adeem.Enums.Role;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Table(name = "users")
public class User {


    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 150)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(name = "full_name", nullable = false, length = 100)
    private String fullName;

    @Column(name = "phone_number", length = 20)
    private String phoneNumber;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();


    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private DoctorProfile doctorProfile;

    @OneToMany(mappedBy = "patient", cascade = CascadeType.ALL)
    private List<Appointment> patientAppointments;

    @OneToMany(mappedBy = "doctor", cascade ={ CascadeType.MERGE , CascadeType.PERSIST})
    private List<Appointment> doctorAppointments;
}
