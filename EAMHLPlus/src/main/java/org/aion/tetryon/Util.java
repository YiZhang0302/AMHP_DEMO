package org.aion.tetryon;

import java.math.BigInteger;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Random;

public class Util {

    public static final int FP_SIZE = 32;

    public static BigInteger getRandom() {
        final BigInteger FIELD_MODULUS = new BigInteger("21888242871839275222246405745257275088696311157297823662689037894645226208583");

        Random random = new Random();
        int len = FIELD_MODULUS.bitLength();
        BigInteger randNum = new BigInteger(len, random).mod(FIELD_MODULUS);
        return randNum;
    }

    public static byte[] serializeG1(G1Point p) {
        byte[] data = new byte[FP_SIZE * 2];

        byte[] px = p.x.c0.toByteArray();
        System.arraycopy(px, 0, data, FP_SIZE - px.length, px.length);

        byte[] py = p.y.c0.toByteArray();
        System.arraycopy(py, 0, data, FP_SIZE * 2 - py.length, py.length);

        return data;
    }

    public static G1Point deserializeG1(byte[] data) {
        byte[] pxData = Arrays.copyOfRange(data, 0, FP_SIZE);
        byte[] pyData = Arrays.copyOfRange(data, FP_SIZE, data.length);

        Fp p1x = new Fp(new BigInteger(pxData));
        Fp p1y = new Fp(new BigInteger(pyData));

        G1Point p1 = new G1Point(p1x, p1y);
        return p1;
    }

    public static GtPoint deserializeGt(byte[] data) {
        Fp[] ci = new Fp[12];
        for (int i = 0; i < 12; i++) {
            byte[] cData = Arrays.copyOfRange(data, FP_SIZE * i, FP_SIZE * (i + 1));
            Fp c = new Fp(new BigInteger(cData));
            ci[i] = c;
        }

        GtPoint reuslt = new GtPoint(ci);
        return reuslt;
    }

    public static byte[] serializeGt(GtPoint p) {
        byte[] data = new byte[FP_SIZE * 12]; // zero byte array
        for (int i = 0; i < 12; i++) {
            byte[] c_byte = p.ci[i].c0.toByteArray();
            System.arraycopy(c_byte, 0, data, FP_SIZE * i + FP_SIZE-c_byte.length, c_byte.length);

        }
        return data;
    }

    public static byte[] serializeG2(G2Point p) {
        byte[] data = new byte[FP_SIZE * 4]; // zero byte array

        byte[] px1 = p.x.a.toByteArray();
        System.arraycopy(px1, 0, data, FP_SIZE * 1 - px1.length, px1.length);

        byte[] px2 = p.x.b.toByteArray();
        System.arraycopy(px2, 0, data, FP_SIZE * 2 - px2.length, px2.length);

        byte[] py1 = p.y.a.toByteArray();
        System.arraycopy(py1, 0, data, FP_SIZE * 3 - py1.length, py1.length);

        byte[] py2 = p.y.b.toByteArray();
        System.arraycopy(py2, 0, data, FP_SIZE * 4 - py2.length, py2.length);
        return data;
    }

    public static G2Point deserializeG2(byte[] data) {
        byte[] px1Data = Arrays.copyOfRange(data, 0, FP_SIZE * 1);
        byte[] px2Data = Arrays.copyOfRange(data, FP_SIZE * 1, FP_SIZE * 2);
        byte[] py1Data = Arrays.copyOfRange(data, FP_SIZE * 2, FP_SIZE * 3);
        byte[] py2Data = Arrays.copyOfRange(data, FP_SIZE * 3, FP_SIZE * 4);

        Fp2 px = new Fp2(new BigInteger(px1Data), new BigInteger(px2Data));
        Fp2 py = new Fp2(new BigInteger(py1Data), new BigInteger(py2Data));

        G2Point point = new G2Point(px, py);
        return point;
    }


    public static byte[] serializeScalar(BigInteger scalar) {
        assert (scalar.signum() != -1); // scalar can't be negative (it can be zero or positive)

        byte[] sdata = scalar.toByteArray();
        assert (sdata.length <= FP_SIZE);

        byte[] sdata_aligned = new byte[FP_SIZE];
        System.arraycopy(sdata, 0, sdata_aligned, FP_SIZE - sdata.length, sdata.length);

        return sdata_aligned;

    }

    private static final char[] HEX_ARRAY = "0123456789abcdef".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];

        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }

        return new String(hexChars);
    }

    public static BigInteger inverse(BigInteger e) {

        BigInteger filed = new BigInteger("21888242871839275222246405745257275088548364400416034343698204186575808495617");
        BigInteger eInverse = e.modInverse(filed);


        return eInverse;

    }

    public static GtPoint stringToGtPoint(String gtPointStr){
        byte[] gtPointByte = gtPointStr.getBytes(Charset.forName("ISO-8859-1"));
        return Util.deserializeGt(gtPointByte);

    }

    public static G1Point stringToG1Point(String g1PointStr){
        byte[] g1PointByte = g1PointStr.getBytes(Charset.forName("ISO-8859-1"));
        return Util.deserializeG1(g1PointByte);
    }

    public static String g1PointToString(G1Point g1Point){

        byte[] g1PointByte = Util.serializeG1(g1Point);
        return new String(g1PointByte, Charset.forName("ISO-8859-1"));
    }

    public static String gtPointToString(GtPoint gtPoint){

        byte[] gtPointByte = Util.serializeGt(gtPoint);
        return new String(gtPointByte, Charset.forName("ISO-8859-1"));
    }
}