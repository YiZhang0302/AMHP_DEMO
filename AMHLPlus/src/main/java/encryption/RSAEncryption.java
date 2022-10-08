package encryption;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

public class RSAEncryption {

    private PrivateKey privateKey = null;
    private PublicKey publicKey = null;

    public RSAEncryption() {

        KeyPairGenerator keyGen = null;
        try {
            keyGen = KeyPairGenerator.getInstance("RSA");
            keyGen.initialize(2048);

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        assert keyGen != null;
        KeyPair pair = keyGen.generateKeyPair();
        this.privateKey = pair.getPrivate();
        this.publicKey = pair.getPublic();
    }

    public RSAEncryption(String rsaSk, String rsaPk) {
        byte[] secretBytes = Base64.getDecoder().decode(rsaSk);
        byte[] publicBytes = Base64.getDecoder().decode(rsaPk);
        PKCS8EncodedKeySpec secretKeySpec = new PKCS8EncodedKeySpec(secretBytes);
        X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicBytes);
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
            this.privateKey = keyFactory.generatePrivate(secretKeySpec);
            this.publicKey = keyFactory.generatePublic(publicKeySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }


    public String rsaDecrypt(String encryptedMessage) {

        byte[] bytes = Base64.getDecoder().decode(encryptedMessage);

        try {
            Cipher decryptCipher = Cipher.getInstance("RSA");
            decryptCipher.init(Cipher.DECRYPT_MODE, this.privateKey);
            String  result = Base64.getEncoder().encodeToString(decryptCipher.doFinal(bytes));
            return result;
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }

        return null;
    }

    public PrivateKey getPrivateKey() {
        return privateKey;
    }

    public PublicKey getPublicKey() {
        return publicKey;
    }

    /**
     * Decrypts a message with the RSA encryption algorithm
     *
     * @param encryptedMessage the message to be decrypted
     * @param privateKey       the private key to decrypt the message
     * @return the decrypted string
     */
    public static String rsaDecrypt(String encryptedMessage, PrivateKey privateKey) {

        byte[] bytes = Base64.getDecoder().decode(encryptedMessage);

        try {
            Cipher decryptCipher = Cipher.getInstance("RSA");
            decryptCipher.init(Cipher.DECRYPT_MODE, privateKey);
            return Base64.getEncoder().encodeToString(decryptCipher.doFinal(bytes));
        } catch (NoSuchPaddingException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException | BadPaddingException e) {
            e.printStackTrace();
        }

        return null;
    }

    public static String getPrivateKeyString(PrivateKey secretKey) {
        return Base64.getEncoder().encodeToString(secretKey.getEncoded());
    }

    public static String getPublicKeyString(PublicKey publicKey) {
        return Base64.getEncoder().encodeToString(publicKey.getEncoded());
    }

    /**
     * Encrypts a string using RSA encryption algorithm
     *
     * @param message   the string to be encrypted
     * @param publicKey the public key to be used for encrypting
     * @return The encrypted string
     */

    public static String rsaEncrypt(String message, PublicKey publicKey) {
        byte[] string = Base64.getDecoder().decode(message);
        Cipher encryptCipher = null;
        try {
            encryptCipher = Cipher.getInstance("RSA");
            encryptCipher.init(Cipher.ENCRYPT_MODE, publicKey);

            byte[] cipherText = encryptCipher.doFinal(string);

            return Base64.getEncoder().encodeToString(cipherText);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException | IllegalBlockSizeException | BadPaddingException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static PublicKey keyFromString(String key) {
        byte[] publicBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance("RSA");
            return keyFactory.generatePublic(keySpec);
        } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
            e.printStackTrace();
        }
        return null;
    }
}
