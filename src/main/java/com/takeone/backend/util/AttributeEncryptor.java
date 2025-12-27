package com.takeone.backend.util;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Component
@Converter
public class AttributeEncryptor implements AttributeConverter<String, String> {

    private static final String AES = "AES";

    // In a real scenario, this key should be injected from a secure environment
    // variable.
    // We will use a default for dev if not present, but for prod it must be set.
    private final String secretKey;

    public AttributeEncryptor(@Value("${app.security.db-encryption-key:DefaultSecretKey123}") String secretKey) {
        // Ensure key is 16, 24, or 32 bytes. We'll pad or truncate for simplicity in
        // this demo,
        // but robust key management is recommended.
        this.secretKey = fixKeyLength(secretKey);
    }

    private String fixKeyLength(String key) {
        if (key.length() < 16) {
            return String.format("%-16s", key).replace(' ', '0');
        }
        return key.substring(0, 16);
    }

    @Override
    public String convertToDatabaseColumn(String attribute) {
        if (attribute == null)
            return null;
        try {
            Cipher cipher = Cipher.getInstance(AES);
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), AES);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec);
            return Base64.getEncoder().encodeToString(cipher.doFinal(attribute.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            throw new IllegalStateException("Error encrypting data", e);
        }
    }

    @Override
    public String convertToEntityAttribute(String dbData) {
        if (dbData == null)
            return null;
        try {
            Cipher cipher = Cipher.getInstance(AES);
            SecretKeySpec keySpec = new SecretKeySpec(secretKey.getBytes(StandardCharsets.UTF_8), AES);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            return new String(cipher.doFinal(Base64.getDecoder().decode(dbData)), StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException("Error decrypting data", e);
        }
    }
}
