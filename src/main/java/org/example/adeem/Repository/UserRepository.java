package org.example.adeem.Repository;

import org.example.adeem.Model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User,Long> {

    User findUserById(Long id);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);
}
