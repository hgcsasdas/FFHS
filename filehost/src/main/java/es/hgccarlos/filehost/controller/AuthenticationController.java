package es.hgccarlos.filehost.controller;

// import es.hgccarlos.filehost.dto.GeneralErrorResponse;
import es.hgccarlos.filehost.dto.JwtResponse;
import es.hgccarlos.filehost.dto.LoginRequest;
import es.hgccarlos.filehost.model.User;
import es.hgccarlos.filehost.service.JwtService;
import es.hgccarlos.filehost.service.UserService;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.*;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final JwtService jwtService;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@RequestBody LoginRequest request) {
        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            User user = userService.getByUsername(request.getUsername());

            String token = jwtService.generateToken(user.getUsername(), java.util.List.of(user.getRole()));

            return ResponseEntity.ok(new JwtResponse(token, ""));
        } catch (AuthenticationException e) {
            if (e instanceof BadCredentialsException) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new JwtResponse("", "Credenciales inválidas"));
            } else if (e instanceof DisabledException) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new JwtResponse("","Usuario deshabilitado"));
            } else if (e instanceof LockedException) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new JwtResponse("","Usuario bloqueado"));
            } else {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new JwtResponse("","Error de autenticación: " + e.getMessage()));
            }
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refreshToken(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        String oldToken = null;
        String username = null;

        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            oldToken = authHeader.substring(7);
        }

        if (oldToken == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new JwtResponse("","Token no proporcionado"));
        }

        try {
            username = jwtService.getUsernameFromToken(oldToken);

            if (!jwtService.isTokenSignatureValid(oldToken)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new JwtResponse("","Firma de token inválida"));
            }

            User user = userService.getByUsername(username);
            if (user == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new JwtResponse("","Usuario no encontrado para el token"));
            }

            String newToken = jwtService.generateToken(user.getUsername(), java.util.List.of(user.getRole()));

            return ResponseEntity.ok(new JwtResponse(newToken, ""));

        } catch (ExpiredJwtException e) {
            try {

                username = jwtService.getUsernameFromTokenEvenIfExpired(oldToken);

                User user = userService.getByUsername(username);
                if (user == null) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new JwtResponse("","Usuario no encontrado al refrescar token expirado"));
                }
                String newToken = jwtService.generateToken(user.getUsername(), java.util.List.of(user.getRole()));
                return ResponseEntity.ok(new JwtResponse(newToken, ""));
            } catch (Exception ex) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new JwtResponse("","Token expirado o inválido para refrescar: " + ex.getMessage()));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new JwtResponse("","Token inválido para refrescar: " + e.getMessage()));
        }
    }
}