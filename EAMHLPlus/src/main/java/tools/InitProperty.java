package tools;

import encryption.AESEncryption;
import encryption.ECElGamalEncryption;
import encryption.RSAEncryption;
import jnr.ffi.annotations.In;
import org.aion.tetryon.*;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.WalletUtils;

import java.io.*;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Base64;
import java.util.Properties;

public class InitProperty {
    private static final BigInteger CURVE_ORDER = new BigInteger("21888242871839275222246405745257275088548364400416034343698204186575808495617");
    private static final BigInteger FIELD_MODULUS = new BigInteger("21888242871839275222246405745257275088696311157297823662689037894645226208583");


    String fileName = "src/main/java/config.properties";

    public String getPropertiesValue(String key) throws IOException {
        Properties prop = new Properties();
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(fileName));
            prop.load(in);
        } catch (IOException e) {

        }

        return prop.getProperty(key);
    }


    public void writeProperties(String key, String value) {
        Properties prop = new Properties();
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(fileName));
            prop.load(in);
            prop.setProperty(key, value);
            OutputStream out = new FileOutputStream(fileName);

            prop.store(out, "config file");
        } catch (IOException e) {

        }


    }

    public void initNormalNodeKey() throws IOException {
        Properties prop = new Properties();

        InputStream in = new BufferedInputStream(new FileInputStream(fileName));
        prop.load(in);
        OutputStream out = new FileOutputStream(fileName);

        int normalNodeStart = Integer.parseInt(prop.getProperty("RELAY_NODE_PORT_START"));
        int nodeNumber = 51;

        for (int i = 8; i < nodeNumber; i++) {
            try {
                String nodeName = "NODE_" + (normalNodeStart + i * 10);
                ECElGamalEncryption nodeElG = new ECElGamalEncryption();

                String nodeElGPk = ECElGamalEncryption.getPublicKeyString(nodeElG.getPublicKey());
                String nodeElGSk = ECElGamalEncryption.getPrivateKeyString(nodeElG.getPrivateKey());

                AESEncryption nodeAES = new AESEncryption();
                String nodeAesKey = AESEncryption.getAesKeyString(nodeAES.getAESKey());

                ECKeyPair ecKeyPair = Keys.createEcKeyPair();
                String password = "abc123";
                File file = new File("D:\\geth\\db1\\keystore");
                String keyFileName = WalletUtils.generateWalletFile(password, ecKeyPair, file, true);
                // System.out.println(fileName);

                prop.put(nodeName + "_SIGN_KEY_FILENAME", keyFileName);
                prop.put(nodeName + "_ElG_PRIVATE_KEY", nodeElGSk);
                prop.put(nodeName + "_ElG_PUBLIC_KEY", nodeElGPk);
                prop.put(nodeName + "_AES_KEY", nodeAesKey);
            }catch (Exception e){

            }


        }
        prop.store(out, "config file");

    }

    public void initServer() {
        ECElGamalEncryption serverElG = new ECElGamalEncryption();

        String serverElGPk = ECElGamalEncryption.getPublicKeyString(serverElG.getPublicKey());
        String serverElGSk = ECElGamalEncryption.getPrivateKeyString(serverElG.getPrivateKey());

        // AESEncryption serverAES = new AESEncryption();
        // String serverAesKey = AESEncryption.getAesKeyString(serverAES.getAESKey());

        Properties prop = new Properties();
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(fileName));
            prop.load(in);
            prop.setProperty("SERVER_ELG_PUBLIC_KEY", serverElGPk);
            prop.setProperty("SERVER_ELG_PRIVATE_KEY", serverElGSk);
            // prop.setProperty("SERVER_AES_KEY", serverAesKey);

            // ECKeyPair ecKeyPair = Keys.createEcKeyPair();
            // String password = "abc123";
            // File file = new File("D:\\geth\\db1\\keystore");
            // String keyFileName = WalletUtils.generateWalletFile(password, ecKeyPair, file, true);
            // prop.setProperty("SERVER_SIGN_KEY_FILENAME", keyFileName);


            DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream(fileName));
            prop.store(dataOutputStream, "config file");
        } catch (Exception e) {
            int a = 1;

        }

    }

    public void initClient() {
        ECElGamalEncryption clientElG = new ECElGamalEncryption();

        String clientElGPk = ECElGamalEncryption.getPublicKeyString(clientElG.getPublicKey());
        String clientElGSk = ECElGamalEncryption.getPrivateKeyString(clientElG.getPrivateKey());

        // RSAEncryption clientRsa = new RSAEncryption();
        // String clientRsaPk = RSAEncryption.getPublicKeyString(clientRsa.getPublicKey());
        // String clientRsaSk = RSAEncryption.getPrivateKeyString(clientRsa.getPrivateKey());
        // AESEncryption clientAES = new AESEncryption();
        // String clientAesKey = AESEncryption.getAesKeyString(clientAES.getAESKey());

        Properties prop = new Properties();
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(fileName));
            prop.load(in);
            String port = prop.getProperty("CLIENT_PORT");
            prop.put("CLIENT_ELG_PUBLIC_KEY", clientElGPk);
            prop.put("NODE_" + port + "_ElG_PUBLIC_KEY", clientElGPk);
            prop.put("CLIENT_ELG_SECRET_KEY", clientElGSk);
            prop.put("NODE_" + port + "_ELG_PRIVATE_KEY", clientElGSk);

            // prop.put("CLIENT_AES_KEY", clientAesKey);
            // ECKeyPair ecKeyPair = Keys.createEcKeyPair();
            // String password = "abc123";
            // File file = new File("D:\\geth\\db1\\keystore");
            // String keyFileName = WalletUtils.generateWalletFile(password, ecKeyPair, file, true);
            // prop.put("CLIENT_SIGN_KEY_FILENAME", keyFileName);

            OutputStream out = new FileOutputStream(fileName);

            prop.store(out, "config file");
        } catch (Exception e) {
            int a = 1;
        }

    }

    public void initG1(){
        Properties prop = new Properties();
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(fileName));
            prop.load(in);
            Fp px = new Fp(new BigInteger("1e462d01d1861f7ee499bf70ab12ade335d98586b52db847ee2ec1e790170e04", 16));
            Fp py = new Fp(new BigInteger("14bd807f4e64904b29e874fd824ff16e465b5798b19aafe0cae60a2dbcf91333", 16));
            G1Point G1 = new G1Point(px, py);
            byte [] G1PointByte = Util.serializeG1(G1);
            prop.put("G1_POINT", Base64.getEncoder().encodeToString(G1PointByte));

            OutputStream out = new FileOutputStream(fileName);

            prop.store(out, "config file");

        } catch (IOException e) {

        }


    }

    public void initG2(){

        Properties prop = new Properties();
        try {
            InputStream in = new BufferedInputStream(new FileInputStream(fileName));
            prop.load(in);
            G2Point g2 = new G2Point(
                    new Fp2(
                            new BigInteger("10857046999023057135944570762232829481370756359578518086990519993285655852781"),
                            new BigInteger("11559732032986387107991004021392285783925812861821192530917403151452391805634")
                    ),
                    new Fp2(
                            new BigInteger("8495653923123431417604973247489272438418190587263600148770280649306958101930"),
                            new BigInteger("4082367875863433681332203403145435568316851327593401208105741076214120093531")
                    )
            );
            G2Point hPoint =  G2.ECTwistMul(g2, Util.getRandom());
            byte [] G2PointByte = Util.serializeG2(hPoint);
            prop.put("H_POINT", new String(Base64.getEncoder().encodeToString(G2PointByte)));

            OutputStream out = new FileOutputStream(fileName);

            prop.store(out, "config file");

        } catch (IOException e) {

        }
    }

    public void initSignKey() throws IOException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, NoSuchProviderException, CipherException {
        Properties prop = new Properties();

        InputStream in = new BufferedInputStream(new FileInputStream(fileName));
        prop.load(in);
        OutputStream out = new FileOutputStream(fileName);

        int normalNodeStart = Integer.parseInt(prop.getProperty("RELAY_NODE_PORT_START"));
        int nodeNumber = 1;

        for (int i = 0; i < nodeNumber; i++) {
            String nodeName = "NODE_" + (normalNodeStart + i * 10);
            ECKeyPair ecKeyPair = Keys.createEcKeyPair();
            String privateKey = ecKeyPair.getPrivateKey().toString(16);
            String publicKey = ecKeyPair.getPublicKey().toString(16);
            String address = Keys.getAddress(publicKey);
            // System.out.println("Your private key:"+privateKey);
            // System.out.println("Your public key:"+publicKey);
            // System.out.println("Your address:"+address);
            String password = "abc123";
            File file = new File("D:\\geth\\db1\\keystore");
            String fileName = WalletUtils.generateWalletFile(password, ecKeyPair, file, true);
            System.out.println(fileName);

            prop.setProperty(nodeName + "_SIGN_KEY_FILENAME", fileName);
        }
        prop.store(out, "config file");
    }

    public void initConfig() throws IOException, NoSuchAlgorithmException, CipherException, InvalidAlgorithmParameterException, NoSuchProviderException {
        this.initNormalNodeKey();
        // this.initServer();
        // this.initClient();
        //this.initG1();
        //this.initG2();
        // this.initSignKey();
    }

    public void test(){
        try {
            Properties p = new Properties();
            // DataInputStream dataInputStream = new DataInputStream(new FileInputStream("src/main/java/test.properties"));
            // p.load(dataInputStream);
            ECKeyPair ecKeyPair = Keys.createEcKeyPair();
            String privateKey = ecKeyPair.getPrivateKey().toString(16);
            String publicKey = ecKeyPair.getPublicKey().toString(16);
            String address = Keys.getAddress(publicKey);
            // System.out.println("Your private key:"+privateKey);
            // System.out.println("Your public key:"+publicKey);
            // System.out.println("Your address:"+address);
            String password = "abc123";
            File file = new File("D:\\geth\\db1\\keystore");
            String fileName = WalletUtils.generateWalletFile(password, ecKeyPair, file, true);
            System.out.println(fileName);

            p.setProperty( "SIGN_KEY_FILENAME", fileName);
            p.setProperty("abc", "123");

            DataOutputStream dataOutputStream = new DataOutputStream(new FileOutputStream("F:\\20220812\\EAMHLPlus\\src\\main\\java\\test.properties"));
            p.store(dataOutputStream, "config file");

        }catch (Exception e){

        }
    }


    public static void main(String[] args) throws IOException, NoSuchProviderException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, CipherException {
        InitProperty initProperty = new InitProperty();
        initProperty.initConfig();
        // initProperty.test();
    }

}
