package es.hgccarlos.filehost.service;

import es.hgccarlos.filehost.dto.UserDTO;
import es.hgccarlos.filehost.model.User;

import java.util.List;
import java.util.UUID;

public interface UserService {
    User createUser(String username, String rawPassword, String role);
    User getByUsername(String username);
    
    // CRUD operations
    List<UserDTO> getAllUsers();
    UserDTO getUserById(UUID id);
    UserDTO updateUser(UUID id, UserDTO request);
    void deleteUser(UUID id);
    
    // Helper methods
    UserDTO toDTO(User user);
    boolean isUserOwner(UUID userId, String authenticatedUsername);
}
