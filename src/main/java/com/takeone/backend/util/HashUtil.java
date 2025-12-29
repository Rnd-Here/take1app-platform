package com.takeone.backend.util;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class for hashing operations
 */
public class HashUtil {

    private static final String ALGORITHM = "SHA-256";

    /**
     * Generate SHA-256 hash of input string
     * Used for username uniqueness checking
     */
    public static String sha256(String input) {
        if (input == null || input.isEmpty()) {
            return null;
        }

        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITHM);
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }

    /**
     * Convert byte array to hex string
     */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder(2 * bytes.length);
        for (byte b : bytes) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }

    /**
     * Validate if hash matches input
     */
    public static boolean validateHash(String input, String hash) {
        if (input == null || hash == null) {
            return false;
        }
        return sha256(input).equals(hash);
    }
}