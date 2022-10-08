package encryption;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.*;

import java.util.Arrays;


public class HashDemo {
    public static void main(String[] args) throws Exception {
        Digest bcSha1 = new SHA1Digest();
        String message = "hello";
        byte[] bmessage = message.getBytes();
        System.out.println(Arrays.toString(bmessage));
        bcSha1.update(bmessage, 0, bmessage.length);
        byte[] value = new byte[bcSha1.getDigestSize()];
        bcSha1.doFinal(value, 0);

        System.out.println(Arrays.toString(value));
        System.out.println(bytesToHexFun2(value));

    }

    public static String Sha224(String message) {
        Digest bcSha224 = new SHA224Digest();
        byte[] bmessage = message.getBytes();
        System.out.println(Arrays.toString(bmessage));
        bcSha224.update(message.getBytes(), 0, bmessage.length);
        byte[] value = new byte[bcSha224.getDigestSize()];
        bcSha224.doFinal(value, 0);
        return bytesToHexFun2(value);
    }

    public static String Sha256(String message) {
        Digest bcSha256 = new SHA256Digest();
        byte[] bmessage = message.getBytes();
        // System.out.println(Arrays.toString(bmessage));
        bcSha256.update(bmessage, 0, bmessage.length);
        byte[] value = new byte[bcSha256.getDigestSize()];
        bcSha256.doFinal(value, 0);
        return bytesToHexFun2(value);
    }

    public static String Sha3_(String message) {
        Digest bcSha3 = new SHA3Digest();
        byte[] bmessage = message.getBytes();
        System.out.println(Arrays.toString(bmessage));
        bcSha3.update(bmessage, 0, bmessage.length);
        byte[] value = new byte[bcSha3.getDigestSize()];
        bcSha3.doFinal(value, 0);
        return bytesToHexFun2(value);
    }

    public static String Sha512_(String message) {
        Digest bcSha512 = new SHA512Digest();
        byte[] bmessage = message.getBytes();
        System.out.println(Arrays.toString(bmessage));
        bcSha512.update(bmessage, 0, bmessage.length);
        byte[] value = new byte[bcSha512.getDigestSize()];
        bcSha512.doFinal(value, 0);
        return bytesToHexFun2(value);
    }

    public static String MD5(String message) {
        Digest bcMD5 = new MD5Digest();
        byte[] bmessage = message.getBytes();
        System.out.println(Arrays.toString(bmessage));
        bcMD5.update(message.getBytes(), 0, bmessage.length);
        byte[] value = new byte[bcMD5.getDigestSize()];
        bcMD5.doFinal(value, 0);
        return bytesToHexFun2(value);
    }

    public static String Sm3_(String message) {
        Digest bcSha512 = new SM3Digest();
        byte[] bmessage = message.getBytes();
        System.out.println(Arrays.toString(bmessage));
        bcSha512.update(bmessage, 0, bmessage.length);
        byte[] value = new byte[bcSha512.getDigestSize()];
        bcSha512.doFinal(value, 0);
        return bytesToHexFun2(value);
    }


    public static String bytesToHexFun2(byte[] bytes) {
        char[] HEX_CHAR = {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
        char[] buf = new char[bytes.length * 2];
        int index = 0;
        for (byte b : bytes) { // 利用位运算进行转换，可以看作方法一的变种
            buf[index++] = HEX_CHAR[b >>> 4 & 0xf];
            buf[index++] = HEX_CHAR[b & 0xf];
        }

        return new String(buf);
    }


}
