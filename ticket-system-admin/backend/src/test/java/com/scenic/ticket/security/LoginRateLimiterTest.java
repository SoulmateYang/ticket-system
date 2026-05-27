package com.scenic.ticket.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class LoginRateLimiterTest {

    private LoginRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        rateLimiter = new LoginRateLimiter();
    }

    @Test
    void isLocked_ReturnsFalse_WhenNoAttempts() {
        assertFalse(rateLimiter.isLocked("13800138000"));
    }

    @Test
    void isLocked_ReturnsFalse_WhenUnderMaxAttempts() {
        rateLimiter.recordFailure("13800138000");
        rateLimiter.recordFailure("13800138000");
        rateLimiter.recordFailure("13800138000");

        assertFalse(rateLimiter.isLocked("13800138000"));
        assertEquals(2, rateLimiter.getRemainingAttempts("13800138000"));
    }

    @Test
    void isLocked_ReturnsTrue_WhenMaxAttemptsReached() {
        for (int i = 0; i < 5; i++) {
            rateLimiter.recordFailure("13800138000");
        }

        assertTrue(rateLimiter.isLocked("13800138000"));
        assertEquals(0, rateLimiter.getRemainingAttempts("13800138000"));
    }

    @Test
    void recordSuccess_ClearsAttempts() {
        rateLimiter.recordFailure("13800138000");
        rateLimiter.recordFailure("13800138000");

        rateLimiter.recordSuccess("13800138000");

        assertFalse(rateLimiter.isLocked("13800138000"));
        assertEquals(5, rateLimiter.getRemainingAttempts("13800138000"));
    }

    @Test
    void differentPhones_TrackedSeparately() {
        rateLimiter.recordFailure("13800138000");
        rateLimiter.recordFailure("13800138000");
        rateLimiter.recordFailure("13800138000");

        assertFalse(rateLimiter.isLocked("13800138001"));
        assertEquals(5, rateLimiter.getRemainingAttempts("13800138001"));
    }
}
