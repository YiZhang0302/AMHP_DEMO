package web3j;

import org.bouncycastle.util.encoders.Hex;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.Hash;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;

import java.math.BigInteger;
import java.security.SignatureException;

public class Signature {

    public static String getSignDataHash(String message)  {


        // 原文摘要字节数组
        byte[] contentHashBytes = Hash.sha3(message.getBytes());

        // 原文摘要16进制字符串
        String contentHashHex = Hex.toHexString(contentHashBytes);
        return contentHashHex;

    }


    public static String sign(String message){
        // 钱包私钥
        final String priKey = "e62248374af86aa480f9cebd44f04cd02b915130d4fbda885a201488257b0a17";
        // 钱包地址
        final String walletAddress = "0x5ebacac108d665819398e5c37e12b0162d781398";

        // 原文
        String content = message;

        // 原文摘要字节数组
        byte[] contentHashBytes = Hash.sha3(content.getBytes());

        // 原文摘要16进制字符串
        String contentHashHex = Hex.toHexString(contentHashBytes);

        Credentials credentials = Credentials.create(priKey);
        Sign.SignatureData signMessage = Sign.signPrefixedMessage(contentHashBytes, credentials.getEcKeyPair());


        String R = Hex.toHexString(signMessage.getR());
        byte[] originR = signMessage.getR();
        byte[] R_ = Hex.decode(R);

        for (int i = 0; i < originR.length; i++) {
            if (originR[i]!= R_[i]){
                System.out.println("R error");
            }
        }

        String S = Hex.toHexString(signMessage.getS());
        byte[] originS = signMessage.getS();
        byte[] S_ = Hex.decode(S);
        for (int i = 0; i < originS.length; i++) {
            if (originS[i]!= S_[i]){
                System.out.println("S error");
            }
        }

        String V = Hex.toHexString(signMessage.getV());
        byte[] originV = signMessage.getV();
        byte[] V_ = Hex.decode(V);
        for (int i = 0; i < originV.length; i++) {
            if (originV[i]!= V_[i]){
                System.out.println("V error");
            }
        }



        // 签名后的字符串
        String signStr = Hex.toHexString(signMessage.getR()) + Hex.toHexString(signMessage.getS()) + Hex.toHexString(signMessage.getV());
        return signStr;
    }


    public static String sign(String message, String priKey){

        // 原文
        String content = message;

        // 原文摘要字节数组
        byte[] contentHashBytes = Hash.sha3(content.getBytes());

        // 原文摘要16进制字符串
        String contentHashHex = Hex.toHexString(contentHashBytes);

        Credentials credentials = Credentials.create(priKey);
        Sign.SignatureData signMessage = Sign.signPrefixedMessage(contentHashBytes, credentials.getEcKeyPair());


        String R = Hex.toHexString(signMessage.getR());
        byte[] originR = signMessage.getR();
        byte[] R_ = Hex.decode(R);

        for (int i = 0; i < originR.length; i++) {
            if (originR[i]!= R_[i]){
                System.out.println("R error");
            }
        }

        String S = Hex.toHexString(signMessage.getS());
        byte[] originS = signMessage.getS();
        byte[] S_ = Hex.decode(S);
        for (int i = 0; i < originS.length; i++) {
            if (originS[i]!= S_[i]){
                System.out.println("S error");
            }
        }

        String V = Hex.toHexString(signMessage.getV());
        byte[] originV = signMessage.getV();
        byte[] V_ = Hex.decode(V);
        for (int i = 0; i < originV.length; i++) {
            if (originV[i]!= V_[i]){
                System.out.println("V error");
            }
        }



        // 签名后的字符串
        String signStr = Hex.toHexString(signMessage.getR()) + Hex.toHexString(signMessage.getS()) + Hex.toHexString(signMessage.getV());
        return signStr;
    }

    public static boolean verify(String message, String signStr, String address){


        // 原文摘要字节数组
        byte[] contentHashBytes = Hash.sha3(message.getBytes());

        // 原文摘要16进制字符串
        String contentHashHex = Hex.toHexString(contentHashBytes);

        byte[] signatureR = Hex.decode(signStr.substring(0,64)) ;
        byte[] signatureS = Hex.decode(signStr.substring(64,128)) ;
        byte[] signatureV = Hex.decode(signStr.substring(128,130)) ;



        try {
            // 原文摘要 添加 ETH签名头信息后再生成摘要
            byte[] messageHash = Sign.getEthereumMessageHash(Hex.decode(contentHashHex));

            //通过摘要和签名后的数据，还原公钥
            Sign.SignatureData signatureData = new Sign.SignatureData(signatureV, signatureR, signatureS);
            BigInteger publicKey = Sign.signedMessageHashToKey(messageHash, signatureData);

            //还原地址 0x5ebacac108d665819398e5c37e12b0162d781398
            String parseAddress = "0x" + Keys.getAddress(publicKey);

            if (parseAddress.equals(address)){
                return true;
            }else {
                return false;
            }
        } catch (SignatureException e) {
            e.printStackTrace();
        }
        return false;
    }


    public static void main(String[] args) {
        String address = "0x5ebacac108d665819398e5c37e12b0162d781398";
        String message = "abc";
        String sig = Signature.sign(message);
        String messageHash = Signature.getSignDataHash(message);
        Signature.verify(message, sig, address);


    }
}
