package encryption;

import org.junit.Test;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;

/**
 * @Author archer_oneee
 * @Description //TODO None
 * @Date @date 2022-09-18 23:21
 **/
public class ECElGamalEncryptionTest {
    @Test
    public void testEncryption(){

        String m = "hello world";
        System.out.println("m = " + m);
        String m1 = "hello world";
        int result1 =  m.compareTo(m1);
        ECElGamalEncryption ecElGamalEncryption = new ECElGamalEncryption();
        BigInteger d = ecElGamalEncryption.getPrivateKey().getD();
        System.out.println("d = " + d);
        long start = System.currentTimeMillis();
        String encM =ecElGamalEncryption.ECElGEncrypt(m);
        long end = System.currentTimeMillis();
        long cost = end - start;
        System.out.println("enc cost = " + cost);

        start = System.currentTimeMillis();
        String m_=ecElGamalEncryption.ECElGDecrypt(encM);
         end = System.currentTimeMillis();
         cost = end - start;
        System.out.println("dec cost = " + cost);

        // System.out.println("m_ = " + m_);

    }
}
