package encryption;

import org.bouncycastle.crypto.params.ECDomainParameters;
import org.bouncycastle.crypto.params.ECPrivateKeyParameters;
import org.bouncycastle.crypto.params.ECPublicKeyParameters;
import org.bouncycastle.math.ec.ECConstants;
import org.bouncycastle.math.ec.ECCurve;
import org.bouncycastle.math.ec.ECPoint;
import org.bouncycastle.util.encoders.Hex;
import tools.MyUtils;


import java.math.BigInteger;
import java.security.*;


/**
 * @Author archer_oneee
 * @Description //TODO None
 * @Date @date 2022-09-08 09:04
 **/
public class ECElGamalEncryption {

    static BigInteger  n = new BigInteger("6277101735386680763835789423176059013767194773182842284081");

    public static ECCurve.Fp curve = new ECCurve.Fp(
            // q
            new BigInteger("6277101735386680763835789423207666416083908700390324961279"),
            // a
            new BigInteger("fffffffffffffffffffffffffffffffefffffffffffffffc", 16),
            // b
            new BigInteger("64210519e59c80e70fa7e9ab72243049feb8deecc146b9b1", 16),
            n, ECConstants.ONE);



    // G
    public static ECDomainParameters params = new ECDomainParameters(
            curve,
            curve.decodePoint(Hex.decode("03188da80eb03090f67cbf20eb43a18800f4ff0afd82ff1012")),
            n);
    // // Q
    // public ECPublicKeyParameters pubKey = new ECPublicKeyParameters(
    //         curve.decodePoint(Hex.decode("0262b12d60690cdcf330babab6e69763b471f994dd702d16a5")),
    //         params);
    // // d
    // public ECPrivateKeyParameters priKey = new ECPrivateKeyParameters(
    //         new BigInteger("651056770906015076056810763456358567190100156695615665659"),
    //         params);

    public ECPrivateKeyParameters priKey = null;
    public ECPublicKeyParameters pubKey = null;


    public static BigInteger randomNum(){
        BigInteger n = new BigInteger("6277101735386680763835789423176059013767194773182842284081");
        BigInteger r = new BigInteger(n.toByteArray().length - 1, new SecureRandom());
        return r;
    }

    public ECElGamalEncryption() {
        BigInteger sk = ECElGamalEncryption.randomNum();
        // d
        ECPrivateKeyParameters priKey = new ECPrivateKeyParameters(
                sk,
                params);

        // Q
        ECPublicKeyParameters pubKey = new ECPublicKeyParameters(
                priKey.getParameters().getG().multiply(sk),
                params);

        this.priKey = priKey;
        this.pubKey = pubKey;
    }

    public ECElGamalEncryption(String sk, String pk) {

            this.priKey = new ECPrivateKeyParameters(
                    new BigInteger(sk,16),
                    params);
            this.pubKey  = new ECPublicKeyParameters(
                curve.decodePoint(Hex.decode(pk)),
                params);

    }



    public String ECElGEncrypt(final String m) {
        BigInteger r = randomNum();

        // c1 = [r] * G
        ECPoint C1 = priKey.getParameters().getG().multiply(r);
        String hexC1 = Hex.toHexString(C1.getEncoded(true));

        // Y = [x] * G
        ECPoint pub = priKey.getParameters().getG().multiply(priKey.getD());
        // [r] * Y
        ECPoint r_Y = pub.multiply(r);


        String hashValue = HashDemo.Sha256(r_Y.normalize().toString());
        String C2 = MyUtils.xor(hashValue, m);
        return hexC1+"@@@" +C2;
    }

    public String ECElGDecrypt(final String encMessage) {
        String []Ci = encMessage.split("@@@");
        ECPoint C1 =  curve.decodePoint(Hex.decode(Ci[0]));

        ECPoint x_C1 = C1.multiply(priKey.getD());
        String hashValue = HashDemo.Sha256(x_C1.normalize().toString());

        String m = MyUtils.xor(Ci[1], hashValue);
        return m;
    }



    public ECPrivateKeyParameters getPrivateKey() {
        return priKey;
    }

    public ECPublicKeyParameters getPublicKey() {
        return pubKey;
    }


    public static String ECElGDecrypt(String encryptedMessage, ECPrivateKeyParameters priKey) {
        String []Ci = encryptedMessage.split("@@@");
        ECPoint C1 =  curve.decodePoint(Hex.decode(Ci[0]));

        ECPoint x_C1 = C1.multiply(priKey.getD());
        String hashValue = HashDemo.Sha256(x_C1.normalize().toString());

        String m = MyUtils.xor(Ci[1], hashValue);
        return m;
    }

    public static String getPrivateKeyString(ECPrivateKeyParameters priKey) {
        return priKey.getD().toString(16);
    }

    public static String getPublicKeyString(ECPublicKeyParameters pubKey) {
        return Hex.toHexString(pubKey.getQ().getEncoded(true));
    }



    public static String ECElGEncrypt(String m, ECPublicKeyParameters pubKey) {
        BigInteger r = randomNum();

        // c1 = [r] * G
        ECPoint C1 = pubKey.getParameters().getG().multiply(r);
        String hexC1 = Hex.toHexString(C1.getEncoded(true));

        // [r] * Y
        ECPoint r_Y = pubKey.getQ().multiply(r);


        String hashValue = HashDemo.Sha256(r_Y.normalize().toString());
        String C2 = MyUtils.xor(hashValue, m);
        return hexC1+"@@@" +C2;
    }

    public static ECPublicKeyParameters keyFromString(String hexKey) {
        ECPublicKeyParameters pubKey = new ECPublicKeyParameters(
                curve.decodePoint(Hex.decode(hexKey)),
                params);
        return pubKey;
    }


}
