package org.aion.tetryon;

import org.junit.Test;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Base64;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

public class GtTest {
    @Test
    public void myPairingTest() {
        Fp g11x = new Fp(new BigInteger("1e462d01d1861f7ee499bf70ab12ade335d98586b52db847ee2ec1e790170e04", 16));
        Fp g11y = new Fp(new BigInteger("14bd807f4e64904b29e874fd824ff16e465b5798b19aafe0cae60a2dbcf91333", 16));
        G1Point g11 = new G1Point(g11x, g11y);


        Fp2 g2x = new Fp2(new BigInteger("10857046999023057135944570762232829481370756359578518086990519993285655852781", 10),
                new BigInteger("11559732032986387107991004021392285783925812861821192530917403151452391805634", 10));
        Fp2 g2y = new Fp2(new BigInteger("8495653923123431417604973247489272438418190587263600148770280649306958101930", 10),
                new BigInteger("4082367875863433681332203403145435568316851327593401208105741076214120093531", 10));
        G2Point g2 = new G2Point(g2x, g2y);

        boolean r = false;

        try {
            long start = System.nanoTime();
            GtPoint gt_point = Pairing.myPairing(g11, g2);
            System.out.println("gt_point");
            System.out.println(gt_point);

            BigInteger exp = new BigInteger("5");
            GtPoint result = Gt.gtPow(gt_point, exp);
            // System.out.println("g pow 5");
            // System.out.println(result);

            BigInteger filed = new BigInteger("21888242871839275222246405745257275088548364400416034343698204186575808495617");
            BigInteger exp_d = exp.modInverse(filed);
            GtPoint new_gt_point = Gt.gtPow(result, exp_d);
            System.out.println("new_gt_point");
            System.out.println(new_gt_point);


            long ms = TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
            System.out.println("ecPair pairingProd2 test took " + ms + " ms");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        // assertTrue(r);
    }

    @Test
    public void GtPowTest() throws Exception {
        Properties prop = new Properties();
        try {
            DataInputStream dataInputStream = new DataInputStream(new FileInputStream("src/main/java/config.properties"));
            prop.load(dataInputStream);
        }catch (IOException e){

        }

        String g1Str = prop.getProperty("G1_POINT");
        String hStr = prop.getProperty("H_POINT");
        BigInteger secret = Util.getRandom();
        G1Point g1 = Util.deserializeG1(Base64.getDecoder().decode(g1Str.getBytes()));
        G2Point g2 = Util.deserializeG2(Base64.getDecoder().decode(hStr.getBytes()));



        GtPoint result = Pairing.myPairing(g1, g2);

        byte [] resultByte = Util.serializeGt(result);
        String resultStr = new String(resultByte, Charset.forName("ISO-8859-1"));

        byte [] resultByte_ = resultStr.getBytes(Charset.forName("ISO-8859-1"));
        result = Util.deserializeGt(resultByte_);
        GtPoint resultSecret = Gt.gtPow(result, secret);
        BigInteger filed = new BigInteger("21888242871839275222246405745257275088548364400416034343698204186575808495617");
        BigInteger exp_d = secret.modInverse(filed);
        GtPoint new_gt_point = Gt.gtPow(result, exp_d);
        int a = 1;

    }

    @Test
    public void nullTest() throws Exception {
        // BigInteger a = new BigInteger("4003d9f0ca9c852a59e89ef1bdff68b845c81d653124bd3137bec77e921f9b", 16);
        BigInteger a = new BigInteger("10", 16);
        byte[] by = a.toByteArray();
        byte[] newBy = new byte[by.length+1];
        System.arraycopy(by, 0, newBy,newBy.length-by.length, by.length);

        BigInteger newA = new BigInteger(newBy);
        assert(newA.equals(a));
        int len = by.length;
        int c = 1;


    }
}
