package encryption;


import org.junit.Test;

public class RSAEncryptionTest {
    @Test
    public void testRsaEncryptAndDecrypt() throws Exception {

        String message = "1021";

        RSAEncryption encryption = new RSAEncryption();
        String pubStr = encryption.getPublicKeyString(encryption.getPublicKey());
        String priStr = encryption.getPrivateKeyString(encryption.getPrivateKey());

        long start = System.currentTimeMillis();

        String encrypted = encryption.rsaEncrypt(message, encryption.keyFromString(pubStr));
        long end = System.currentTimeMillis();
        long cost = end - start;
        System.out.println("enc cost = " + cost);
        RSAEncryption encryption1 = new RSAEncryption(priStr, pubStr);

        start = System.currentTimeMillis();
        String result = encryption.rsaDecrypt(encrypted, encryption1.getPrivateKey());
        end = System.currentTimeMillis();
        cost = end - start;
        System.out.println("dec cost = " + cost);

        System.out.println("result = " + result);
        assert (message.equals(result));
    }
}