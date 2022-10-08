package server;

import contract.ChannelState;
import contract.Partner;
import encryption.AESEncryption;
import encryption.ECElGamalEncryption;
import encryption.RSAEncryption;
import encryption.RandomLetter;
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
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
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
    G1Point g1;
    G2Point h;
    public GtPoint Yn;
    String address;
    String configFile = "src/main/java/config.properties";
    Properties prop = null;
    public HashMap<String, String > nodeInfo;
    public ChannelState channel;

    public void initNodeChannel(String nextAddress){

        assert(this.nodeInfo != null);

        Partner PartnerA = new Partner(nodeInfo.get("address"), new BigInteger("100"));
        Partner PartnerB = new Partner(nextAddress, new BigInteger("100"));

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
        this.g1 = Util.deserializeG1(Base64.getDecoder().decode(prop.getProperty("G1_POINT")));
        this.h = Util.deserializeG2(Base64.getDecoder().decode(prop.getProperty("H_POINT")));

    }

    public HashMap<String, String> sendToRelayNodes(String onionMessage, GtPoint yn) {
        HashMap<String, String> sendMessage = new HashMap<String, String>();
        // String ynString = new String(Util.serializeGt(yn), Charset.forName("ISO-8859-1"));
        // String XnString = new String(Util.serializeG1(g1),Charset.forName("ISO-8859-1"));
        BigInteger secret = Util.getRandom();
        GtPoint newY = null;
        G1Point newX = null;
        try{
             newY = Gt.gtPow(yn, secret);
            newX = G1.mul(g1, secret);
        }catch (Exception e) {
            e.printStackTrace();
        }
        // logger.info("newY: "+newY);
        this.Yn = newY;
        HashMap<String, String> param = new HashMap<String, String>();
        param.put("Y", new String(Util.serializeGt(newY),Charset.forName("ISO-8859-1")));
        param.put("X", new String(Util.serializeG1(newX),Charset.forName("ISO-8859-1")));



        String newState = this.updateChannel();
        sendMessage.put("state", newState);
        long endTime = System.currentTimeMillis()+12*3600*1000;
        sendMessage.put("endTime", String.valueOf(endTime) );


        String sig = Signature.sign(newState, this.nodeInfo.get("privateKey"));
        sendMessage.put("signMessage", newState);
        sendMessage.put("sig", sig);
        sendMessage.put("onionMessage", onionMessage);
        sendMessage.put("sender", this.nodeInfo.get("address"));

        HashMap<String, String> paddedSendMessage = this.padMessage(sendMessage);
        paddedSendMessage.put("Y", new String(Util.serializeGt(newY),Charset.forName("ISO-8859-1")));
        paddedSendMessage.put("X", new String(Util.serializeG1(newX),Charset.forName("ISO-8859-1")));


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

        
        String nextPort = chainRelayNodePort[0];
        //String nextPort = nextOnionMessageMap.get("nextPort");
        int paddedLen;

        int maxMessageLen = Integer.parseInt(prop.getProperty("MAX_MESSAGE_LEN"));
        int len = messageMap.get("onionMessage").length();
        // System.out.println("len = " + len);


        if (messageMap.get("onionMessage").length() > maxMessageLen) {
            paddedLen = 0;

        } else {
            paddedLen = maxMessageLen - messageMap.get("onionMessage").length();
        }


        String randomLetter = RandomLetter.randomLetter(paddedLen);
        String nextPaddedMessage = messageMap.get("onionMessage") + randomLetter;
        messageMap.put("nextEncOniRouPadMessage", nextPaddedMessage);

        return messageMap;
    }

    public String constructChainRoute(int chainRelayNodeCounts) {
        this.chainRelayNodeCounts = chainRelayNodeCounts;


        chainRelayNodeElGPk = new String[chainRelayNodeCounts];
        chainRelayNodeAesKey = new String[chainRelayNodeCounts];
        chainRelayNodeIp = new String[chainRelayNodeCounts];
        chainRelayNodePort = new String[chainRelayNodeCounts];

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


        String message = "hello####";
        String newMessage = makeChainCell(message, chainRelayNodeCounts - 1);
        for (int i = chainRelayNodeCounts - 2; i >= 0; i--) {
            newMessage = makeChainCell(newMessage, i);
        }

        return newMessage;

    }


    private String makeChainCell(String message, int index) {
        //  Cell:  rsaEnc(aesKey)///len:aesEnc(ip:port:message)

        String ip;
        int port;
        if ((index + 1) != this.chainRelayNodeCounts) {
            ip = chainRelayNodeIp[index + 1];
            port = Integer.parseInt(chainRelayNodePort[index + 1]);
        } else {
            ip = clientIP;
            port = clientPort;
        }
        String aesKey = chainRelayNodeAesKey[index];

        String ElGkey = chainRelayNodeElGPk[index];
        //System.out.println("index: " + index + " rsakey = " + rsakey);

        String sendMessage = ip + ":::" + port + ":::" + new String(message);
        //serverLog.info("Select port: " + port + " as " + "relay node " + index);
        String encMessage = AESEncryption.encrypt(sendMessage, AESEncryption.formatAesKey(aesKey));




        String encMessageLen = Integer.toString(encMessage.length(),16)  ;
        assert(encMessageLen.length() <= 8);

        int padLen = 8 - encMessageLen.length();
        for (int i = 0; i < padLen; i++) {
            encMessageLen = '0' + encMessageLen;
        }



        String elgMsg = aesKey+":::" +encMessageLen;
        //System.out.println("index = " + index);
        //System.out.println("elgMsg = " + elgMsg);
        String encElgMsg = ECElGamalEncryption.ECElGEncrypt(elgMsg, ECElGamalEncryption.keyFromString(ElGkey));
        //System.out.println("encMessage = " + encMessage);

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



    public static void main(String[] args) {
        ServerNode serverNode = new ServerNode();
    }
}
