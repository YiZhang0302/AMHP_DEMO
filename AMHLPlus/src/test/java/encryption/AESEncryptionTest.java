package encryption;//package encryption;


import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import javax.crypto.SecretKey;

public class AESEncryptionTest {

    @Test
   public void testEncryptDecrypt() {
       AESEncryption aesEncryption = new AESEncryption();

       SecretKey secretKey = aesEncryption.getAESKey();

       String message = "Darth Vader: Ah! Obi-Wan Kenobi: YOU WERE THE CHOSEN ONE! It was said that you would destroy the Sith, not join them, bring balance to the force, not leave it in darkness.";

       String encryptedMessage = aesEncryption.encrypt(message, secretKey);

       String keyString = aesEncryption.getAesKeyString(secretKey);
       SecretKey newSk = aesEncryption.formatAesKey(keyString);

       String decryptedMessage = aesEncryption.decrypt(encryptedMessage, newSk);

       Assertions.assertNotEquals(message, encryptedMessage);
       Assertions.assertEquals(message, decryptedMessage);

   }

}
