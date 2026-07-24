package com.example.memberpreferences.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.Positive;

@Validated
@ConfigurationProperties(prefix = "preferences.rate-limit")
public class RateLimitingProperties {

    private Overall overall = new Overall();
    private PerMember perMember = new PerMember();

    public Overall getOverall() {
        return overall;
    }

    public void setOverall(Overall overall) {
        this.overall = overall;
    }

    public PerMember getPerMember() {
        return perMember;
    }

    public void setPerMember(PerMember perMember) {
        this.perMember = perMember;
    }

    public static class Overall {

        @Positive
        private int capacity = 100;

        @Positive
        private int refillPerMinute = 100;

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

        public double refillPerSecond() {
            return refillPerMinute / 60.0;
        }
    }

    public static class PerMember {

        @Positive
        private int capacity = 20;

        @Positive
        private int refillPerMinute = 20;

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

        public double refillPerSecond() {
            return refillPerMinute / 60.0;
        }
    }
}
