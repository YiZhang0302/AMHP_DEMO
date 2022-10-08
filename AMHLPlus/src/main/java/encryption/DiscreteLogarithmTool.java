package encryption;


import java.math.BigInteger;
import java.util.Random;

public class DiscreteLogarithmTool {
    // private BigInteger  g1024 = new BigInteger("1db17639cdf96bc4eabba19454f0b7e5bd4e14862889a725c96eb61048dcd676ceb303d586e30f060dbafd8a571a39c4d823982117da5cc4e0f89c77388b7a08896362429b94a18a327604eb7ff227bffbc83459ade299e57b5f77b50fb045250934938efa145511166e3197373e1b5b1e52de713eb49792bedde722c6717abf", 16);
    // private BigInteger  p1024 = new BigInteger("a00e283b3c624e5b2b4d9fbc2653b5185d99499b00fd1bf244c6f0bb817b4d1c451b2958d62a0f8a38caef059fb5ecd25d75ed9af403f5b5bdab97a642902f824e3c13789fed95fa106ddfe0ff4a707c85e2eb77d49e68f2808bcea18ce128b178cd287c6bc00efa9a1ad2a673fe0dceace53166f75b81d6709d5f8af7c66bb7", 16);

    public static BigInteger p = new BigInteger("a00e283b3c624e5b2b4d9fbc2653b5185d99499b00fd1bf244c6f0bb817b4d1c451b2958d62a0f8a38caef059fb5ecd25d75ed9af403f5b5bdab97a642902f824e3c13789fed95fa106ddfe0ff4a707c85e2eb77d49e68f2808bcea18ce128b178cd287c6bc00efa9a1ad2a673fe0dceace53166f75b81d6709d5f8af7c66bb7",16);
    public static BigInteger g = new BigInteger("1db17639cdf96bc4eabba19454f0b7e5bd4e14862889a725c96eb61048dcd676ceb303d586e30f060dbafd8a571a39c4d823982117da5cc4e0f89c77388b7a08896362429b94a18a327604eb7ff227bffbc83459ade299e57b5f77b50fb045250934938efa145511166e3197373e1b5b1e52de713eb49792bedde722c6717abf",16);

    public static BigInteger getRandomNum() {
        int len = DiscreteLogarithmTool.p.bitLength();

        Random random = new Random();
        BigInteger randNum = new BigInteger(len, random);

        return randNum.mod(DiscreteLogarithmTool.p);
    }


    public static BigInteger modAdd(BigInteger a, BigInteger b){
        return a.add(b).mod(DiscreteLogarithmTool.p.subtract(new BigInteger("1")));
    }

    public static BigInteger modSub(BigInteger a, BigInteger b){
        // a - b
        return a.subtract(b).mod(DiscreteLogarithmTool.p.subtract(new BigInteger("1")));
    }

    public static BigInteger modPow(BigInteger g, BigInteger e){
        return g.modPow(e, DiscreteLogarithmTool.p);
    }

    public static BigInteger modPow(BigInteger e){
        return DiscreteLogarithmTool.g.modPow(e, DiscreteLogarithmTool.p);
    }

    public static BigInteger modMul(BigInteger a, BigInteger b){
        // a * b
        return a.multiply(b).mod(DiscreteLogarithmTool.p);
    }


    public static BigInteger modInverseNum(BigInteger num){

        return num.modInverse(DiscreteLogarithmTool.p);
    }
}
