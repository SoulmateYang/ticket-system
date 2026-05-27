package com.scenic.ticket.security;

import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 登录限流器 — 防止暴力破解
 * 使用内存存储，单实例部署足够。生产环境建议使用 Redis 分布式限流。
 */
@Component
public class LoginRateLimiter {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCKOUT_MS = 15 * 60 * 1000; // 15分钟

    private final ConcurrentHashMap<String, LoginAttempt> attempts = new ConcurrentHashMap<>();

    public boolean isLocked(String phone) {
        LoginAttempt attempt = attempts.get(phone);
        if (attempt == null) {
            return false;
        }
        if (System.currentTimeMillis() - attempt.firstAttemptMs > LOCKOUT_MS) {
            attempts.remove(phone);
            return false;
        }
        return attempt.count.get() >= MAX_ATTEMPTS;
    }

    public void recordFailure(String phone) {
        LoginAttempt attempt = attempts.computeIfAbsent(phone, k -> new LoginAttempt());
        attempt.count.incrementAndGet();
        attempt.firstAttemptMs = System.currentTimeMillis();
    }

    public void recordSuccess(String phone) {
        attempts.remove(phone);
    }

    public int getRemainingAttempts(String phone) {
        LoginAttempt attempt = attempts.get(phone);
        if (attempt == null) {
            return MAX_ATTEMPTS;
        }
        return Math.max(0, MAX_ATTEMPTS - attempt.count.get());
    }

    private static class LoginAttempt {
        AtomicInteger count = new AtomicInteger(0);
        long firstAttemptMs = System.currentTimeMillis();
    }
}
