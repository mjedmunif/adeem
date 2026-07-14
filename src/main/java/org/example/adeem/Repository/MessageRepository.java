package org.example.adeem.Repository;

import org.example.adeem.Model.Message;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MessageRepository extends JpaRepository<Message,Long> {
    Message findMessageById(Long id);
}
