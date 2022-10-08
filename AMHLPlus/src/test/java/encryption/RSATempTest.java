package encryption;


import org.junit.Test;

public class RSATempTest {
    @Test
    public void testRsaEncryptAndDecrypt() throws Exception {

        String message = "12140";

        RSATemp encryption = new RSATemp();
        String pubStr = encryption.getPublicKeyString(encryption.getPublicKey());
        String priStr = encryption.getPrivateKeyString(encryption.getPrivateKey());

        String encrypted = encryption.rsaEncrypt(message, encryption.keyFromString(pubStr));

        RSATemp encryption1 = new RSATemp(priStr, pubStr);
        String result = encryption.rsaDecrypt(encrypted, encryption1.getPrivateKey());
        System.out.println("result = " + result);
        assert (message.equals(result));
    }
}