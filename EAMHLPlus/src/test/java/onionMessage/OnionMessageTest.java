package onionMessage;

import client.ClientNode;
import node.RelayNode;
import org.aion.tetryon.*;
import org.junit.Test;
import server.ServerNode;
import tools.MyUtils;

import java.awt.*;
import java.io.*;
import java.math.BigInteger;
import java.util.Base64;
import java.util.HashMap;
import java.util.Properties;

public class OnionMessageTest {
    @Test
    public void onionMessageTest() throws Exception {
        // for (int k = 0; k < 10; k++) {
        //
        // }
        int relayNodeCounts = 3;
        Properties prop = new Properties();
        String configFile = "src/main/java/config.properties";
        try {
            DataInputStream dataInputStream = new DataInputStream(new FileInputStream(configFile));
            prop.load(dataInputStream);
        } catch (IOException e) {
            System.out.println("Can't load config file");
        }

        G2Point hPoint = Util.deserializeG2(Base64.getDecoder().decode(prop.getProperty("H_POINT")));

        String clientSignKeyFileName = prop.getProperty("CLIENT_SIGN_KEY_FILENAME");
        ClientNode clientNode = new ClientNode();
        clientNode.initNodeInfo(clientSignKeyFileName);

        String serverSignKeyFileName = prop.getProperty("SERVER_SIGN_KEY_FILENAME");
        ServerNode serverNode = new ServerNode();
        serverNode.initNodeInfo(serverSignKeyFileName);


        int nodePortStart = Integer.parseInt(prop.getProperty("RELAY_NODE_PORT_START"));
        int nodePort = 0;
        nodePort = nodePortStart;

        RelayNode[] relayNodes = new RelayNode[relayNodeCounts];
        for (int i = 0; i < relayNodeCounts; i++) {
            nodePort = nodePortStart + i * 10;
            String elgSk = prop.getProperty("NODE_" + nodePort + "_ElG_PRIVATE_KEY");
            String elgPk = prop.getProperty("NODE_" + nodePort + "_ElG_PUBLIC_KEY");
            relayNodes[i] = new RelayNode("127.0.0.1", nodePort, elgSk, elgPk, hPoint);
            String signKeyFileName = prop.getProperty("NODE_" + nodePort + "_SIGN_KEY_FILENAME");
            relayNodes[i].loadKeyFile(signKeyFileName);
        }

        serverNode.initNodeChannel(relayNodes[0].nodeInfo.get("address"));

        relayNodes[0].initNodeChannel(serverNode.nodeInfo.get("address"),  relayNodes[1].nodeInfo.get("address"));
        for (int i = 1; i < relayNodeCounts-1; i++) {
           relayNodes[i].initNodeChannel(relayNodes[i-1].nodeInfo.get("address"), relayNodes[i+1].nodeInfo.get("address"));
        }

        relayNodes[relayNodeCounts-1].initNodeChannel(relayNodes[relayNodeCounts-2].nodeInfo.get("address"), clientNode.nodeInfo.get("address"));
        clientNode.initNodeChannel(relayNodes[relayNodeCounts-1].nodeInfo.get("address"));





            GtPoint initYn = clientNode.sendToOnion();
            // get client message
            String route = serverNode.constructChainRoute(relayNodeCounts);
            HashMap<String, String> paddedSendRelayMessage = serverNode.sendToRelayNodes(new String(route), initYn);


            HashMap<String, String> nextMessage = relayNodes[0].forwardMessage(paddedSendRelayMessage);


            for (int i = 1; i < relayNodeCounts; i++) {
                // System.out.println("i = " + i);
                nextMessage = relayNodes[i].forwardMessage(nextMessage);
            }


            HashMap<String, String> clientMessage = nextMessage;

            HashMap<String, String> nextUnlockMessage = clientNode.forwardMessage(clientMessage);

            for (int i = relayNodeCounts - 1; i >= 0; i--) {
                nextUnlockMessage = relayNodes[i].backwardMessage(nextUnlockMessage);
            }
            G1Point RPoint = Util.stringToG1Point(nextUnlockMessage.get("R"));
            GtPoint finalY = Pairing.myPairing(RPoint, hPoint);
            assert(finalY.equals(serverNode.Yn));
            System.err.println("success");



        // int a = 1;
    }
}
