package org.example.adeem.Service;

import lombok.RequiredArgsConstructor;
import org.example.adeem.API.APIException;
import org.example.adeem.DTO.IN.UserRegisterDTO;
import org.example.adeem.DTO.IN.UserUpdateDTO;
import org.example.adeem.DTO.OUT.UserResponseDTO;
import org.example.adeem.Enums.Role;
import org.example.adeem.Model.User;
import org.example.adeem.Repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    // PasswordEncoder intentionally omitted for now - to be added later with Spring Security setup

    // ==================== CREATE ====================
    @Transactional
    public void register(UserRegisterDTO dto) {

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new APIException("Email is already registered");
        }

        User user = new User();
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword()); // TODO: hash with PasswordEncoder once Security is set up
        user.setFullName(dto.getFullName());
        user.setPhoneNumber(dto.getPhoneNumber());
        user.setRole(Role.PATIENT); // always PATIENT on public registration - never trust client input here

        userRepository.save(user);
    }

    // ==================== READ (single) ====================
    @Transactional(readOnly = true)
    public UserResponseDTO getById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new APIException("User not found"));

        return toResponseDTO(user);
    }

    // ==================== READ (all) ====================
    @Transactional(readOnly = true)
    public List<UserResponseDTO> getAll() {
        return userRepository.findAll()
                .stream()
                .map(this::toResponseDTO)
                .toList();
    }

    // ==================== UPDATE ====================
    @Transactional
    public void update(Long id, UserUpdateDTO dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new APIException("User not found"));

        if (dto.getFullName() != null) {
            user.setFullName(dto.getFullName());
        }
        if (dto.getPhoneNumber() != null) {
            user.setPhoneNumber(dto.getPhoneNumber());
        }

        userRepository.save(user);
    }

    // ==================== DELETE ====================
    @Transactional
    public void delete(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new APIException("User not found"));

        userRepository.delete(user);
    }

    // ==================== Helper ====================
    private UserResponseDTO toResponseDTO(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getFullName(),
                user.getPhoneNumber(),
                user.getRole(),
                user.getCreatedAt()
        );
    }
}