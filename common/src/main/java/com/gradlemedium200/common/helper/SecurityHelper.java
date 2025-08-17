package com.gradlemedium200.common.helper;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;
import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.util.UUID;

/**
 * Helper class for security-related operations like encryption and hashing.
 * This class provides methods for password hashing, encryption/decryption,
 * and API key generation.
 */
public class SecurityHelper {

    /**
     * Default hash algorithm used for password hashing
     */
    public static final String HASH_ALGORITHM = "SHA-256";
    
    /**
     * Default encryption algorithm name
     */
    public static final String ENCRYPTION_ALGORITHM = "AES";
    
    /**
     * Salt length for password hashing in bytes
     */
    public static final int SALT_LENGTH = 16;
    
    private static final SecureRandom SECURE_RANDOM = new SecureRandom();

    /**
     * Hash password with salt using secure algorithm
     * 
     * @param password The plain text password to hash
     * @param salt The salt to use in hashing
     * @return Base64 encoded hashed password
     * @throws RuntimeException if hashing algorithm is not available
     */
    public String hashPassword(String password, String salt) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            
            // Add salt to the digest
            digest.update(Base64.getDecoder().decode(salt));
            
            // Add password bytes to digest
            byte[] hashBytes = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            
            // Repeat the hashing to increase security
            for (int i = 0; i < 1000; i++) {
                digest.reset();
                hashBytes = digest.digest(hashBytes);
            }
            
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            // This should not happen as SHA-256 is a standard algorithm
            throw new RuntimeException("Error hashing password: " + e.getMessage(), e);
        }
    }

    /**
     * Generate random salt for password hashing
     * 
     * @return Base64 encoded random salt
     */
    public String generateSalt() {
        byte[] salt = new byte[SALT_LENGTH];
        SECURE_RANDOM.nextBytes(salt);
        return Base64.getEncoder().encodeToString(salt);
    }

    /**
     * Verify password against hashed password
     * 
     * @param password The plain text password to verify
     * @param hashedPassword The hashed password to compare against
     * @param salt The salt used in the original hashing
     * @return true if password matches, false otherwise
     */
    public boolean verifyPassword(String password, String hashedPassword, String salt) {
        String hashedInput = hashPassword(password, salt);
        return hashedInput.equals(hashedPassword);
    }

    /**
     * Encrypt plain text using specified key
     * 
     * @param plainText The text to encrypt
     * @param key The encryption key
     * @return Base64 encoded encrypted string
     * @throws RuntimeException if encryption fails
     */
    public String encrypt(String plainText, String key) {
        try {
            SecretKeySpec secretKey = createSecretKey(key);
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, secretKey);
            byte[] encryptedBytes = cipher.doFinal(plainText.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(encryptedBytes);
        } catch (Exception e) {
            // FIXME: Consider more specific exception handling
            throw new RuntimeException("Error encrypting data: " + e.getMessage(), e);
        }
    }

    /**
     * Decrypt encrypted text using specified key
     * 
     * @param encryptedText Base64 encoded encrypted text
     * @param key The decryption key
     * @return Decrypted plain text
     * @throws RuntimeException if decryption fails
     */
    public String decrypt(String encryptedText, String key) {
        try {
            SecretKeySpec secretKey = createSecretKey(key);
            Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, secretKey);
            byte[] decodedBytes = Base64.getDecoder().decode(encryptedText);
            byte[] decryptedBytes = cipher.doFinal(decodedBytes);
            return new String(decryptedBytes, StandardCharsets.UTF_8);
        } catch (Exception e) {
            // FIXME: Consider more specific exception handling and security implications
            throw new RuntimeException("Error decrypting data: " + e.getMessage(), e);
        }
    }

    /**
     * Generate secure API key
     * 
     * @return A secure random API key
     */
    public String generateApiKey() {
        // Generate a UUID as base for the API key
        String uuid = UUID.randomUUID().toString().replace("-", "");
        
        // Add additional randomness
        byte[] randomBytes = new byte[16];
        SECURE_RANDOM.nextBytes(randomBytes);
        String randomPart = Base64.getEncoder().encodeToString(randomBytes)
                .replace("=", "")
                .replace("/", "")
                .replace("+", "");
        
        // TODO: Consider adding timestamp or other unique identifiers
        return uuid + randomPart.substring(0, 16);
    }
    
    /**
     * Helper method to create a SecretKeySpec from the provided key
     * 
     * @param key The key to use for encryption/decryption
     * @return SecretKeySpec for cipher operations
     * @throws NoSuchAlgorithmException if the hashing algorithm is not available
     */
    private SecretKeySpec createSecretKey(String key) throws NoSuchAlgorithmException {
        // Use hash to normalize the key length
        MessageDigest sha = MessageDigest.getInstance("SHA-256");
        byte[] keyBytes = key.getBytes(StandardCharsets.UTF_8);
        keyBytes = sha.digest(keyBytes);
        // Use first 16 bytes for AES key
        byte[] truncatedKeyBytes = new byte[16];
        System.arraycopy(keyBytes, 0, truncatedKeyBytes, 0, truncatedKeyBytes.length);
        return new SecretKeySpec(truncatedKeyBytes, ENCRYPTION_ALGORITHM);
    }
}