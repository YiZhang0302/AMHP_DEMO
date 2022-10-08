package node;

import contract.ChannelState;
import contract.Partner;
import encryption.AESEncryption;
import encryption.DiscreteLogarithmTool;
import encryption.RSAEncryption;
import encryption.RandomLetter;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.WalletUtils;
import tools.MyUtils;
import web3j.Signature;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Logger;

public class NoPaddingRelayNode {
    String localIp;
    int localPort;
    RSAEncryption rsaEncryption;
    public Logger logger;
    String configFile;
    int messageLenMax = 1024;
    HashMap<String, String> lockMessage;
    int myToken;
    int partnerToken;
    String aesKey;
    String userSk;
    HashMap<String, String> unlockMessage;
    HashMap<String, String> nextLockMessage;
    BigInteger g;
    Properties prop = new Properties();
    public HashMap<String, String> nodeInfo;
    public ChannelState prevChannel;
    public ChannelState nextChannel;


    public NoPaddingRelayNode(String localIp, int localPort, String rsaSk, String rsaPk, BigInteger g) {
        this.localIp = localIp;
        this.localPort = localPort;
        this.rsaEncryption = new RSAEncryption(rsaSk, rsaPk);
        this.logger = Logger.getLogger("relayNode_" + localPort);
        this.userSk = "abc";
        this.g = g;
        try {
            DataInputStream din = new DataInputStream(new FileInputStream("src/main/java/config.properties"));
            this.prop.load(din);
        }catch (IOException e){

        }
    }


    public void initNodeChannel(String preAddress, String nextAddress){
        assert(this.nodeInfo != null);

        Partner prevPartnerA = new Partner(preAddress, new BigInteger("100"));
        Partner prevPartnerB = new Partner(nodeInfo.get("address"), new BigInteger("100"));
        this.prevChannel = new ChannelState(prevPartnerA, prevPartnerB);

        Partner nextPartnerA = new Partner(nodeInfo.get("address"), new BigInteger("100"));
        Partner nextPartnerB = new Partner(nextAddress, new BigInteger("100"));
        this.nextChannel = new ChannelState(nextPartnerA, nextPartnerB);

    }

    public void loadKeyFile(String fileName){
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

    public void updatePreVChannel(){
        BigInteger nowABalance = this.prevChannel.partnerA.balance;
        BigInteger nowBBalance = this.prevChannel.partnerB.balance;
        BigInteger newABalance = nowABalance.subtract(new BigInteger("1"));
        BigInteger newBBalance = nowBBalance.add(new BigInteger("1"));
        this.prevChannel.changeState(newABalance, newBBalance);
    }

    public void updateNextVChannel(){
        BigInteger nowABalance = this.nextChannel.partnerA.balance;
        BigInteger nowBBalance = this.nextChannel.partnerB.balance;
        BigInteger newABalance = nowABalance.subtract(new BigInteger("1"));
        BigInteger newBBalance = nowBBalance.add(new BigInteger("1"));
        this.nextChannel.changeState(newABalance, newBBalance);
    }

    /**
     * @param lockMessage
     */
    public HashMap<String, String> forwardMessage(HashMap<String, String> lockMessage) {

        this.lockMessage = lockMessage;


        // peel the onion message
        //HashMap<String, String> onionMessageMap = this.peelOnionRouteMessage(lockMessage);
        HashMap<String, String> onionMessageMap = this.peelMessage(lockMessage);
        this.lockMessage = onionMessageMap;
        // check lock message
        if (!MyUtils.checkLockMessage(lockMessage, logger)) {
            // check failed
            return null;
        }

        this.updatePreVChannel();
        this.lockMessage = lockMessage;

        HashMap<String, String> nextLockMessage = new HashMap<String, String>();


        this.updateNextVChannel();
        // new state
        String newState = this.channelState();
        nextLockMessage.put("state", newState);

        String signData = newState;

        // new sig
        // signData
        String sig = Signature.sign(signData, this.nodeInfo.get("privateKey"));
        nextLockMessage.put("sig", sig);
        nextLockMessage.put("signMessage", signData);


        // new end time shorten one hour
        String newEndTime = String.valueOf(Long.parseLong(lockMessage.get("endTime")) - 1 * 60 * 60 * 1000);
        nextLockMessage.put("endTime", newEndTime);
        nextLockMessage.put("sender", this.nodeInfo.get("address"));


        nextLockMessage.put("nextEncDLPOniMessage", onionMessageMap.get("nextEncDLPOniMessage"));
        nextLockMessage.put("nextEncOniRouMessage", onionMessageMap.get("nextEncOniRouMessage"));
        nextLockMessage.put("nextPort", onionMessageMap.get("nextPort"));
        nextLockMessage.put("nextIP", onionMessageMap.get("nextIP"));




      nextLockMessage = this.padMessage(nextLockMessage);
        this.nextLockMessage = nextLockMessage;
        return nextLockMessage;
    }

    public HashMap<String, String> padMessage(HashMap<String, String> messageMap) {
        // pad DLP onion message
        messageMap = this.padDLPOnionMessage(messageMap);

        // pad chainOnionMessage
        messageMap = this.padOnionRouteMessage(messageMap);

        return messageMap;
    }



    public HashMap<String, String> peelMessage(HashMap<String, String> messageMap){
        HashMap<String, String> onionRouteMessageMap = this.peelOnionRouteMessage(lockMessage);
        HashMap<String, String > DLPOnionMessageMap = this.peelDLPRouteMessage(lockMessage);
        messageMap.put("nextPort", onionRouteMessageMap.get("nextPort"));
        messageMap.put("nextIP", onionRouteMessageMap.get("nextIP"));
        messageMap.put("nextEncOniRouMessage", onionRouteMessageMap.get("nextEncOniRouMessage"));
        messageMap.put("nextEncDLPOniMessage", DLPOnionMessageMap.get("nextEncDLPOniMessage"));
        messageMap.put("Y", DLPOnionMessageMap.get("Y"));
        messageMap.put("nextY", DLPOnionMessageMap.get("nextY"));
        messageMap.put("next_y", DLPOnionMessageMap.get("next_y"));
        return messageMap;
    }

    public HashMap<String, String> padDLPOnionMessage(HashMap<String, String> messageMap){
        String nextPort = this.lockMessage.get("nextPort");
        int paddedLen;


        int maxMessageLen = Integer.parseInt(this.prop.getProperty("MAX_DLP_MESSAGE_LEN"));

        String nextRsaPk = this.prop.getProperty("NODE_" + nextPort + "_RSA_PUBLIC_KEY");


        if (messageMap.get("nextEncDLPOniMessage").length() > maxMessageLen) {
            paddedLen = 0;

        } else {
            paddedLen = maxMessageLen - messageMap.get("nextEncDLPOniMessage").length();
        }

        String len = String.valueOf(paddedLen);
        int padded = 8 - len.length();
        for (int i = 0; i < padded; i++) {
            len = '0' + len;
        }

        String encPaddedLen = RSAEncryption.rsaEncrypt(len, RSAEncryption.keyFromString(nextRsaPk));

        String randomLetter = RandomLetter.randomLetter(paddedLen);
        String nextPaddedMessage = messageMap.get("nextEncDLPOniMessage") + randomLetter;
        messageMap.put("nextEncDLPOniPadMessage", nextPaddedMessage);
        messageMap.put("DLPPadLen", len);
        messageMap.put("nextEncDLPPadLen", encPaddedLen);
        // System.out.println("nextPaddedMessage = " + nextPaddedMessage);

        return messageMap;
    }

    public HashMap<String, String> padOnionRouteMessage(HashMap<String, String> messageMap){
        String nextPort = this.lockMessage.get("nextPort");
        //String nextPort = nextOnionMessageMap.get("nextPort");
        int paddedLen;


        int maxMessageLen = Integer.parseInt(this.prop.getProperty("MAX_ROUTE_MESSAGE_LEN"));

        String nextRsaPk = this.prop.getProperty("NODE_" + nextPort + "_RSA_PUBLIC_KEY");


        if (messageMap.get("nextEncOniRouMessage").length() > maxMessageLen) {
            paddedLen = 0;

        } else {
            paddedLen = maxMessageLen - messageMap.get("nextEncOniRouMessage").length();
        }
        String encPaddedLen = RSAEncryption.rsaEncrypt(String.valueOf(paddedLen), RSAEncryption.keyFromString(nextRsaPk));

        String randomLetter = RandomLetter.randomLetter(paddedLen);
        String nextPaddedMessage = messageMap.get("nextEncOniRouMessage") + randomLetter;
        messageMap.put("nextEncOniRouPadMessage", nextPaddedMessage);
        messageMap.put("ORPaddedLen", String.valueOf(paddedLen));
        messageMap.put("nextEncOniRouPadLen", encPaddedLen);


        return messageMap;

    }




    public HashMap<String, String> backwardMessage(HashMap<String, String> unlockMessage) {
        if (!MyUtils.checkUnlockMessage(this.nextLockMessage, unlockMessage,logger)) {
            return null;
        }
        this.unlockMessage = unlockMessage;

        HashMap<String, String> nextUnlockMessage = new HashMap<String, String>();


        // new state
        String newState = this.channelState();
        nextUnlockMessage.put("state", newState);

        String sig = Signature.sign(this.lockMessage.get("signMessage"), this.nodeInfo.get("privateKey"));
        // new sig
        nextUnlockMessage.put("sig", sig);
        nextUnlockMessage.put("sender", this.nodeInfo.get("address"));

        BigInteger next_y = new BigInteger(this.lockMessage.get("next_y"));
        BigInteger k = new BigInteger(unlockMessage.get("k"));

        BigInteger new_k = DiscreteLogarithmTool.modSub(k, next_y);
        nextUnlockMessage.put("k", new_k.toString());





        return nextUnlockMessage;

        //
        //
        //return nextUnlockMessage;

    }

    public void computeBackwardPairParam(String R) {
        //

        //BigInteger x = new BigInteger(this.nextLockMessage.get("x")) ;
        //BigInteger xInverse = Util.inverse(x);
        //G1Point RPoint = Util.stringToG1Point(R);
        //G1Point newR = null;
        //
        //// compute newR = R^(1/x)
        //try {
        //     newR = G1.mul(RPoint, xInverse);
        //
        //}catch (Exception e){
        //    logger.info("compute newR = R^(1/x) error!");
        //}

        return ;


    }


    /**
     * compute  random x,  XNew = X^x, YNew = Y^x
     *
     * @param Y
     * @param X
     * @return
     */
    public HashMap<String, String> computeForwardPairParam(String Y, String X){
        //try {
        //    GtPoint yPoint = Util.deserializeGt(Y.getBytes(Charset.forName("ISO-8859-1")));
        //    G1Point xPoint = Util.deserializeG1(X.getBytes(Charset.forName("ISO-8859-1")));
        //    BigInteger secret = Util.getRandom();
        //    GtPoint newY = Gt.gtPow(yPoint, secret);
        //    logger.info("newY: "+newY);
        //
        //    G1Point newX = G1.mul(xPoint, secret);
        //    HashMap<String, String> param = new HashMap<String, String>();
        //    param.put("Y", new String(Util.serializeGt(newY),Charset.forName("ISO-8859-1")));
        //    param.put("X", new String(Util.serializeG1(newX),Charset.forName("ISO-8859-1")));
        //    param.put("x", secret.toString());
        //    return param;
        //}catch (Exception e){
        //    logger.warning("Compute forward pair parameter error!");
        //}
        return null;
    }

    public String channelState() {

        return "Test token";
    }


    /**
     * @param message
     * @return HashMap string string
     * encAesKey
     * nextIP
     * nextPort
     * nextOnionMessage
     */
    public HashMap<String, String> peelOnionRouteMessage(HashMap<String, String> message) {
        /**
         *   client  message format
         *   encAesKey///encOnionRoute
         *
         *   enc onion route format
         *   ip:port:m
         */


        String paddedOnionMessage = message.get("nextEncOniRouPadMessage");
        String encPaddingLen = message.get("nextEncOniRouPadLen");

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


        this.aesKey = this.rsaEncryption.rsaDecrypt(encAesKey);
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

        messageMap.put("nextEncOniRouMessage", nextMessage);


        return messageMap;

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

        AESEncryption aesEncryption = new AESEncryption(aesKey);

        // decrypt the onion route
        String DLPOniMessage = aesEncryption.decrypt(encDLPOniMessage);



        String[] onionToken = new String[4];
        onionToken = DLPOniMessage.split(":");

        String Y = onionToken[0];
        messageMap.put("Y", Y);


        String nextY = onionToken[1];
        messageMap.put("nextY", nextY);

        String y = onionToken[2];
        messageMap.put("next_y", y);

        String nextMessage = onionToken[3];

        messageMap.put("nextEncDLPOniMessage", nextMessage);


        return messageMap;
    }

    }
