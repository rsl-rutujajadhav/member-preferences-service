package com.example.memberpreferences.filter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.memberpreferences.config.RateLimitingProperties;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Pattern MEMBER_ID_PATH = Pattern.compile("/v1/preferences/([A-Za-z0-9_-]+)");
    private static final String RETRY_AFTER = "Retry-After";

    private final TokenBucket overallBucket;
    private final Map<String, TokenBucket> memberBuckets = new ConcurrentHashMap<>();
    private final RateLimitingProperties properties;

    public RateLimitingFilter(RateLimitingProperties properties) {
        this.properties = properties;
        this.overallBucket = new TokenBucket(
                properties.getOverall().getCapacity(),
                properties.getOverall().refillPerSecond());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String path = request.getRequestURI();

        if (!overallBucket.tryConsume()) {
            sendTooManyRequests(response, "Overall rate limit exceeded");
            return;
        }

        String memberId = extractMemberId(path);
        if (memberId != null) {
            TokenBucket bucket = memberBuckets.computeIfAbsent(memberId, k ->
                    new TokenBucket(
                            properties.getPerMember().getCapacity(),
                            properties.getPerMember().refillPerSecond()));
            if (!bucket.tryConsume()) {
                sendTooManyRequests(response, "Rate limit exceeded for member: " + memberId);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private static String extractMemberId(String path) {
        Matcher matcher = MEMBER_ID_PATH.matcher(path);
        return matcher.find() ? matcher.group(1) : null;
    }

    private static void sendTooManyRequests(HttpServletResponse response, String detail) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(RETRY_AFTER, "1");
        response.getWriter().write("{\"title\":\"Too Many Requests\",\"status\":429,\"detail\":\""
                + detail + "\"}");
    }
}
