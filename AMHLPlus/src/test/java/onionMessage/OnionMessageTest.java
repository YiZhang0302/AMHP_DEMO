package onionMessage;

import client.ClientNode;
import node.RelayNode;
import org.junit.Test;
import server.ServerNode;

import java.io.*;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.Properties;

public class OnionMessageTest {
    @Test
    public void onionMessageTest() throws Exception {
        // int relayNodeCounts = 16;
        int relayNodeCounts = 16;
        Properties prop = new Properties();
        Properties costProP = new Properties();

        String configFile = "src/main/java/config.properties";
        try {
            DataInputStream dataInputStream = new DataInputStream(new FileInputStream(configFile));


            prop.load(dataInputStream);
        } catch (IOException e) {
            System.out.println("Can't load config file");
        }


        // init
        ClientNode clientNode = new ClientNode();
        String clientSignKeyFileName = prop.getProperty("CLIENT_SIGN_KEY_FILENAME");
        clientNode.initNodeInfo(clientSignKeyFileName);


        ServerNode serverNode = new ServerNode();
        String serverSignKeyFileName = prop.getProperty("SERVER_SIGN_KEY_FILENAME");
        serverNode.initNodeInfo(serverSignKeyFileName);


        int nodePortStart = Integer.parseInt(prop.getProperty("RELAY_NODE_PORT_START"));
        int nodePort = 0;
        nodePort = nodePortStart;
        RelayNode[] relayNodes = new RelayNode[relayNodeCounts];
        for (int i = 0; i < relayNodeCounts; i++) {
            nodePort = nodePortStart + i * 10;
            String elgSk = prop.getProperty("NODE_" + nodePort + "_ElG_PRIVATE_KEY");
            String elgPk = prop.getProperty("NODE_" + nodePort + "_ElG_PUBLIC_KEY");
            relayNodes[i] = new RelayNode("127.0.0.1", nodePort, elgSk, elgPk, new BigInteger("5"));
            String signKeyFileName = prop.getProperty("NODE_" + nodePort + "_SIGN_KEY_FILENAME");
            relayNodes[i].loadKeyFile(signKeyFileName);
        }

        serverNode.initNodeChannel(relayNodes[0].nodeInfo.get("address"));

        relayNodes[0].initNodeChannel(serverNode.nodeInfo.get("address"), relayNodes[1].nodeInfo.get("address"));
        for (int i = 1; i < relayNodeCounts - 1; i++) {
            relayNodes[i].initNodeChannel(relayNodes[i - 1].nodeInfo.get("address"), relayNodes[i + 1].nodeInfo.get("address"));
        }

        relayNodes[relayNodeCounts - 1].initNodeChannel(relayNodes[relayNodeCounts - 2].nodeInfo.get("address"), clientNode.nodeInfo.get("address"));
        clientNode.initNodeChannel(relayNodes[relayNodeCounts - 1].nodeInfo.get("address"));


        // get client message
        serverNode.constructChainOnionMessage(relayNodeCounts);
        String onionRoute = serverNode.constructChainRoute(relayNodeCounts);



        HashMap<String, String> paddedSendRelayMessage = serverNode.sendToRelayNodes(onionRoute);


        HashMap<String, String> nextMessage = relayNodes[0].forwardMessage(paddedSendRelayMessage);
        for (int i = 1; i < relayNodeCounts; i++) {
            System.out.println("i = " + i);
            nextMessage = relayNodes[i].forwardMessage(nextMessage);
        }


        HashMap<String, String> clientMessage = nextMessage;

        HashMap<String, String> nextUnlockMessage = clientNode.forwardMessage(clientMessage);

        for (int i = relayNodeCounts - 1; i >= 0; i--) {
            nextUnlockMessage = relayNodes[i].backwardMessage(nextUnlockMessage);
        }

        assert (serverNode.checkLockMessage(nextUnlockMessage));

        System.err.println("success");

    }
}
