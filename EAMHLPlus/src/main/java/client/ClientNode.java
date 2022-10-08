package client;

import contract.ChannelState;
import contract.Partner;
import encryption.AESEncryption;
import encryption.ECElGamalEncryption;
import encryption.RSAEncryption;
import org.aion.tetryon.*;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.WalletUtils;
import tools.MyUtils;
import web3j.Signature;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.Base64;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Logger;

public class ClientNode {
    String localIp;
    int localPort;
    ECElGamalEncryption ecElGamalEncryption;
    Logger logger;
    String configFile = "src/main/java/config.properties";
    int messageLenMax = 1024;
    HashMap<String, String> lockMessage;
    int myToken;
    int partnerToken;
    G1Point g1;
    G2Point h;
    BigInteger secret;
    HashMap<String, String> unlockMessage;
    public HashMap<String, String> nodeInfo;
    public ChannelState channel;

    public ClientNode() {

        Properties prop = null;
        try {
            prop = new Properties();
            DataInputStream din = new DataInputStream(new FileInputStream(configFile));
            prop.load(din);
        } catch (IOException e) {
            logger.info("Can not load the config properties");
        }


        this.localIp = prop.getProperty("CLIENT_IP");
        this.localPort = Integer.parseInt(prop.getProperty("CLIENT_PORT"));
        this.ecElGamalEncryption = new ECElGamalEncryption(prop.getProperty("CLIENT_ELG_SECRET_KEY"),prop.getProperty("CLIENT_ELG_PUBLIC_KEY"));
        this.g1 = Util.deserializeG1(Base64.getDecoder().decode(prop.getProperty("G1_POINT")));
        this.h = Util.deserializeG2(Base64.getDecoder().decode(prop.getProperty("H_POINT")));

        this.logger = Logger.getLogger("clientLog");

    }

    public void initNodeChannel(String prevAddress){

        assert(this.nodeInfo != null);
        Partner PartnerA = new Partner(prevAddress, new BigInteger("100"));

        Partner PartnerB = new Partner(nodeInfo.get("address"), new BigInteger("100"));

        this.channel = new ChannelState(PartnerA, PartnerB);



    }

    public void initNodeInfo(String fileName){
        try {
            HashMap<String ,String > nodeInfo = new HashMap<String , String >();
            String prePath = "D:\\geth\\db1\\keystore\\";
            Credentials credentials = WalletUtils.loadCredentials("abc123", prePath+fileName);
            ECKeyPair ecKeyPair = credentials.getEcKeyPair();
            String privateKey = ecKeyPair.getPrivateKey().toString(16);
            // System.out.println("privateKey = " + privateKey);
            String publicKey=ecKeyPair.getPublicKey().toString(16);
            // System.out.println("publicKey = " + publicKey);
            String address =  credentials.getAddress();
            // System.out.println("address = " + address);
            nodeInfo.put("privateKey", privateKey);
            nodeInfo.put("publicKey", publicKey);
            nodeInfo.put("address", address);
            this.nodeInfo = nodeInfo;
        }catch (Exception e){

        }
    }




    public HashMap<String, String> forwardMessage(HashMap<String, String> message) {


        // check lock message
        if (!this.checkLockMessage(message)) {
            // check failed
            return null;
        }
        this.updateChannel();


        this.lockMessage = message;





        HashMap<String , String > responseMessage = null;
        try {
             responseMessage = this.backwardMessage();

        }catch (Exception e){

        }

        // new sig
        String sig = Signature.sign(lockMessage.get("signMessage"), this.nodeInfo.get("privateKey"));
        responseMessage.put("sig", sig);
        responseMessage.put("sender", this.nodeInfo.get("address"));
        responseMessage.put("signMessage", lockMessage.get("signMessage"));


        return responseMessage;
    }



    public HashMap<String, String> computeNewPairParam(String Y, String X) {
        HashMap<String, String> param = new HashMap<String, String>();
        param.put("Y", "Y");
        param.put("X", "X");
        return param;
    }

    public HashMap<String, String> backwardMessage() throws Exception {
        HashMap<String, String> unlockMessage = new HashMap<String, String>();
        HashMap<String, String> lockMessage = this.lockMessage;
        GtPoint Y = Util.stringToGtPoint(lockMessage.get("Y"));
        // System.out.println("Y = " + Y);

        G1Point X = Util.stringToG1Point(lockMessage.get("X"));

        G1Point R = G1.mul(X, this.secret);
        GtPoint result = Pairing.myPairing(R, this.h);


        assert (Y.equals(result));




        //
        unlockMessage.put("R", Util.g1PointToString(R));
        this.unlockMessage = unlockMessage;

        return unlockMessage;
    }

    public void updateChannel(){
        BigInteger nowABalance = this.channel.partnerA.balance;
        BigInteger nowBBalance = this.channel.partnerB.balance;
        BigInteger newABalance = nowABalance.subtract(new BigInteger("1"));
        BigInteger newBBalance = nowBBalance.add(new BigInteger("1"));
        this.channel.changeState(newABalance, newBBalance);

        return;
    }

    public String channelState() {

        return "Test token";
    }


    public boolean checkLockMessage(HashMap<String, String> messageMap) {
        // check end time
        // if (System.currentTimeMillis() > Long.parseLong(messageMap.get("endTime"))) {
        //     logger.warning("End time illegal");
        //     return false;
        //
        // }

        // check signature
        String senderAddress = messageMap.get("sender");
        if (!Signature.verify(messageMap.get("signMessage"),messageMap.get("sig"),  senderAddress)) {
            logger.warning("Signature check failed");
            return false;
        }


        this.lockMessage = messageMap;
        return true;
    }


    /**
     * @param message
     * @return HashMap string string
     * encAesKey
     * nextIP
     * nextPort
     * nextOnionMessage
     */
    public HashMap<String, String> peelOnionMessage(HashMap<String, String> message) {
        /**
         *   onion  message format
         *   encAesKey///encOnionRoute
         *
         *   enc onion route format
         *   ip:port:m
         */


        String paddedOnionMessage = message.get("onionMessage");
        String encPaddingLen = message.get("encPaddingLen");

        String onionMessage = null;
        int paddingLen = Integer.parseInt(this.ecElGamalEncryption.ECElGDecrypt(encPaddingLen));
        assert (paddingLen >= 0);
        if (paddingLen == 0) {
            onionMessage = paddedOnionMessage;
        } else {
            onionMessage = paddedOnionMessage.substring(0, paddedOnionMessage.length() - paddingLen);
        }


        HashMap<String, String> messageMap = new HashMap<String, String>();


        String[] token = new String[2];
        token = onionMessage.split("///");

        String encAesKey = token[0];

        String encOnionRoute = token[1];


        String aesKey = this.ecElGamalEncryption.ECElGDecrypt(encAesKey);
        // get aes key to decrypt the onion route
        AESEncryption aesEncryption = new AESEncryption(aesKey);

        // decrypt the onion route
        String onionRoute = aesEncryption.decrypt(encOnionRoute);
        String[] onionToken = new String[3];
        onionToken = onionRoute.split(":");
        String nextIP = onionToken[0];
        messageMap.put("nextIP", nextIP);

        String nextPort = onionToken[1];
        messageMap.put("nextPort", nextPort);

        String nextMessage = onionToken[2];

        messageMap.put("nextOnionMessage", nextMessage);


        return messageMap;

    }

    public GtPoint sendToOnion() throws Exception {
        this.secret = Util.getRandom();
        G1Point g1Secret = G1.mul(this.g1, this.secret);
        GtPoint result = Pairing.myPairing(g1Secret, this.h);
        return result;
    }


}
