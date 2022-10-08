package org.aion.tetryon;


import org.junit.Test;

import java.math.BigInteger;
import java.util.concurrent.TimeUnit;

public class MyPairingTest {
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
            Fp exp = new Fp(new BigInteger("10857046999023057135944570762232829481370756359578518086990519993285655852781", 10));
            long ms = TimeUnit.MILLISECONDS.convert(System.nanoTime() - start, TimeUnit.NANOSECONDS);
            System.out.println("ecPair pairingProd2 test took " + ms + " ms");
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

        // assertTrue(r);
    }
}
