package encryption;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Base64;

/**
 * Class to handle AES encryption:
 * <p>
 * Key generating
 * Encryption and decryption
 */

public class AESEncryption {

    /**
     * Generates a random AES key
     *
     * @return the generated key
     */
    SecretKey aesKey;

    public AESEncryption() {
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("AES");
            keyGen.init(256, SecureRandom.getInstanceStrong());
            aesKey = keyGen.generateKey();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
    }

    public AESEncryption(String key) {
        byte[] keyBytes = Base64.getDecoder().decode(key);
        aesKey = new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
    }

    public SecretKey getAESKey() {
        return aesKey;
    }

    public String encrypt(final String string) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, this.aesKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(string.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e);
        }
        return null;
    }

    public String decrypt(final String encMessage) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, this.aesKey);
            return new String(cipher.doFinal(Base64.getDecoder().decode(encMessage)));
        } catch (Exception e) {
            System.out.println("Error while decrypting: " + e);
        }
        return null;
    }

    /**
     * Encrypts a string using AES encryption
     *
     * @param string The string
     * @param key    the key to encrypt the string with
     * @return the encrypted string
     */
    public static String encrypt(final String string, final SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, key);
            return Base64.getEncoder().encodeToString(cipher.doFinal(string.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e);
        }
        return null;
    }

    public static String encrypt(final String string, String  key) {
        try {
            byte[] keyBytes = Base64.getDecoder().decode(key);
            SecretKey originalKey = new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");

            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5Padding");
            cipher.init(Cipher.ENCRYPT_MODE, originalKey);
            return Base64.getEncoder().encodeToString(cipher.doFinal(string.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception e) {
            System.out.println("Error while encrypting: " + e);
        }
        return null;
    }

    /**
     * Decrypts an AES encrypted string
     *
     * @param string the encrypted string to be decrypted
     * @param key    the AES key used to decrypt
     * @return the decrypted string
     */
    public static String decrypt(final String string, final SecretKey key) {
        try {
            Cipher cipher = Cipher.getInstance("AES/ECB/PKCS5PADDING");
            cipher.init(Cipher.DECRYPT_MODE, key);
            return new String(cipher.doFinal(Base64.getDecoder().decode(string)));
        } catch (Exception e) {
            System.out.println("Error while decrypting: " + e);
        }
        return null;
    }

    public static String getAesKeyString(SecretKey key) {
        return Base64.getEncoder().encodeToString(key.getEncoded());
    }

    public static SecretKey formatAesKey(String key) {
        byte[] keyBytes = Base64.getDecoder().decode(key);
        SecretKey originalKey = new SecretKeySpec(keyBytes, 0, keyBytes.length, "AES");
        return originalKey;
    }
}
