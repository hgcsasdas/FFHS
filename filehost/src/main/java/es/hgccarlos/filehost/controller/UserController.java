package es.hgccarlos.filehost.controller;

import es.hgccarlos.filehost.dto.UserDTO;
import es.hgccarlos.filehost.dto.Response;
import es.hgccarlos.filehost.model.User;
import es.hgccarlos.filehost.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // ---- Existing register endpoint ----
    @PostMapping("/create")
    public ResponseEntity<UserDTO> register(@RequestBody RegisterRequest req) {
            User newUser = userService.createUser(req.getUsername(), req.getPassword(), req.getRole());
            UserDTO userDTO = userService.toDTO(newUser);
            return ResponseEntity.status(HttpStatus.CREATED).body(userDTO);
    }

    // ---- GET /api/users - List all users (ADMIN only) ----
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserDTO>> getAllUsers() {
        List<UserDTO> users = userService.getAllUsers();
        return ResponseEntity.ok(users);
    }

    // ---- GET /api/users/{id} - Get user by ID (ADMIN or owner) ----
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userService.isUserOwner(#id, authentication.name)")
    public ResponseEntity<UserDTO> getUserById(@PathVariable UUID id, Authentication authentication) {
        UserDTO user = userService.getUserById(id);
        return ResponseEntity.ok(user);
    }

    // ---- PUT /api/users/{id} - Update user (ADMIN or owner) ----
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userService.isUserOwner(#id, authentication.name)")
    public ResponseEntity<UserDTO> updateUser(@PathVariable UUID id, 
                                            @RequestBody UserDTO request,
                                            Authentication authentication) {
        // Non-admin users can't change role
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        if (!isAdmin && request.getRole() != null) {
            throw new SecurityException("Only administrators can change user roles");
        }
        
        UserDTO updatedUser = userService.updateUser(id, request);
        return ResponseEntity.ok(updatedUser);
    }

    // ---- DELETE /api/users/{id} - Delete user (ADMIN only) ----
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Response> deleteUser(@PathVariable UUID id, Authentication authentication) {
        
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(new Response("success", "200", "User deleted successfully", null, null));
        } catch (IllegalStateException e) {
            return ResponseEntity.badRequest()
                    .body(new Response("error", "DELETE_FORBIDDEN", e.getMessage(), null, null));
        }
    }

    // ---- Request DTOs ----
    @Data
    public static class RegisterRequest {
        private String username;
        private String password;
        private String role;
    }
}
