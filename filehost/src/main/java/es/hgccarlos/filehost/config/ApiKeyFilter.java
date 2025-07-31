package es.hgccarlos.filehost.config;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class ApiKeyFilter extends OncePerRequestFilter {

    @Value("${api.key}")
    private String apikey;

    private static final String HEADER = "X-API-KEY";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.startsWith("/actuator") || path.startsWith("/auth");
    }


    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain)
            throws ServletException, IOException {

        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            chain.doFilter(request, response);
            return;
        }

        // Requiere API Key y Basic Auth
        String apiKey = request.getHeader(HEADER);
        String auth = request.getHeader("Authorization");

        if (apiKey == null || !apiKey.equals(apikey)) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Missing or wrong API Key");
            return;
        }

        if (auth == null || !auth.startsWith("Basic ")) {
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Basic Auth required with API Key");
            return;
        }

        chain.doFilter(request, response);
    }

}

