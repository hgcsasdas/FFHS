package es.hgccarlos.filehost.service;

import es.hgccarlos.filehost.model.User;
import es.hgccarlos.filehost.repository.UserRepository;
import es.hgccarlos.filehost.service.UserService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

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
}
