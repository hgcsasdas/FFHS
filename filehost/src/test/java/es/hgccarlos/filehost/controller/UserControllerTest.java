package es.hgccarlos.filehost.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.hgccarlos.filehost.config.ApiKeyFilter;
import es.hgccarlos.filehost.config.IpRateLimitFilter;
import es.hgccarlos.filehost.config.JwtAuthFilter;

import es.hgccarlos.filehost.dto.UserDTO;
import es.hgccarlos.filehost.model.User;
import es.hgccarlos.filehost.service.UserService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(
        controllers = UserController.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        IpRateLimitFilter.class,
                        JwtAuthFilter.class,
                        ApiKeyFilter.class
                }
        )
)
@AutoConfigureMockMvc(addFilters = false)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    // Test data
    private final UUID userId1 = UUID.randomUUID();
    private final UUID userId2 = UUID.randomUUID();
    private final LocalDateTime now = LocalDateTime.now();

    @Test
    @DisplayName("POST /api/users/create - Should register new user successfully")
    void testRegisterUser_Success() throws Exception {
        // Arrange
        UserController.RegisterRequest request = new UserController.RegisterRequest();
        request.setUsername("testuser");
        request.setPassword("password123");
        request.setRole("USER");

        User mockUser = User.builder()
                .id(userId1)
                .username("testuser")
                .role("USER")
                .enabled(true)
                .createdAt(now)
                .build();

        UserDTO expectedDTO = new UserDTO(userId1, "testuser", "USER", true, now);

        when(userService.createUser("testuser", "password123", "USER")).thenReturn(mockUser);
        when(userService.toDTO(mockUser)).thenReturn(expectedDTO);

        // Act & Assert
        mockMvc.perform(post("/api/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id", is(userId1.toString())))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.role", is("USER")))
                .andExpect(jsonPath("$.enabled", is(true)));
    }

    @Test
    @DisplayName("POST /api/users/create - Should fail when username already exists")
    void testRegisterUser_DuplicateUsername_ShouldFail() throws Exception {
        UserController.RegisterRequest request = new UserController.RegisterRequest();
        request.setUsername("duplicate");
        request.setPassword("pass");
        request.setRole("USER");

        when(userService.createUser("duplicate", "pass", "USER"))
                .thenThrow(new IllegalArgumentException("Username already exists"));

        mockMvc.perform(post("/api/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.code", is("RUNTIME_EXCEPTION")));
    }

    @Test
    @DisplayName("GET /api/users - Should return all users for ADMIN")
    @WithMockUser(roles = "ADMIN")
    void testGetAllUsers_AsAdmin() throws Exception {
        List<UserDTO> mockUsers = Arrays.asList(
                new UserDTO(userId1, "admin", "ADMIN", true, now),
                new UserDTO(userId2, "user", "USER", true, now)
        );

        when(userService.getAllUsers()).thenReturn(mockUsers);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].username", is("admin")))
                .andExpect(jsonPath("$[0].role", is("ADMIN")))
                .andExpect(jsonPath("$[1].username", is("user")))
                .andExpect(jsonPath("$[1].role", is("USER")));
    }

    @Test
    @DisplayName("GET /api/users - Should deny access for non-ADMIN")
    @WithMockUser(roles = "USER")
    void testGetAllUsers_AsUser_ShouldFail() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/users/{id} - Should return user by ID for ADMIN")
    @WithMockUser(roles = "ADMIN")
    void testGetUserById_AsAdmin() throws Exception {
        UserDTO mockUser = new UserDTO(userId1, "testuser", "USER", true, now);
        when(userService.getUserById(userId1)).thenReturn(mockUser);

        mockMvc.perform(get("/api/users/{id}", userId1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(userId1.toString())))
                .andExpect(jsonPath("$.username", is("testuser")))
                .andExpect(jsonPath("$.role", is("USER")));
    }

    @Test
    @DisplayName("GET /api/users/{id} - Should return own user data for user owner")
    @WithMockUser(username = "testuser", roles = "USER")
    void testGetUserById_AsOwner() throws Exception {
        UserDTO mockUser = new UserDTO(userId1, "testuser", "USER", true, now);
        when(userService.isUserOwner(userId1, "testuser")).thenReturn(true);
        when(userService.getUserById(userId1)).thenReturn(mockUser);

        mockMvc.perform(get("/api/users/{id}", userId1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("testuser")));
    }

    @Test
    @DisplayName("GET /api/users/{id} - Should deny access when not owner and not ADMIN")
    @WithMockUser(username = "otheruser", roles = "USER")
    void testGetUserById_NotOwner_ShouldFail() throws Exception {
        // Arrange – ensure ownership check fails
        when(userService.isUserOwner(userId1, "otheruser")).thenReturn(false);
        // Service shouldn't be called further, but safe to stub
        when(userService.getUserById(any())).thenReturn(null);

        mockMvc.perform(get("/api/users/{id}", userId1))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/users/{id} - Should update user successfully as ADMIN")
    @WithMockUser(roles = "ADMIN")
    void testUpdateUser_AsAdmin() throws Exception {
        UserDTO request = new UserDTO();
        request.setUsername("newusername");
        request.setRole("ADMIN");
        request.setEnabled(false);

        UserDTO updatedUser = new UserDTO(userId1, "newusername", "ADMIN", false, now);
        when(userService.updateUser(eq(userId1), any())).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/{id}", userId1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("newusername")))
                .andExpect(jsonPath("$.role", is("ADMIN")))
                .andExpect(jsonPath("$.enabled", is(false)));
    }

    @Test
    @DisplayName("PUT /api/users/{id} - Should update own user data but not role")
    @WithMockUser(username = "testuser", roles = "USER")
    void testUpdateUser_AsOwner_CannotChangeRole() throws Exception {
        UserDTO request = new UserDTO();
        request.setUsername("newusername");
        request.setRole("ADMIN"); // This should cause security exception

        when(userService.isUserOwner(userId1, "testuser")).thenReturn(true);

        mockMvc.perform(put("/api/users/{id}", userId1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError()); // SecurityException should cause 500
    }

    @Test
    @DisplayName("PUT /api/users/{id} - Should deny update when not owner and not ADMIN")
    @WithMockUser(username = "otheruser", roles = "USER")
    void testUpdateUser_NotOwner_ShouldFail() throws Exception {
        UserDTO request = new UserDTO();
        request.setUsername("whatever");

        when(userService.isUserOwner(userId1, "otheruser")).thenReturn(false);

        mockMvc.perform(put("/api/users/{id}", userId1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/users/{id} - Should update own user data (without role) successfully")
    @WithMockUser(username = "testuser", roles = "USER")
    void testUpdateUser_AsOwner_Success() throws Exception {
        UserDTO request = new UserDTO();
        request.setUsername("mynewname");
        request.setEnabled(false);

        UserDTO updated = new UserDTO(userId1, "mynewname", "USER", false, now);

        when(userService.isUserOwner(userId1, "testuser")).thenReturn(true);
        when(userService.updateUser(eq(userId1), any())).thenReturn(updated);

        mockMvc.perform(put("/api/users/{id}", userId1)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is("mynewname")))
                .andExpect(jsonPath("$.role", is("USER")))
                .andExpect(jsonPath("$.enabled", is(false)));
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - Should delete user successfully as ADMIN")
    @WithMockUser(roles = "ADMIN")
    void testDeleteUser_AsAdmin_Success() throws Exception {

        mockMvc.perform(delete("/api/users/{id}", userId1))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status", is("success")))
                .andExpect(jsonPath("$.message", is("User deleted successfully")));
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - Should fail when trying to delete last admin")
    @WithMockUser(roles = "ADMIN")
    void testDeleteUser_LastAdmin_ShouldFail() throws Exception {

        doThrow(new IllegalStateException("Cannot delete the last admin user"))
                .when(userService).deleteUser(userId1);

        mockMvc.perform(delete("/api/users/{id}", userId1))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status", is("error")))
                .andExpect(jsonPath("$.code", is("DELETE_FORBIDDEN")));
    }

    @Test
    @DisplayName("DELETE /api/users/{id} - Should deny access for non-ADMIN")
    @WithMockUser(roles = "USER")
    void testDeleteUser_AsUser_ShouldFail() throws Exception {

        mockMvc.perform(delete("/api/users/{id}", userId1))
                .andExpect(status().isForbidden());
    }

    
    
}