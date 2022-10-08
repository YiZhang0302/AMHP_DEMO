package tools;


import encryption.RSAEncryption;
import encryption.RandomLetter;
import org.aion.tetryon.*;
import web3j.Signature;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Properties;
import java.util.logging.Logger;

public class MyUtils {
    int messageLenMax = 1024;
    String configFile;

    public static void sendMessage(Socket sender, byte[] message) {
        try {
            DataOutputStream dout = new DataOutputStream(sender.getOutputStream());
            dout.writeInt(message.length);

            dout.write(message);
            dout.flush();
        } catch (IOException e) {
            System.out.println("Send message error!");
        }
    }

    public static byte[] receiveMessage(Socket receiver) {
        try {
            DataInputStream dint = new DataInputStream(receiver.getInputStream());
            int messageLen = dint.readInt();
            byte[] message = new byte[messageLen];
            dint.readFully(message, 0, messageLen);
            return message;
        } catch (IOException e) {
            System.out.println("Receive message error!");
        }

        return null;
    }

    public static boolean checkUnlockMessage(HashMap<String, String> lockMessageMap, HashMap<String, String> unlockMessageMap, G2Point pointH, Logger logger) {
        // check time
        // if (System.currentTimeMillis() > Long.parseLong(lockMessageMap.get("endTime"))) {
        //     logger.warning("time  had expired");
        //     return false;
        // }


        // check signature
        String senderAddress = unlockMessageMap.get("sender");

        if (!Signature.verify(lockMessageMap.get("signMessage"), unlockMessageMap.get("sig"),  senderAddress)) {
            logger.warning("Signature check failed");
            return false;
        }

        String Y = lockMessageMap.get("Y");
        String R = unlockMessageMap.get("R");
        GtPoint YPoint = Util.stringToGtPoint(Y);
        // System.out.println("YPoint = " + YPoint);
        G1Point RPoint = Util.stringToG1Point(R);
        GtPoint newY = null;
        try {
             newY = Pairing.myPairing(RPoint, pointH);

        }catch (Exception e){
            logger.warning("check e(R, h) == Y error");
        }

        // calculate e(R, h) == Y
        String result = null;
        if (!newY.equals(YPoint)) {
            return false;
        }


        return true;
    }

    public static boolean checkLockMessage(HashMap<String, String> messageMap, Logger logger) {
        // check end time
        // if (System.currentTimeMillis() > Long.parseLong(messageMap.get("endTime"))) {
        //     logger.warning("End time illegal");
        //     return false;
        //
        // }

        // check signature
        String senderAddress = messageMap.get("sender");

        String signData = messageMap.get("signMessage");
        if (!Signature.verify(messageMap.get("signMessage"), messageMap.get("sig"), messageMap.get("sender"))) {
            logger.warning("Signature check failed");
            return false;
        }


        return true;
    }

    public static HashMap<String, String> padOnionMessage(HashMap<String, String> nextOnionMessageMap, Logger log) {

        String configFile = "src/main/java/config.properties";
        String nextPort = nextOnionMessageMap.get("nextPort");
        int paddedLen;
        Properties prop = new Properties();
        try {
            DataInputStream dataInputStream = new DataInputStream(new FileInputStream(configFile));
            prop.load(dataInputStream);
        } catch (IOException e) {
            log.warning("Can't load config file!");
        }

        int maxMessageLen = Integer.parseInt(prop.getProperty("MAX_MESSAGE_LEN"));

        if (nextOnionMessageMap.get("nextOnionMessage").length() > maxMessageLen) {
            paddedLen = 0;
        } else {
            paddedLen = maxMessageLen - nextOnionMessageMap.get("nextOnionMessage").length();
        }
        String randomLetter = RandomLetter.randomLetter(paddedLen);
        String nextPaddedMessage = nextOnionMessageMap.get("nextOnionMessage") + randomLetter;
        nextOnionMessageMap.put("nextPaddedMessage", nextPaddedMessage);
        return nextOnionMessageMap;


    }

    public static String xor(String str1, String str2) {
        byte[] byte1 = str1.getBytes(StandardCharsets.UTF_8);
        byte[] byte2 = str2.getBytes(StandardCharsets.UTF_8);

        byte[] shortByte;
        byte[] longByte;


        if (byte1.length < byte2.length) {
            shortByte = byte1.clone();
            longByte = byte2.clone();
        } else {
            shortByte = byte2.clone();
            longByte = byte1.clone();
        }
        byte[] result = longByte.clone();
        for (int i = 0; i < shortByte.length; i++) {
            result[i] = (byte) (shortByte[i] ^ longByte[i]);
        }
        return new String(result, StandardCharsets.UTF_8);

    }

}
