package es.hgccarlos.filehost.controller;

import es.hgccarlos.filehost.model.User;
import es.hgccarlos.filehost.service.UserService;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public ResponseEntity<User> register(@RequestBody RegisterRequest req) {
        User newUser = userService.createUser(req.getUsername(), req.getPassword(), req.getRole());
        return ResponseEntity.ok(newUser);
    }

    @Data
    static class RegisterRequest {
        private String username;
        private String password;
        private String role;
    }
}
