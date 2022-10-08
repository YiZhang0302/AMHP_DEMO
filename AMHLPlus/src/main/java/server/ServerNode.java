package server;

import contract.ChannelState;
import contract.Partner;
import encryption.*;
import jnr.ffi.annotations.In;
import org.bouncycastle.util.encoders.Hex;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.WalletUtils;
import tools.MyUtils;
import web3j.Signature;

import java.io.DataInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Logger;

public class ServerNode {
    String localIP;
    int localPort;
    String clientIP;
    int clientPort;

    Logger logger;
    String[] chainRelayNodeElGPk;
    String[] chainRelayNodeAesKey;
    String[] chainRelayNodePort;
    String[] chainRelayNodeIp;
    int chainRelayNodeCounts;
    int chainRelayNodePortStrat;
    BigInteger g;
    String address;
    String configFile = "src/main/java/config.properties";
    Properties prop = null;
    String[] lockMessagesY;
    String[] unlockMessagesK ;
    String[] random_y ;
    String[] onionMessageLen;
    BigInteger[] YPows;
    public HashMap<String, String > nodeInfo;
    public ChannelState channel;

    public void initNodeChannel(String nextAddress){
        assert(this.nodeInfo != null);

        Partner PartnerA = new Partner(nodeInfo.get("address"), new BigInteger("100"));
        Partner PartnerB = new Partner(nextAddress, new BigInteger("100"));

        this.channel = new ChannelState(PartnerA, PartnerB);



    }

    public ServerNode() {
        logger = Logger.getLogger("serverNode");

        try {
            prop = new Properties();
            DataInputStream din = new DataInputStream(new FileInputStream(configFile));
            prop.load(din);
        } catch (IOException e) {
            logger.info("Can not load the config properties");
        }

        localIP = prop.getProperty("SERVER_IP");
        localPort = Integer.parseInt(prop.getProperty("SERVER_PORT"));

        clientIP = prop.getProperty("CLIENT_IP");
        clientPort = Integer.parseInt(prop.getProperty("CLIENT_PORT"));
        chainRelayNodePortStrat = Integer.parseInt(prop.getProperty("RELAY_NODE_PORT_START"));
        this.g = new BigInteger(prop.getProperty("PARAM_G"),16);
        chainRelayNodeCounts = 0;
    }

    public void initNodeInfo(String fileName){
        try {
            HashMap<String ,String > nodeInfo = new HashMap<String , String >();
            String prePath = "D:\\geth\\db1\\keystore\\";
            Credentials credentials = WalletUtils.loadCredentials("abc123", prePath+fileName);
            ECKeyPair ecKeyPair = credentials.getEcKeyPair();
            String privateKey = ecKeyPair.getPrivateKey().toString(16);
            String publicKey=ecKeyPair.getPublicKey().toString(16);
            String address =  credentials.getAddress();
            nodeInfo.put("privateKey", privateKey);
            nodeInfo.put("publicKey", publicKey);
            nodeInfo.put("address", address);
            this.nodeInfo = nodeInfo;
        }catch (Exception e){

        }
    }

    public HashMap<String, String> sendToRelayNodes(String onionMessage) {
        HashMap<String, String> sendMessage = new HashMap<String, String>();

        String newState = this.updateChannel();
        sendMessage.put("state", newState);
        long endTime = System.currentTimeMillis()+12*3600*1000;
        sendMessage.put("endTime", String.valueOf(endTime) );


        String sig = Signature.sign(newState, this.nodeInfo.get("privateKey"));
        sendMessage.put("signMessage", newState);
        sendMessage.put("sig", sig);

        sendMessage.put("onionRouteMessage", onionMessage);
        sendMessage.put("sender", this.nodeInfo.get("address"));

        // messageMap = this.padOnionRouteMessage(messageMap);

        HashMap<String, String> paddedSendMessage = this.padOnionRouteMessage(sendMessage);
        return paddedSendMessage;

    }

    public String updateChannel(){
        BigInteger nowABalance = this.channel.partnerA.balance;
        BigInteger nowBBalance = this.channel.partnerB.balance;
        BigInteger newABalance = nowABalance.subtract(new BigInteger("1"));
        BigInteger newBBalance = nowBBalance.add(new BigInteger("1"));
        this.channel.changeState(newABalance, newBBalance);

        return this.channel.toString();
    }

    public HashMap<String, String> padMessage(HashMap<String, String> messageMap) {
        // pad DLP onion message
        //  messageMap = this.padDLPOnionMessage(messageMap);

         // pad chainOnionMessage
         messageMap = this.padOnionRouteMessage(messageMap);

        return messageMap;
    }


    public HashMap<String, String> padDLPOnionMessage(HashMap<String, String> messageMap){
        String nextPort = chainRelayNodePort[0];
        //String nextPort = nextOnionMessageMap.get("nextPort");
        int paddedLen;


        int maxMessageLen = Integer.parseInt(prop.getProperty("MAX_DLP_MESSAGE_LEN"));

        String nextRsaPk = prop.getProperty("NODE_" + nextPort + "_RSA_PUBLIC_KEY");


        if (messageMap.get("DLPOnionMessage").length() > maxMessageLen) {
            paddedLen = 0;

        } else {
            paddedLen = maxMessageLen - messageMap.get("DLPOnionMessage").length();
        }

        String len = String.valueOf(paddedLen);
        int padded = 8 - len.length();
        for (int i = 0; i < padded; i++) {
            len = '0' + len;
        }

        String encPaddedLen = RSAEncryption.rsaEncrypt(String.valueOf(paddedLen), RSAEncryption.keyFromString(nextRsaPk));

        String randomLetter = RandomLetter.randomLetter(paddedLen);
        String nextPaddedMessage = messageMap.get("DLPOnionMessage") + randomLetter;
        messageMap.put("nextEncDLPOniPadMessage", nextPaddedMessage);
        messageMap.put("DLPPadLen", String.valueOf(paddedLen));
        messageMap.put("nextEncDLPPadLen", encPaddedLen);
        // System.out.println("nextPaddedMessage = " + nextPaddedMessage);

        return messageMap;
    }

    public HashMap<String, String> padOnionRouteMessage(HashMap<String, String> messageMap){
        int paddedLen;


        int maxMessageLen = Integer.parseInt(prop.getProperty("MAX_MESSAGE_LEN"));
        // int lenP = messageMap.get("onionRouteMessage").length();
        // System.out.println("lenP = " + lenP);

        if (messageMap.get("onionRouteMessage").length() > maxMessageLen) {
            paddedLen = 0;

        } else {
            paddedLen = maxMessageLen - messageMap.get("onionRouteMessage").length();
        }

        // System.out.println("paddedLen = " + paddedLen);
        String randomLetter = RandomLetter.randomLetter(paddedLen);
        String nextPaddedMessage = messageMap.get("onionRouteMessage") + randomLetter;
        messageMap.put("nextEncOniRouPadMessage", nextPaddedMessage);


        return messageMap;

    }



    public String  constructChainRoute(int chainRelayNodeCounts) {

        // construct the off chain relay route
        int nodeStartPort = Integer.parseInt(prop.getProperty("RELAY_NODE_PORT_START"));
        for (int i = 0; i < chainRelayNodeCounts; i++) {
            int nodePort = nodeStartPort + (i * 10);
            String nodeElGName = "NODE_" + nodePort + "_ElG_PUBLIC_KEY";
            String nodeAesName = "NODE_" + nodePort + "_AES_KEY";
            chainRelayNodeElGPk[i] = prop.getProperty(nodeElGName);
            chainRelayNodeAesKey[i] = prop.getProperty(nodeAesName);
            chainRelayNodeIp[i] = "127.0.0.1";
            chainRelayNodePort[i] = "" + nodePort;
        }


        String clientElGPkName = "NODE_" + clientPort + "_ElG_PUBLIC_KEY";
        String clientAesName = "NODE_" + clientPort + "_AES_KEY";
        String clientElGPk = prop.getProperty(clientElGPkName);
        String clientAes = prop.getProperty(clientAesName);
        chainRelayNodeElGPk[chainRelayNodeCounts] = clientElGPk;
        chainRelayNodeAesKey[chainRelayNodeCounts] = clientAes;
        chainRelayNodeIp[chainRelayNodeCounts] = "127.0.0.1";
        chainRelayNodePort[chainRelayNodeCounts] = "" + clientPort;



        String newMessage = clientCell(this.chainRelayNodeCounts);
        //
        // // 创建发送给client的消息
        // String newMessage = makeChainCell(this.chainRelayNodeCounts-1);



        for (int i = chainRelayNodeCounts - 1; i >= 0; i--) {
            newMessage = makeChainCell(newMessage, i);
        }


        return newMessage;
    }

    public void  constructChainOnionMessage(int relayNodeCounts) {
        this.chainRelayNodeCounts = relayNodeCounts;
        chainRelayNodeElGPk = new String[chainRelayNodeCounts+1];
        chainRelayNodeAesKey = new String[chainRelayNodeCounts+1];
        chainRelayNodeIp = new String[chainRelayNodeCounts+1];
        chainRelayNodePort = new String[chainRelayNodeCounts+1];
        this.YPows = new BigInteger[chainRelayNodeCounts+1];
        this.random_y = new String[chainRelayNodeCounts+1];
        this.lockMessagesY = new String[chainRelayNodeCounts+1];
        this.unlockMessagesK = new String[chainRelayNodeCounts+1];
        // chainRelayNodeCounts must bigger than 0
        assert (this.chainRelayNodeCounts > 0) ;


        BigInteger sum_y = new BigInteger("0");
        BigInteger YPow = new BigInteger("0");
        // construct the off chain relay route
        int nodeStartPort = Integer.parseInt(prop.getProperty("RELAY_NODE_PORT_START"));

        for (int i = 0; i <= chainRelayNodeCounts; i++) {
            int nodePort = nodeStartPort + (i * 10);
            BigInteger y =  DiscreteLogarithmTool.getRandomNum();
            while (y.compareTo(new BigInteger("0"))<=0){
                y =  DiscreteLogarithmTool.getRandomNum();
            }
            if(i == 0){
                sum_y = y;
            }else {
                sum_y = DiscreteLogarithmTool.modAdd(sum_y, y);
            }
            this.random_y[i] = y.toString();
            // System.out.println("random_y = " + y);
            this.unlockMessagesK[i] = sum_y.toString();
            this.YPows[i] = DiscreteLogarithmTool.modPow(sum_y);
        }


    }



    private String makeDLPMessageCell(String message, int index){
        /// Y_i:Y_(i+1):y///messsage

        BigInteger Y = this.YPows[index];
        BigInteger nextY = this.YPows[index+1];
        String nextMessage = Y.toString()+":"+nextY.toString()+":"+ this.random_y[index+1] +":"+message;
        String encNextMessage = AESEncryption.encrypt(nextMessage,  this.chainRelayNodeAesKey[index]);

        return encNextMessage;
    }

    private String makeDLPMessageCell(int index){
        // client message
        BigInteger k =  new BigInteger(this.unlockMessagesK[index]) ;
        BigInteger Y = this.YPows[index];
        String message = Y.toString()+":"+ k.toString()+":"+"null"+":"+"null";
        String encMessage = AESEncryption.encrypt(message,  this.chainRelayNodeAesKey[index]);
        // System.out.println("encMessage = " + encMessage);
        return encMessage;

    }


    private String makeMessageCellLen(BigInteger encMessageLen, int index){
        // encMessageLen < 16^5
        assert ((encMessageLen.compareTo(new BigInteger("16").pow(5) ))< 0);
        String hexLen = encMessageLen.toString(16);
        String paddedLen = String.format("%05x", encMessageLen);
        String encPaddedLen = AESEncryption.encrypt(paddedLen,  this.chainRelayNodeAesKey[index]);

        return encPaddedLen;
    }


    private String clientCell(int index) {
        //  Cell:  rsaEnc(aesKey)///len:aesEnc(ip:port:message)

        String ip;
        int port;

            ip = clientIP;
            port = clientPort;

        String aesKey = chainRelayNodeAesKey[index];

        String ElGkey = chainRelayNodeElGPk[index];

        BigInteger k =  new BigInteger(this.unlockMessagesK[index]) ;
        BigInteger Y = this.YPows[index];
        String message = Y.toString()+":::"+ k.toString()+":::"+"null";
        String sendMessage = ip + ":::" + port + ":::" + message;

        String encMessage = AESEncryption.encrypt(sendMessage, AESEncryption.formatAesKey(aesKey));




        String encMessageLen = Integer.toString(encMessage.length(),16)  ;
        assert(encMessageLen.length() <= 8);

        int padLen = 8 - encMessageLen.length();
        for (int i = 0; i < padLen; i++) {
            encMessageLen = '0' + encMessageLen;
        }
        // System.out.println("encMessageLen = " + encMessageLen);
        String elgMsg = aesKey+":::" +encMessageLen;
        // System.out.println("index = " + index);
        // System.out.println("elgMsg = " + elgMsg);
        String encElgMsg = ECElGamalEncryption.ECElGEncrypt(elgMsg, ECElGamalEncryption.keyFromString(ElGkey));
        // System.out.println("encMessage = " + encMessage);

        String onion = "";
        onion = onion + encElgMsg + "####" + encMessage;
        return onion;
    }

    private String makeChainCell(String message, int index) {
        //  Cell:  rsaEnc(aesKey)///aesEnc(ip:port:message)
        String ip;
        int port;
        if(index+1 == this.chainRelayNodeCounts){
            ip = this.clientIP;
            port = this.clientPort;
        }else {
            ip = chainRelayNodeIp[index + 1];
            port = Integer.parseInt(chainRelayNodePort[index + 1]);
        }

        String aesKey = chainRelayNodeAesKey[index];

        BigInteger k =  new BigInteger(this.random_y[index+1]);
        BigInteger Y = this.YPows[index];
        BigInteger nextY = this.YPows[index+1];

        String newMessage = Y.toString()+":::"+nextY.toString()+":::"+ k.toString();
        String sendMessage = ip + ":::" + port + ":::" + newMessage+":::"+message;

        String encMessage = AESEncryption.encrypt(sendMessage, AESEncryption.formatAesKey(aesKey));
        // String message_ =  AESEncryption.decrypt(encMessage, AESEncryption.formatAesKey(aesKey));



        String encMessageLen = Integer.toString(encMessage.length(),16) ;
        // System.out.println("encMessageLen = " + encMessage.length());
        assert(encMessageLen.length() < 8);

        int padLen = 8 - encMessageLen.length();
        for (int i = 0; i < padLen; i++) {
            encMessageLen = '0' + encMessageLen;
        }
        String ElGkey = chainRelayNodeElGPk[index];
        String elgMsg = aesKey + ":::" + encMessageLen;
        // System.out.println("index = " + index);
        // System.out.println("elgMsg = " + elgMsg);
        String encElgMsg = ECElGamalEncryption.ECElGEncrypt(elgMsg, ECElGamalEncryption.keyFromString(ElGkey));
        // System.out.println("encMessage = " + encMessage);


        String onion = "";
        onion = onion + encElgMsg + "####" + encMessage;
        return onion;
    }

    public void waitResponse() {

        try {
            ServerSocket serverSocket = new ServerSocket(localPort + 4, 5);
            Socket incoming = serverSocket.accept();
            byte[] message = MyUtils.receiveMessage(incoming);
            logger.info("received message = " + new String(message));
        } catch (IOException e) {

        }

    }

    public void start() {


        String chainRoute = this.constructChainRoute(3);

        String nextMessage = "Hello, I'm from server node.";
        String sendMessage = nextMessage + "///" + new String(chainRoute);

        try {
            Socket socket = new Socket(localIP, chainRelayNodePortStrat, InetAddress.getByName(localIP), localPort);
            MyUtils.sendMessage(socket, sendMessage.getBytes(StandardCharsets.UTF_8));
            this.waitResponse();
        } catch (IOException e) {
            logger.info("Can not connect the first relay node:" + chainRelayNodePortStrat);
        }


    }

    public boolean checkLockMessage(HashMap<String , String > unlockMessage){

        BigInteger Y =  this.YPows[0];
        BigInteger k = new BigInteger(unlockMessage.get("k"));
        BigInteger newY  = DiscreteLogarithmTool.modPow(k);
        if (!Y.equals(newY)){
            return false;
        }

        return true;

    }

    public static void main(String[] args) {
        ServerNode serverNode = new ServerNode();
        serverNode.start();
    }
}
