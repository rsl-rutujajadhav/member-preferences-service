package com.example.memberpreferences.filter;

import java.util.concurrent.atomic.AtomicLong;

class TokenBucket {

    private final int capacity;
    private final double refillPerSecond;
    private final AtomicLong tokens;
    private volatile long lastRefillNanos;

    TokenBucket(int capacity, double refillPerSecond) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("capacity must be positive: " + capacity);
        }
        if (refillPerSecond <= 0) {
            throw new IllegalArgumentException("refillPerSecond must be positive: " + refillPerSecond);
        }
        this.capacity = capacity;
        this.refillPerSecond = refillPerSecond;
        this.tokens = new AtomicLong(capacity);
        this.lastRefillNanos = System.nanoTime();
    }

    boolean tryConsume() {
        refill();
        long current;
        do {
            current = tokens.get();
            if (current <= 0) {
                return false;
            }
        } while (!tokens.compareAndSet(current, current - 1));
        return true;
    }

    private void refill() {
        long now = System.nanoTime();
        long last = lastRefillNanos;
        long elapsed = now - last;
        if (elapsed <= 0) {
            return;
        }
        if (tokens.get() >= capacity) {
            return;
        }
        double newTokens = elapsed / 1_000_000_000.0 * refillPerSecond;
        if (newTokens < 1) {
            return;
        }
        lastRefillNanos = now;
        tokens.updateAndGet(current -> {
            long updated = current + (long) newTokens;
            return Math.min(updated, capacity);
        });
    }
}
