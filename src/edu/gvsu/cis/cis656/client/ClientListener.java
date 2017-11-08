package edu.gvsu.cis.cis656.client;

import edu.gvsu.cis.cis656.clock.VectorClock;
import edu.gvsu.cis.cis656.clock.VectorClockComparator;
import edu.gvsu.cis.cis656.message.Message;
import edu.gvsu.cis.cis656.message.MessageComparator;
import edu.gvsu.cis.cis656.queue.PriorityQueue;
import org.json.JSONObject;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.Vector;

public class ClientListener implements Runnable {

    private DatagramSocket listenSocket;
    private PriorityQueue queue;
    private VectorClock vectorClock;


    public ClientListener(DatagramSocket listenSocket, VectorClock vc) {
        this.listenSocket = listenSocket;
        this.vectorClock = vc;
    }

    @Override
    public void run() {
        MessageComparator comparator = new MessageComparator();
        this.queue = new PriorityQueue(comparator);


        System.out.println("Listener Started...");
        listenerMethod();

    }

    private void listenerMethod() {
        while (true) {

            try {
                Message msg = Message.receiveMessage(listenSocket);
//                byte[] buffer = new byte[1024];
//                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
//                try {
//
//                    listenSocket.receive(packet);
//                    String received = new String(packet.getData(), 0, packet.getLength());
//                    Message parsedMessage = Message.parseMessage(received);

                switch (msg.type) {
                    case 2:
                        this.queue.add(msg);
                        break;
                }

                Message topMsg = (Message) queue.peek();

                //prints incoming messages
                printMessages(topMsg);
            } catch (Exception e) {
            }
        }
    }


    private void printMessages(Message topMsg) {
        while (topMsg != null) {
            //expect the next message for a given proceess based on current clock
            if (vectorClock.getTime(topMsg.pid) + 1 == topMsg.ts.getTime(topMsg.pid)) {

                //do conversion
//                JSONObject topMessageClockAsJson = new JSONObject(topMsg.ts.toString());
//                Map<String, Object> mapClock = topMessageClockAsJson.toMap();
////                mapClock.remove("" + topMsg.pid);
//                VectorClock tempClock = new VectorClock();
//                tempClock.setClockFromString(mapClock.toString());

                VectorClock tmp = new VectorClock();
                tmp.setClock(topMsg.ts);
                Integer localTimeForSender = vectorClock.getTime(topMsg.pid);
                tmp.addProcess(topMsg.pid, localTimeForSender);

                //ensure that the top message happened before the current vector clock.
                if (this.vectorClock.happenedBefore(tmp)) {
                    System.out.println("("+topMsg.tag+")"+topMsg.sender + ": " + topMsg.message);
                    this.vectorClock.update(topMsg.ts);
                    this.queue.remove(topMsg);
                    if (this.queue.isEmpty()) {
                        break;
                    }
                    topMsg = (Message) this.queue.peek();
                }
            } else {
                topMsg = null;
            }
        }
    }

    public void syncVectorClock(VectorClock otherClock) {
        this.vectorClock.update(otherClock);
    }
}

