package es.hgccarlos.filehost.service;

import es.hgccarlos.filehost.dto.UserDTO;
import es.hgccarlos.filehost.model.User;
import es.hgccarlos.filehost.repository.UserRepository;

import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ---- Create User ----

    @Value("${app.user}")
    private String adminUsername;

    @Value("${app.password}")
    private String adminPassword;

  @Override
    public User createUser(String username, String rawPassword, String role) {
        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username already exists");
        }
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setRole(role);
        return userRepository.save(user);
    }

    @Override
    public User getByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    @PostConstruct
    public void createDefaultAdmin() {
        if (!userRepository.existsByUsername(adminUsername)) {
            createUser(adminUsername, adminPassword, "ADMIN");
        }
        
    }

    // ---- CRUD Operations ----

    @Override
    public List<UserDTO> getAllUsers() {
        log.info("Fetching all users");
        return userRepository.findAll()
                .stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public UserDTO getUserById(UUID id) {
        log.info("Fetching user by id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        return toDTO(user);
    }

    @Override
    public UserDTO updateUser(UUID id, UserDTO request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));

        // Update username if provided and different
        if (request.getUsername() != null && !request.getUsername().equals(user.getUsername())) {
            if (userRepository.existsByUsername(request.getUsername())) {
                throw new IllegalArgumentException("Username already exists: " + request.getUsername());
            }
            user.setUsername(request.getUsername());
        }

        // Update role if provided
        if (request.getRole() != null) {
            user.setRole(request.getRole());
        }

        // Update enabled status if provided
        user.setEnabled(request.isEnabled());

        User updatedUser = userRepository.save(user);
        return toDTO(updatedUser);
    }

    @Override
    public void deleteUser(UUID id) {
        log.info("Deleting user with id: {}", id);
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found with id: " + id));
        
        // Prevent deletion of the last admin user
        if ("ADMIN".equals(user.getRole())) {
            long adminCount = userRepository.findAll().stream()
                    .filter(u -> "ADMIN".equals(u.getRole()))
                    .count();
            if (adminCount <= 1) {
                throw new IllegalStateException("Cannot delete the last admin user");
            }
        }

        userRepository.delete(user);
        log.info("User deleted successfully: {}", user.getUsername());
    }

    // ---- Helper Methods ----

    @Override
    public UserDTO toDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getRole(),
                user.isEnabled(),
                user.getCreatedAt()
        );
    }

    @Override
    public boolean isUserOwner(UUID userId, String authenticatedUsername) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            return user != null && user.getUsername().equals(authenticatedUsername);
        } catch (Exception e) {
            log.warn("Error checking user ownership: {}", e.getMessage());
            return false;
        }
    }
}
