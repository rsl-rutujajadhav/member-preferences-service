package com.example.memberpreferences.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Positive;

@Validated
@ConfigurationProperties(prefix = "preferences.rate-limit")
public class RateLimitingProperties {

    private RateLimitConfig overall = new RateLimitConfig(100, 100);
    private RateLimitConfig perMember = new RateLimitConfig(20, 20);

    public RateLimitConfig getOverall() {
        return overall;
    }

    public void setOverall(RateLimitConfig overall) {
        this.overall = overall;
    }

    public RateLimitConfig getPerMember() {
        return perMember;
    }

    public void setPerMember(RateLimitConfig perMember) {
        this.perMember = perMember;
    }

    public static class RateLimitConfig {

        @Positive
        private int capacity;

        @Positive
        private int refillPerMinute;

        public RateLimitConfig() {
        }

        public RateLimitConfig(int capacity, int refillPerMinute) {
            this.capacity = capacity;
            this.refillPerMinute = refillPerMinute;
        }

        public int getCapacity() {
            return capacity;
        }

        public void setCapacity(int capacity) {
            this.capacity = capacity;
        }

        public int getRefillPerMinute() {
            return refillPerMinute;
        }

        public void setRefillPerMinute(int refillPerMinute) {
            this.refillPerMinute = refillPerMinute;
        }

        public double tokensPerSecond() {
            return refillPerMinute / 60.0;
        }
    }
}
