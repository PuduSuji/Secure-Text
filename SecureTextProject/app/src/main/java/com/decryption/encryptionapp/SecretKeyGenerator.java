package com.decryption.encryptionapp;

import android.util.Base64;

import java.security.SecureRandom;

public class SecretKeyGenerator {

    public static String generateSecretKey(int keySize) {
        // Use a cryptographically secure random number generator
        SecureRandom random = new SecureRandom();

        // Generate random bytes for the secret key
        byte[] keyBytes = new byte[keySize / 8]; // Key size in bytes
        random.nextBytes(keyBytes);

        // Convert bytes to a base64 encoded string for easier storage and use
        return Base64.encodeToString(keyBytes, Base64.DEFAULT);
    }

}
