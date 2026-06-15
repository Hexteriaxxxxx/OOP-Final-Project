package main.utils;

import java.security.SecureRandom;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages OTP (One-Time Password) generation and verification.
 * Stores codes in-memory with a 5-minute expiry window.
 */
public class OTPManager {

    private static final Map<String, String> otpStore    = new HashMap<>();
    private static final Map<String, Long>   expiryStore = new HashMap<>();
    private static final long OTP_EXPIRY_MS = 5 * 60 * 1000L; // 5 minutes
    private static final SecureRandom random = new SecureRandom();

    /** Generates a 6-digit OTP for the given email and stores it with expiry. */
    public static String generateOTP(String email) {
        String otp = String.format("%06d", random.nextInt(1_000_000));
        String key = email.toLowerCase().trim();
        otpStore.put(key, otp);
        expiryStore.put(key, System.currentTimeMillis() + OTP_EXPIRY_MS);
        System.out.println("[OTP] Generated for " + key + ": " + otp);
        return otp;
    }

    /**
     * Verifies the OTP for the given email.
     * Returns true and clears the OTP if valid and not expired.
     * Returns false if invalid, expired, or not found.
     */
    public static boolean verifyOTP(String email, String code) {
        String key = email.toLowerCase().trim();
        String stored = otpStore.get(key);
        Long expiry   = expiryStore.get(key);

        if (stored == null || expiry == null) {
            System.out.println("[OTP] No code found for " + key);
            return false;
        }
        if (System.currentTimeMillis() > expiry) {
            System.out.println("[OTP] Code expired for " + key);
            otpStore.remove(key);
            expiryStore.remove(key);
            return false;
        }
        if (!stored.equals(code)) {
            System.out.println("[OTP] Wrong code for " + key);
            return false;
        }

        // Clear OTP after successful verification (one-time use)
        otpStore.remove(key);
        expiryStore.remove(key);
        System.out.println("[OTP] Verified successfully for " + key);
        return true;
    }

    /** Returns true if the stored OTP for this email has already expired. */
    public static boolean isExpired(String email) {
        String key = email.toLowerCase().trim();
        Long expiry = expiryStore.get(key);
        if (expiry == null) return true;
        return System.currentTimeMillis() > expiry;
    }

    /** Returns remaining seconds before OTP expires (0 if already expired). */
    public static long getRemainingSeconds(String email) {
        String key = email.toLowerCase().trim();
        Long expiry = expiryStore.get(key);
        if (expiry == null) return 0;
        long remaining = (expiry - System.currentTimeMillis()) / 1000;
        return Math.max(0, remaining);
    }
}
