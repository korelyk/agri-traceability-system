package com.agritrace.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Base64;

@Service
public class KeySecurityService {

    private static final String PREFIX = "enc:v1:";
    private static final String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int IV_LENGTH = 12;
    private static final int TAG_LENGTH = 128;

    private final SecretKeySpec secretKeySpec;
    private final SecureRandom secureRandom = new SecureRandom();

    public KeySecurityService(@Value("${app.security.key-secret}") String keySecret) {
        this.secretKeySpec = new SecretKeySpec(deriveKey(keySecret), "AES");
    }

    public String encrypt(String plainText) {
        if (plainText == null || plainText.isBlank()) {
            return plainText;
        }
        if (isEncrypted(plainText)) {
            return plainText;
        }

        try {
            byte[] iv = new byte[IV_LENGTH];
            secureRandom.nextBytes(iv);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, new GCMParameterSpec(TAG_LENGTH, iv));
            byte[] encrypted = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));

            byte[] payload = new byte[IV_LENGTH + encrypted.length];
            System.arraycopy(iv, 0, payload, 0, IV_LENGTH);
            System.arraycopy(encrypted, 0, payload, IV_LENGTH, encrypted.length);
            return PREFIX + Base64.getEncoder().encodeToString(payload);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to encrypt key material", ex);
        }
    }

    public String decrypt(String cipherText) {
        if (cipherText == null || cipherText.isBlank()) {
            return cipherText;
        }
        if (!isEncrypted(cipherText)) {
            return cipherText;
        }

        try {
            byte[] payload = Base64.getDecoder().decode(cipherText.substring(PREFIX.length()));
            byte[] iv = Arrays.copyOfRange(payload, 0, IV_LENGTH);
            byte[] encrypted = Arrays.copyOfRange(payload, IV_LENGTH, payload.length);

            Cipher cipher = Cipher.getInstance(TRANSFORMATION);
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, new GCMParameterSpec(TAG_LENGTH, iv));
            return new String(cipher.doFinal(encrypted), StandardCharsets.UTF_8);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to decrypt key material", ex);
        }
    }

    public boolean isEncrypted(String value) {
        return value != null && value.startsWith(PREFIX);
    }

    private byte[] deriveKey(String keySecret) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            return digest.digest(keySecret.getBytes(StandardCharsets.UTF_8));
        } catch (Exception ex) {
            throw new RuntimeException("Failed to derive encryption key", ex);
        }
    }
}
