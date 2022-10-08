// package encryption;
//
// import javax.crypto.BadPaddingException;
// import javax.crypto.Cipher;
// import javax.crypto.IllegalBlockSizeException;
// import javax.crypto.NoSuchPaddingException;
// import java.security.*;
// import java.security.interfaces.RSAPrivateKey;
// import java.security.interfaces.RSAPublicKey;
// import java.security.spec.InvalidKeySpecException;
// import java.security.spec.PKCS8EncodedKeySpec;
// import java.security.spec.X509EncodedKeySpec;
// import java.util.Base64;
//
// public class Temp {
//     public static final String CIPHER_ALGORITHM = "RSA/ECB/PKCS1Padding";
//     private static final String PUBLIC_KEY = "RSAPublicKey";
//     private static final String PRIVATE_KEY = "RSAPrivateKey";
//     public static final int KEY_SIZE = 2048;
//     public static final String PLAIN_TEXT = "13407";
//     private PrivateKey privateKey = null;
//     private PublicKey publicKey = null;
//
//     public RSAEncryption() {
//         int keySize = 1024;
//         KeyPair keyPair = null;
//         try {
//             KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
//             keyPairGenerator.initialize(keySize);
//             keyPair = keyPairGenerator.generateKeyPair();
//
//         } catch (NoSuchAlgorithmException e) {
//             e.printStackTrace();
//         }
//         assert (keyPair != null);
//
//         RSAPublicKey publicKey = (RSAPublicKey) keyPair.getPublic();
//         RSAPrivateKey privateKey = (RSAPrivateKey) keyPair.getPrivate();
//
//         // Map<String, byte[]> keyMap = new HashMap<String, byte[]>();
//         // keyMap.put(PUBLIC_KEY, publicKey.getEncoded());
//         // keyMap.put(PRIVATE_KEY, privateKey.getEncoded());
//
//         this.privateKey = (RSAPrivateKey) keyPair.getPrivate();
//         this.publicKey = (RSAPublicKey) keyPair.getPublic();
//     }
//
//     public void RSAEncryption(String rsaSk, String rsaPk) {
//         byte[] secretBytes = Base64.getDecoder().decode(rsaSk);
//         byte[] publicBytes = Base64.getDecoder().decode(rsaPk);
//
//         PKCS8EncodedKeySpec privateKeySpec = new PKCS8EncodedKeySpec(secretBytes);
//         X509EncodedKeySpec publicKeySpec = new X509EncodedKeySpec(publicBytes);
//
//         try {
//             KeyFactory factory = KeyFactory.getInstance("RSA");
//             PrivateKey privateKey = factory
//                     .generatePrivate(privateKeySpec);
//             PublicKey publicKey = factory.generatePublic(publicKeySpec);
//             this.privateKey = privateKey;
//             this.publicKey = publicKey;
//
//
//         } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
//             e.printStackTrace();
//         }
//
//     }
//
//
//     public String rsaDecrypt(String encryptedMessage) {
//
//         byte[] bytes = Base64.getDecoder().decode(encryptedMessage);
//
//         try {
//             Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
//             cipher.init(Cipher.DECRYPT_MODE, this.privateKey);
//             return new String(cipher.doFinal(bytes));
//         } catch (NoSuchAlgorithmException | NoSuchPaddingException
//                 | InvalidKeyException | IllegalBlockSizeException
//                 | BadPaddingException e) {
//             e.printStackTrace();
//         }
//         return null;
//     }
//
//     public PrivateKey getPrivateKey() {
//         return privateKey;
//     }
//
//     public PublicKey getPublicKey() {
//         return publicKey;
//     }
//
//     /**
//      * Decrypts a message with the RSA encryption algorithm
//      *
//      * @param encryptedMessage the message to be decrypted
//      * @param privateKey       the private key to decrypt the message
//      * @return the decrypted string
//      */
//     public static String rsaDecrypt(String encryptedMessage, PrivateKey privateKey) {
//
//         try {
//             Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
//             cipher.init(Cipher.DECRYPT_MODE, privateKey);
//             return new String(cipher.doFinal(encryptedMessage.getBytes()));
//         } catch (NoSuchAlgorithmException | NoSuchPaddingException
//                 | InvalidKeyException | IllegalBlockSizeException
//                 | BadPaddingException e) {
//             e.printStackTrace();
//         }
//         return null;
//
//
//     }
//
//     public static String getPrivateKeyString(PrivateKey secretKey) {
//         return Base64.getEncoder().encodeToString(secretKey.getEncoded());
//     }
//
//     public static String getPublicKeyString(PublicKey publicKey) {
//
//         return Base64.getEncoder().encodeToString(publicKey.getEncoded());
//     }
//
//     /**
//      * Encrypts a string using RSA encryption algorithm
//      *
//      * @param message   the string to be encrypted
//      * @param publicKey the public key to be used for encrypting
//      * @return The encrypted string
//      */
//
//     public static String rsaEncrypt(String message, PublicKey publicKey) {
//
//         try {
//             Cipher cipher = Cipher.getInstance(CIPHER_ALGORITHM);
//             cipher.init(Cipher.ENCRYPT_MODE, publicKey);
//             return Base64.getEncoder().encodeToString(cipher.doFinal(message.getBytes()));
//         } catch (NoSuchAlgorithmException | NoSuchPaddingException
//                 | InvalidKeyException | IllegalBlockSizeException
//                 | BadPaddingException e) {
//             e.printStackTrace();
//         }
//         return null;
//
//
//     }
//
//
//     public static PublicKey keyFromString(String key) {
//
//         byte[] publicBytes = Base64.getDecoder().decode(key);
//
//         X509EncodedKeySpec x509EncodedKeySpec = new X509EncodedKeySpec(publicBytes);
//
//         try {
//             KeyFactory factory = KeyFactory.getInstance("RSA");
//             PublicKey publicKey = factory.generatePublic(x509EncodedKeySpec);
//             return publicKey;
//         } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
//             e.printStackTrace();
//         }
//         return null;
//
//
//     }
// }
