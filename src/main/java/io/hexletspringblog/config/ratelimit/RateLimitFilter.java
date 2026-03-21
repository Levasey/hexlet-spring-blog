package io.hexletspringblog.config.ratelimit;

import com.fasterxml.jackson.databind.ObjectMapper;
import io.hexletspringblog.handler.ApiProblem;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.lang.NonNull;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Ограничение частоты по IP для входа и (опционально) публичных чтений API.
 */
public class RateLimitFilter extends OncePerRequestFilter {

    private static final AntPathMatcher PATH_MATCHER = new AntPathMatcher();

    private final RateLimitProperties properties;
    private final ObjectMapper objectMapper;
    private final ConcurrentHashMap<String, Bucket> loginBuckets = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, Bucket> publicGetBuckets = new ConcurrentHashMap<>();

    public RateLimitFilter(RateLimitProperties properties, ObjectMapper objectMapper) {
        this.properties = properties;
        this.objectMapper = objectMapper;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        if (!properties.isEnabled()) {
            return true;
        }
        String path = pathWithinApplication(request);
        String method = request.getMethod();
        if (properties.getLogin().isEnabled() && "POST".equalsIgnoreCase(method) && "/api/login".equals(path)) {
            return false;
        }
        return !(properties.getPublicGet().isEnabled()
                && "GET".equalsIgnoreCase(method)
                && matchesPublicApiGet(path));
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        String clientKey = clientKey(request);
        String path = pathWithinApplication(request);
        String method = request.getMethod();

        if (properties.getLogin().isEnabled() && "POST".equalsIgnoreCase(method) && "/api/login".equals(path)) {
            if (!consumeOrDeny(loginBuckets, clientKey, loginBandwidth(), response)) {
                return;
            }
        } else if (properties.getPublicGet().isEnabled()
                && "GET".equalsIgnoreCase(method)
                && matchesPublicApiGet(path)) {
            if (!consumeOrDeny(publicGetBuckets, clientKey, publicGetBandwidth(), response)) {
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private Bandwidth loginBandwidth() {
        int rpm = Math.max(1, properties.getLogin().getRequestsPerMinute());
        return Bandwidth.builder()
                .capacity(rpm)
                .refillGreedy(rpm, Duration.ofMinutes(1))
                .build();
    }

    private Bandwidth publicGetBandwidth() {
        int rpm = Math.max(1, properties.getPublicGet().getRequestsPerMinute());
        return Bandwidth.builder()
                .capacity(rpm)
                .refillGreedy(rpm, Duration.ofMinutes(1))
                .build();
    }

    private boolean consumeOrDeny(
            ConcurrentHashMap<String, Bucket> cache,
            String key,
            Bandwidth bandwidth,
            HttpServletResponse response
    ) throws IOException {
        Bucket bucket = cache.computeIfAbsent(key, k -> Bucket.builder().addLimit(bandwidth).build());
        ConsumptionProbe probe = bucket.tryConsumeAndReturnRemaining(1);
        if (!probe.isConsumed()) {
            deny(response, probe);
            return false;
        }
        return true;
    }

    private void deny(HttpServletResponse response, ConsumptionProbe probe) throws IOException {
        long retryAfterSeconds = Math.max(1L, nanosToSecondsCeil(probe.getNanosToWaitForRefill()));
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setHeader("Retry-After", Long.toString(retryAfterSeconds));
        response.setContentType(ApiProblem.MEDIA_TYPE.toString());

        var pd = ApiProblem.of(
                HttpStatus.TOO_MANY_REQUESTS,
                "TOO_MANY_REQUESTS",
                "Too Many Requests",
                "Rate limit exceeded. Try again later.",
                null
        );
        objectMapper.writeValue(response.getOutputStream(), pd);
    }

    private static long nanosToSecondsCeil(long nanos) {
        if (nanos <= 0) {
            return 1L;
        }
        return (nanos + 999_999_999L) / 1_000_000_000L;
    }

    private String clientKey(HttpServletRequest request) {
        if (properties.isTrustXForwardedFor()) {
            String forwarded = request.getHeader("X-Forwarded-For");
            if (forwarded != null && !forwarded.isBlank()) {
                String first = forwarded.split(",")[0].trim();
                if (!first.isEmpty()) {
                    return first;
                }
            }
        }
        return request.getRemoteAddr() != null ? request.getRemoteAddr() : "unknown";
    }

    private static String pathWithinApplication(HttpServletRequest request) {
        String uri = request.getRequestURI();
        String context = request.getContextPath();
        if (context != null && !context.isEmpty() && uri != null && uri.startsWith(context)) {
            return uri.substring(context.length());
        }
        return uri != null ? uri : "";
    }

    private static boolean matchesPublicApiGet(String path) {
        return PATH_MATCHER.match("/api/posts", path)
                || PATH_MATCHER.match("/api/posts/*", path)
                || PATH_MATCHER.match("/api/tags", path)
                || PATH_MATCHER.match("/api/tags/*", path);
    }
}
