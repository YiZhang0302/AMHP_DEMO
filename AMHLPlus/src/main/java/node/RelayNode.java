package node;

import contract.ChannelState;
import contract.Partner;
import encryption.*;
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

public class RelayNode {
    String localIp;
    int localPort;
    RSAEncryption rsaEncryption;
    ECElGamalEncryption ecElGamalEncryption;
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


    public RelayNode(String localIp, int localPort, String elgSk, String elgPk, BigInteger g) {
        this.localIp = localIp;
        this.localPort = localPort;
        // this.rsaEncryption = new RSAEncryption(rsaSk, rsaPk);
        this.ecElGamalEncryption = new ECElGamalEncryption(elgSk, elgPk);
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

        // now
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


        nextLockMessage.put("nextEncOniRouMessage", onionMessageMap.get("nextEncOniRouMessage"));
        nextLockMessage.put("nextPort", onionMessageMap.get("nextPort"));
        nextLockMessage.put("nextIP", onionMessageMap.get("nextIP"));




      nextLockMessage = this.padMessage(nextLockMessage);
        this.nextLockMessage = nextLockMessage;
        return nextLockMessage;
    }

    public HashMap<String, String> padMessage(HashMap<String, String> messageMap) {
        // pad DLP onion message
        // messageMap = this.padDLPOnionMessage(messageMap);

        // pad chainOnionMessage
        messageMap = this.padOnionRouteMessage(messageMap);

        return messageMap;
    }



    public HashMap<String, String> peelMessage(HashMap<String, String> messageMap){
        HashMap<String, String> onionRouteMessageMap = this.peelOnionRouteMessage(lockMessage);
        messageMap.put("nextPort", onionRouteMessageMap.get("nextPort"));
        messageMap.put("nextIP", onionRouteMessageMap.get("nextIP"));
        messageMap.put("nextEncOniRouMessage", onionRouteMessageMap.get("nextEncOniRouMessage"));
        messageMap.put("Y", onionRouteMessageMap.get("Y"));
        messageMap.put("nextY", onionRouteMessageMap.get("nextY"));
        messageMap.put("next_y", onionRouteMessageMap.get("next_y"));
        return messageMap;
    }


    public HashMap<String, String> padOnionRouteMessage(HashMap<String, String> messageMap){
        int paddedLen;


        int maxMessageLen = Integer.parseInt(prop.getProperty("MAX_MESSAGE_LEN"));



        if (messageMap.get("nextEncOniRouMessage").length() > maxMessageLen) {
            paddedLen = 0;

        } else {
            paddedLen = maxMessageLen - messageMap.get("nextEncOniRouMessage").length();
        }


        String randomLetter = RandomLetter.randomLetter(paddedLen);
        String nextPaddedMessage = messageMap.get("nextEncOniRouMessage") + randomLetter;
        messageMap.put("nextEncOniRouPadMessage", nextPaddedMessage);

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
        // System.out.println("paddedOnionMessage.length() = " + paddedOnionMessage.length());


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
        
        // System.out.println("len = " + len);
        // int lenM = encMessage.length();
        // System.out.println("lenM = " + lenM);
        String realM = encMessage.substring(0, len);

        // get aes key to decrypt the onion route
        AESEncryption aesEncryption = new AESEncryption(aesKey);

        // decrypt the onion route
        String onionRoute = aesEncryption.decrypt(realM);
        String[] onionToken = onionRoute.split(":::");
        String nextIP = onionToken[0];
        messageMap.put("nextIP", nextIP);

        String nextPort = onionToken[1];
        messageMap.put("nextPort", nextPort);

        messageMap.put("Y", onionToken[2]);
        messageMap.put("nextY", onionToken[3]);
        messageMap.put("next_y", onionToken[4]);
        messageMap.put("nextEncOniRouMessage", onionToken[5]);



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
