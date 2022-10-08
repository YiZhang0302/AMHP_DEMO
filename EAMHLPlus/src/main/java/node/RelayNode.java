package node;

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

import java.math.BigInteger;
import java.nio.channels.Channel;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.logging.Logger;

public class RelayNode {
    public ECElGamalEncryption ecElGamalEncryption;
    String localIp;
    int localPort;
    String aesKey;
    public Logger logger;
    String configFile;
    int messageLenMax = 1024;
    HashMap<String, String> lockMessage;
    int myToken;
    int partnerToken;
    String userSk;
    HashMap<String, String> unlockMessage;
    HashMap<String, String> nextLockMessage;
    public HashMap<String, String> nodeInfo;
    G2Point h;
    public ChannelState prevChannel;
    public ChannelState nextChannel;

    public RelayNode(String localIp, int localPort, String elgSk, String elgPk, G2Point h) {
        this.localIp = localIp;
        this.localPort = localPort;
        this.ecElGamalEncryption = new ECElGamalEncryption(elgSk, elgPk);
        this.logger = Logger.getLogger("relayNode_" + localPort);
        this.userSk = "abc";
        this.h = h;

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


        // check lock message
        if (!MyUtils.checkLockMessage(lockMessage, logger)) {
           // check failed
           return null;
        }

        this.updatePreVChannel();
        this.lockMessage = lockMessage;


        // peel the onion message
        HashMap<String, String> onionMessageMap = this.peelOnionMessage(lockMessage);


        HashMap<String, String> nextLockMessage = new HashMap<String, String>();

        this.updateNextVChannel();
        // new state
        String newState = this.nextChannel.toString();
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


        HashMap<String, String> newParam = this.computeForwardPairParam(lockMessage.get("Y"), lockMessage.get("X"));
        nextLockMessage.put("x", newParam.get("x"));
        nextLockMessage.put("X", newParam.get("X"));
        nextLockMessage.put("Y", newParam.get("Y"));


        // padded onion messsage
        HashMap<String, String> paddedMessage = MyUtils.padOnionMessage(onionMessageMap, logger);

        String nextPort = paddedMessage.get("nextPort");
        nextLockMessage.put("nextPort", nextPort);

        nextLockMessage.put("encPaddedLen", paddedMessage.get("encPaddedLen"));

        nextLockMessage.put("nextEncOniRouPadMessage", paddedMessage.get("nextPaddedMessage"));

        this.nextLockMessage = nextLockMessage;
        return nextLockMessage;
    }


    public HashMap<String, String> backwardMessage(HashMap<String, String> unlockMessage) {
        if (!MyUtils.checkUnlockMessage(this.nextLockMessage, unlockMessage, this.h,logger)) {
            return null;
        }
        this.unlockMessage = unlockMessage;

        HashMap<String, String> nextUnlockMessage = new HashMap<String, String>();

        G1Point newRPoint = this.computeBackwardPairParam(unlockMessage.get("R"));

        String newR = Util.g1PointToString(newRPoint);
        String sig = Signature.sign(this.lockMessage.get("signMessage"), this.nodeInfo.get("privateKey"));

        nextUnlockMessage.put("R", newR);
        nextUnlockMessage.put("sig", sig);
        nextUnlockMessage.put("sender", this.nodeInfo.get("address"));

        return nextUnlockMessage;
    }

    public G1Point computeBackwardPairParam(String R) {
        //

        BigInteger x = new BigInteger(this.nextLockMessage.get("x")) ;
        BigInteger xInverse = Util.inverse(x);
        G1Point RPoint = Util.stringToG1Point(R);
        G1Point newR = null;

        // compute newR = R^(1/x)
        try {
             newR = G1.mul(RPoint, xInverse);

        }catch (Exception e){
            logger.info("compute newR = R^(1/x) error!");
        }

        return newR;


    }


    /**
     * compute  random x,  XNew = X^x, YNew = Y^x
     *
     * @param Y
     * @param X
     * @return
     */
    public HashMap<String, String> computeForwardPairParam(String Y, String X){
        try {
            GtPoint yPoint = Util.deserializeGt(Y.getBytes(Charset.forName("ISO-8859-1")));
            G1Point xPoint = Util.deserializeG1(X.getBytes(Charset.forName("ISO-8859-1")));
            BigInteger secret = Util.getRandom();
            GtPoint newY = Gt.gtPow(yPoint, secret);
            // logger.info("newY: "+newY);

            G1Point newX = G1.mul(xPoint, secret);
            HashMap<String, String> param = new HashMap<String, String>();
            param.put("Y", new String(Util.serializeGt(newY),Charset.forName("ISO-8859-1")));
            param.put("X", new String(Util.serializeG1(newX),Charset.forName("ISO-8859-1")));
            param.put("x", secret.toString());
            return param;
        }catch (Exception e){
            logger.warning("Compute forward pair parameter error!");
        }
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
    public HashMap<String, String> peelOnionMessage(HashMap<String, String> message) {
        /**
         *   onion  message format
         *   encAesKey///encOnionRoute
         *
         *   enc onion route format
         *   ip:port:m
         */

        
        String paddedOnionMessage = message.get("nextEncOniRouPadMessage");
        int lenP = paddedOnionMessage.length();
        // System.out.println("lenP = " + lenP);


        HashMap<String, String> messageMap = new HashMap<String, String>();


        String[] token = new String[2];
        token = paddedOnionMessage.split("####");

        String encInfo = token[0];

        String encMessage = token[1];


        // String info = this.rsaEncryption.rsaDecrypt(encRsaM);
        String info = this.ecElGamalEncryption.ECElGDecrypt(encInfo);
        String [] infos = info.split(":::");
        this.aesKey = infos[0];
        String realLen = infos[1];
        realLen=realLen.substring(0,8);
        int len = Integer.parseInt(realLen, 16);
        String realM = encMessage.substring(0, len);

        // get aes key to decrypt the onion route
        AESEncryption aesEncryption = new AESEncryption(aesKey);

        // decrypt the onion route
        String onionRoute = aesEncryption.decrypt(realM);
        String[] onionToken = onionRoute.split(":::");
        // onionToken = onionRoute.split(":");
        String nextIP = onionToken[0];
        messageMap.put("nextIP", nextIP);

        String nextPort = onionToken[1];
        messageMap.put("nextPort", nextPort);


        messageMap.put("nextOnionMessage", onionToken[2]);

        return messageMap;

    }


}
