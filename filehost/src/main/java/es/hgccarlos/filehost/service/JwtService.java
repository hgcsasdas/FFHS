package es.hgccarlos.filehost.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.List;
import java.util.function.Function;

@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expirationMs;

    private Key getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    public String generateToken(String username, List<String> roles) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .setSubject(username)
                .claim("roles", roles)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Valida el token, incluyendo la expiración
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    // Nuevo método: Valida solo la firma del token (ignora la expiración)
    public boolean isTokenSignatureValid(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parse(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public String getUsernameFromTokenEvenIfExpired(String token) {
        try {
            // No valida la expiración para obtener las claims
            Claims claims = Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token).getBody(); // parseClaimsJws lanzará ExpiredJwtException si el token ha expirado.
            // Esto es problemático si queremos leer el subject de un token expirado.
            // La estrategia más segura es usar parse() para validar la firma y luego extraer las claims de forma segura.
            // Sin embargo, si tu implementación actual de `extractClaim` no lanza por expiración si solo extrae,
            // entonces `extractClaim` podría servir. Revisa cómo se comporta con tokens expirados.

            // Si `extractClaim` ya maneja `ExpiredJwtException` o permite leer claims sin validación de expiración,
            // entonces este método podría no ser necesario o ser una envoltura simple.
            // La forma más robusta es parsear sin validar expiración, luego extraer.
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject();
        } catch (ExpiredJwtException e) {
            return e.getClaims().getSubject();
        } catch (JwtException | IllegalArgumentException e) {
            throw new RuntimeException("Invalid token for refresh: " + e.getMessage(), e);
        }
    }


    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claimsResolver.apply(claims);
    }

    @SuppressWarnings("unchecked")
    public List<String> getRolesFromToken(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.get("roles", List.class);
    }
}