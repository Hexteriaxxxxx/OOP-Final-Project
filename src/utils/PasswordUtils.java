package utils;

import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;

public class PasswordUtils {

    // I-hash ang password bago i-save sa database
    public static String hashPassword(String plainPassword) {
        try {
            SecureRandom random = new SecureRandom();
            byte[] salt = new byte[16];
            random.nextBytes(salt);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedBytes = md.digest(plainPassword.getBytes());

            byte[] combined = new byte[salt.length + hashedBytes.length];
            System.arraycopy(salt, 0, combined, 0, salt.length);
            System.arraycopy(hashedBytes, 0, combined, salt.length, hashedBytes.length);

            return Base64.getEncoder().encodeToString(combined);

        } catch (Exception e) {
            throw new RuntimeException("❌ Hashing failed!", e);
        }
    }

    // I-verify ang password sa pag-login
    public static boolean verifyPassword(String plainPassword, String storedHash) {
        try {
            byte[] combined = Base64.getDecoder().decode(storedHash);

            byte[] salt = new byte[16];
            System.arraycopy(combined, 0, salt, 0, 16);

            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(salt);
            byte[] hashedBytes = md.digest(plainPassword.getBytes());

            for (int i = 0; i < hashedBytes.length; i++) {
                if (combined[i + 16] != hashedBytes[i]) return false;
            }
            return true;

        } catch (Exception e) {
            throw new RuntimeException("❌ Verification failed!", e);
        }
    }
}