package client;

import contract.ChannelState;
import contract.Partner;
import encryption.AESEncryption;
import encryption.DiscreteLogarithmTool;
import encryption.RSAEncryption;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.WalletUtils;
import web3j.Signature;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Logger;

public class NoPaddingClientNode {
    String localIp;
    int localPort;
    RSAEncryption rsaEncryption;
    Logger logger;
    String configFile = "src/main/java/config.properties";
    int messageLenMax = 1024;
    HashMap<String, String> lockMessage;
    int myToken;
    int partnerToken;
    BigInteger g;
    BigInteger secret;
    HashMap<String, String> unlockMessage;
    String aesKey;
    public HashMap<String, String> nodeInfo;
    public ChannelState channel;

    public NoPaddingClientNode() {

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
        this.rsaEncryption = new RSAEncryption(prop.getProperty("CLIENT_RSA_SECRET_KEY"), prop.getProperty("CLIENT_RSA_PUBLIC_KEY"));

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

    /**
     * @param message
     */
    public HashMap<String, String> forwardMessage(HashMap<String, String> message) {


        // check lock message
        if (!this.checkLockMessage(message)) {
            // check failed
            return null;
        }

        this.updateChannel();

        this.lockMessage = message;
        HashMap <String ,String > lockMessageMap = this.peelMessage(message);
        this.lockMessage = lockMessageMap;
        BigInteger Y = new BigInteger(lockMessageMap.get("Y"));
        BigInteger k = new BigInteger(lockMessageMap.get("k"));
        BigInteger newY = DiscreteLogarithmTool.modPow(k);
        assert (newY.equals(Y));

        HashMap<String, String> responseMessage = new HashMap<String, String>();




        String sig = Signature.sign(lockMessage.get("signMessage"), this.nodeInfo.get("privateKey"));
        responseMessage.put("sig", sig);
        responseMessage.put("sender", this.nodeInfo.get("address"));
        responseMessage.put("signMessage", lockMessage.get("signMessage"));
        responseMessage.put("sig", sig);
        responseMessage.put("k", k.toString());

        return responseMessage;
    }


    public void updateChannel(){
        BigInteger nowABalance = this.channel.partnerA.balance;
        BigInteger nowBBalance = this.channel.partnerB.balance;
        BigInteger newABalance = nowABalance.subtract(new BigInteger("1"));
        BigInteger newBBalance = nowBBalance.add(new BigInteger("1"));
        this.channel.changeState(newABalance, newBBalance);

        return;
    }

    public HashMap<String, String> peelMessage(HashMap<String, String> messageMap){
        this.aesKey=this.getAesKey(lockMessage);
        HashMap<String, String > DLPOnionMessageMap = this.peelDLPRouteMessage(lockMessage);
        HashMap<String, String > lockMessageMap = new HashMap<String, String>();
        messageMap.put("Y", DLPOnionMessageMap.get("Y"));
        messageMap.put("k", DLPOnionMessageMap.get("k"));
        messageMap.put("aesKey", aesKey);
        return messageMap;
    }

    public String getAesKey(HashMap<String, String> message) {
        /**
         *   onion  message format
         *   encAesKey///encOnionRoute
         *
         *   enc onion route format
         *   ip:port:m
         */


        String paddedOnionMessage = message.get("nextEncOniRouPadMessage");
        String encPaddingLen = message.get("nextEncOniRouPadLen");

        String encAesKey = null;
        int paddingLen = Integer.parseInt(this.rsaEncryption.rsaDecrypt(encPaddingLen));
        assert (paddingLen >= 0);
        if (paddingLen == 0) {
            encAesKey = paddedOnionMessage;
        } else {
            encAesKey = paddedOnionMessage.substring(0, paddedOnionMessage.length() - paddingLen);
        }


        HashMap<String, String> messageMap = new HashMap<String, String>();




        return this.rsaEncryption.rsaDecrypt(encAesKey);




    }

    public HashMap<String, String> peelDLPRouteMessage(HashMap<String, String> message) {
        String paddedOnionMessage = message.get("nextEncDLPOniPadMessage");
        String encPaddingLen = message.get("nextEncDLPPadLen");

        String encDLPOniMessage = null;
        int paddingLen = Integer.parseInt(this.rsaEncryption.rsaDecrypt(encPaddingLen));
        assert (paddingLen >= 0);
        if (paddingLen == 0) {
            encDLPOniMessage = paddedOnionMessage;
        } else {
            encDLPOniMessage = paddedOnionMessage.substring(0, paddedOnionMessage.length() - paddingLen);
        }


        HashMap<String, String> messageMap = new HashMap<String, String>();

        AESEncryption aesEncryption = new AESEncryption(this.aesKey);

        // decrypt the onion route
        String DLPOniMessage = aesEncryption.decrypt(encDLPOniMessage);



        String[] onionToken = new String[4];
        onionToken = DLPOniMessage.split(":");

        String Y = onionToken[0];
        messageMap.put("Y", Y);


        String nextY = onionToken[1];
        messageMap.put("k", nextY);


        return messageMap;
    }


    public HashMap<String, String> computeNewPairParam(String Y, String X) {
        HashMap<String, String> param = new HashMap<String, String>();
        param.put("Y", "Y");
        param.put("X", "X");
        return param;
    }

    public HashMap<String, String> backwardMessage() throws Exception {
        //HashMap<String, String> unlockMessage = new HashMap<String, String>();
        //HashMap<String, String> lockMessage = this.lockMessage;
        //GtPoint Y = Util.stringToGtPoint(lockMessage.get("Y"));
        //System.out.println("Y = " + Y);
        //
        //G1Point X = Util.stringToG1Point(lockMessage.get("X"));
        //
        //G1Point R = G1.mul(X, this.secret);
        //GtPoint result = Pairing.myPairing(R, this.h);
        //
        //
        //assert (Y.equals(result));
        //String sig = Signature.signMessage(this.lockMessage.get("signData"), "this.userSk");
        //
        //
        //
        //
        ////
        //unlockMessage.put("R", Util.g1PointToString(R));
        //unlockMessage.put("sig", sig);
        //this.unlockMessage = unlockMessage;
        //
        //return unlockMessage;
        return null;
    }


    public String channelState() {

        return "Test token";
    }


    public boolean checkLockMessage(HashMap<String, String> messageMap) {
        // check end time
        if (System.currentTimeMillis() > Long.parseLong(messageMap.get("endTime"))) {
            logger.warning("End time illegal");
            return false;

        }

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
        int paddingLen = Integer.parseInt(this.rsaEncryption.rsaDecrypt(encPaddingLen));
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


        String aesKey = this.rsaEncryption.rsaDecrypt(encAesKey);
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




}
