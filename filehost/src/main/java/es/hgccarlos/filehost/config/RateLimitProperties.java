package es.hgccarlos.filehost.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.List;

@Data
@Component
@ConfigurationProperties(prefix = "ratelimit.global")
public class RateLimitProperties {

    private int limitForPeriod;
    private Duration limitRefreshPeriod;
    private Duration timeoutDuration;
    private List<String> whitelist;

}
