package tools;

import encryption.ECElGamalEncryption;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.Keys;
import org.web3j.crypto.WalletUtils;

import java.io.*;
import java.math.BigInteger;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Properties;

public class InitProperty1 {
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

    // public void initNormalNodeKey() throws IOException {
    //     Properties prop = new Properties();
    //
    //     InputStream in = new BufferedInputStream(new FileInputStream(fileName));
    //     prop.load(in);
    //     OutputStream out = new FileOutputStream(fileName);
    //
    //     int normalNodeStart = Integer.parseInt(prop.getProperty("RELAY_NODE_PORT_START"));
    //     int nodeNumber = 8;
    //
    //     for (int i = 0; i < nodeNumber; i++) {
    //         try {
    //             String nodeName = "NODE_" + (normalNodeStart + i * 10);
    //             RSAEncryption nodeRsa = new RSAEncryption();
    //             String nodeRsaPk = RSAEncryption.getPublicKeyString(nodeRsa.getPublicKey());
    //             String nodeRsaSk = RSAEncryption.getPrivateKeyString(nodeRsa.getPrivateKey());
    //             AESEncryption nodeAES = new AESEncryption();
    //             String nodeAesKey = AESEncryption.getAesKeyString(nodeAES.getAESKey());
    //
    //             ECKeyPair ecKeyPair = Keys.createEcKeyPair();
    //             String password = "abc123";
    //             File file = new File("D:\\geth\\db1\\keystore");
    //             String keyFileName = WalletUtils.generateWalletFile(password, ecKeyPair, file, true);
    //             // System.out.println(fileName);
    //
    //             prop.put(nodeName + "_SIGN_KEY_FILENAME", keyFileName);
    //             prop.put(nodeName + "_RSA_PRIVATE_KEY", nodeRsaSk);
    //             prop.put(nodeName + "_RSA_PUBLIC_KEY", nodeRsaPk);
    //             prop.put(nodeName + "_AES_KEY", nodeAesKey);
    //         }catch (Exception e){
    //
    //         }
    //
    //
    //     }
    //     prop.store(out, "config file");
    //
    // }


    public void initNormalNodeKey() throws IOException {
        Properties prop = new Properties();

        InputStream in = new BufferedInputStream(new FileInputStream(fileName));
        prop.load(in);
        OutputStream out = new FileOutputStream(fileName);

        int normalNodeStart = Integer.parseInt(prop.getProperty("RELAY_NODE_PORT_START"));
        int nodeNumber = 8;

        for (int i = 0; i < nodeNumber; i++) {
            try {
                String nodeName = "NODE_" + (normalNodeStart + i * 10);
                ECElGamalEncryption nodeElG = new ECElGamalEncryption();

                String nodeElGPk = ECElGamalEncryption.getPublicKeyString(nodeElG.getPublicKey());
                String nodeElGSk = ECElGamalEncryption.getPrivateKeyString(nodeElG.getPrivateKey());

                // AESEncryption nodeAES = new AESEncryption();
                // String nodeAesKey = AESEncryption.getAesKeyString(nodeAES.getAESKey());
                //
                // ECKeyPair ecKeyPair = Keys.createEcKeyPair();
                // String password = "abc123";
                // File file = new File("D:\\geth\\db1\\keystore");
                // String keyFileName = WalletUtils.generateWalletFile(password, ecKeyPair, file, true);
                // System.out.println(fileName);

                // prop.put(nodeName + "_SIGN_KEY_FILENAME", keyFileName);
                prop.put(nodeName + "_ElG_PRIVATE_KEY", nodeElGSk);
                prop.put(nodeName + "_ElG_PUBLIC_KEY", nodeElGPk);
                // prop.put(nodeName + "_AES_KEY", nodeAesKey);
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

    public void initConfig() throws IOException {
        this.initNormalNodeKey();
        this.initServer();
        this.initClient();

    }






    public static void main(String[] args) throws IOException, NoSuchProviderException, InvalidAlgorithmParameterException, NoSuchAlgorithmException, CipherException {
        InitProperty initProperty = new InitProperty();
        initProperty.initConfig();
    }

}
