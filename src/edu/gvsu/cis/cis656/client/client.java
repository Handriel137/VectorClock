package edu.gvsu.cis.cis656.client;

import edu.gvsu.cis.cis656.clock.VectorClock;
import edu.gvsu.cis.cis656.message.Message;
import edu.gvsu.cis.cis656.message.MessageTypes;
import jdk.internal.util.xml.impl.Input;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

public class client {


    public static void main(String args[]) {

        // Register the user
        DatagramSocket listenSocket = null;
        Integer listenerPort = Integer.parseInt(args[0]);

        try {
            listenSocket = new DatagramSocket(listenerPort);
        } catch (Exception e) {
        }

        //register the user
        String name = getUserInput("What is your name? ");
        VectorClock vc = new VectorClock();
        Integer myPid = register(listenSocket, name, vc);
        vc.addProcess(myPid,0);

        ClientListener clientListener = new ClientListener(listenSocket, vc);

        //starts the listener
        Thread listenerThread = new Thread(clientListener);
        listenerThread.start();

        //accept messages from user to send messages.
        while (true) {

            String message = getUserInput("Type a message: ");
            try {
                vc.tick(myPid);
                clientListener.syncVectorClock(vc);
                Message chatMessage = new Message(MessageTypes.CHAT_MSG, name, myPid, vc, message);
                Message.sendMessage(chatMessage, listenSocket, InetAddress.getLocalHost(), 8000);

            } catch (Exception e) {
            }
        }


    }

    private static String getUserInput(String printLine) {
        String input = "";
        //get username first
        BufferedReader is = new BufferedReader(new InputStreamReader(System.in));
        System.out.println(printLine);
        try {
            input = is.readLine();
        } catch (Exception e) {
            System.exit(1);
        }
        return input;


    }

    private static Integer register(DatagramSocket listenSocket, String name, VectorClock vc) {
        Integer myPid = -1;
        try {
            Message registryMessage = new Message(MessageTypes.REGISTER, name, myPid, vc, "");
            Message.sendMessage(registryMessage, listenSocket, InetAddress.getLocalHost(), 8000);

        } catch (Exception e) {
        }

        try {

            byte[] buf = new byte[1024];
            DatagramPacket packet = new DatagramPacket(buf, buf.length);
            try {

                listenSocket.receive(packet);
                String recvd = new String(packet.getData(), 0, packet.getLength());
                Message parsedMessage = Message.parseMessage(recvd);
                switch (parsedMessage.type) {
                    case 1:
                        myPid = parsedMessage.pid;
                        break;
                    case 3:
                        System.exit(1);
                }
            } catch (Exception e) {
            }
        } catch (Exception e) {
        }
        return myPid;
    }
}
