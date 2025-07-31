package es.hgccarlos.filehost.config;

import io.github.resilience4j.ratelimiter.RateLimiter;
import io.github.resilience4j.ratelimiter.RateLimiterConfig;
import io.github.resilience4j.ratelimiter.RateLimiterRegistry;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;

@Component
@RequiredArgsConstructor
public class IpRateLimitFilter extends OncePerRequestFilter {

    private final RateLimitProperties props;
    private final RateLimiterRegistry registry;

    @Override
    protected void doFilterInternal(HttpServletRequest req,
                                    HttpServletResponse res,
                                    FilterChain chain)
            throws ServletException, IOException {

        String ip = req.getRemoteAddr();

        if (props.getWhitelist().contains(ip)) {
            chain.doFilter(req, res);
            return;
        }

        RateLimiterConfig config = RateLimiterConfig.custom()
                .limitForPeriod(props.getLimitForPeriod())
                .limitRefreshPeriod(props.getLimitRefreshPeriod())
                .timeoutDuration(props.getTimeoutDuration())
                .build();

        RateLimiter limiter = registry.rateLimiter(ip, () -> config);

        if (limiter.acquirePermission()) {
            chain.doFilter(req, res);
        } else {
            res.sendError(HttpStatus.TOO_MANY_REQUESTS.value(),
                    "Rate limit exceeded for IP: " + ip);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String p = request.getRequestURI();
        return !p.startsWith("/api/");
    }
}
