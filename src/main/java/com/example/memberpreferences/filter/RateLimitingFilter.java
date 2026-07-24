package com.example.memberpreferences.filter;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;

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
import com.example.memberpreferences.config.RateLimitingProperties.RateLimitConfig;
import com.example.memberpreferences.domain.dto.ErrorResponse;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 10)
public class RateLimitingFilter extends OncePerRequestFilter {

    private static final Pattern MEMBER_ID_PATH = Pattern.compile("/v1/preferences/([A-Za-z0-9_-]+)");

    private final TokenBucket overallBucket;
    private final ObjectMapper objectMapper;
    private final Map<String, TokenBucket> memberBuckets = new ConcurrentHashMap<>();
    private final RateLimitConfig perMemberConfig;

    public RateLimitingFilter(RateLimitingProperties properties) {
        RateLimitConfig overallConfig = properties.getOverall();
        this.overallBucket = new TokenBucket(overallConfig.getCapacity(), overallConfig.tokensPerSecond());
        this.perMemberConfig = properties.getPerMember();
        this.objectMapper = new ObjectMapper();
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        if (!overallBucket.tryConsume()) {
            writeRateLimitResponse(request, response, "Overall rate limit exceeded");
            return;
        }

        String memberId = extractMemberId(request.getRequestURI());
        if (memberId != null) {
            TokenBucket bucket = bucketForMember(memberId);
            if (!bucket.tryConsume()) {
                writeRateLimitResponse(request, response, "Rate limit exceeded for member: " + memberId);
                return;
            }
        }

        chain.doFilter(request, response);
    }

    private TokenBucket bucketForMember(String memberId) {
        return memberBuckets.computeIfAbsent(memberId, k ->
                new TokenBucket(perMemberConfig.getCapacity(), perMemberConfig.tokensPerSecond()));
    }

    private static String extractMemberId(String path) {
        Matcher matcher = MEMBER_ID_PATH.matcher(path);
        return matcher.find() ? matcher.group(1) : null;
    }

    private void writeRateLimitResponse(HttpServletRequest request,
                                         HttpServletResponse response,
                                         String detail) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader("Retry-After", "1");

        ErrorResponse error = new ErrorResponse(
                "Too Many Requests",
                HttpStatus.TOO_MANY_REQUESTS.value(),
                detail,
                request.getRequestURI()
        );
        objectMapper.writeValue(response.getWriter(), error);
    }
}
