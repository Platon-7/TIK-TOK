package myAPP;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.security.NoSuchAlgorithmException;

public class ActionsForConsumer extends Thread {
    ObjectInputStream in;
    ObjectOutputStream out;
    Socket client;
    BrokerNode broker;
    String hashCode;

    public String getHashCode() {
        return hashCode;
    }

    public ActionsForConsumer(Socket connection, BrokerNode broker) {
        this.client=connection;
        this.broker=broker;
        try {
            out = new ObjectOutputStream(client.getOutputStream());
            in = new ObjectInputStream(client.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while(true) {
            try {
                Message msg;
                do {
                    msg = (Message) in.readObject();
                    if (msg.getFlag().equals("PublisherInfo")) {
                        broker.ConnectToPublisher(client.getInetAddress().getHostAddress(), msg.getKey());
                    } else if (msg.getFlag().equals("new Hashtags")) {
                        do {
                            msg = (Message) in.readObject();
                            broker.newHashtags.add(msg.getKey());
                        } while (Integer.parseInt(msg.getFlag()) > 0);
                    }
                }while (msg.getFlag().equals("Consumer"));
                hashCode = Broker.hashFunction(msg.channelName);
                System.out.println("RECEIVED KEY " + msg.getKey());
                if (broker.topics.contains(msg.getKey())) {
                    broker.pull(msg.getKey());
                } else {
                    for (String entry : broker.brokerInfo.keySet()) {
                        if (broker.brokerInfo.get(entry).contains(msg.getKey())) {
                            for(int i=0;i<broker.brokerInfo.get(entry).size();i++){
                                System.out.println("this "+entry+" has "+broker.brokerInfo.get(entry).get(i));
                            }
                            msg = new Message(broker.brokerInfo.get(entry).get(0), broker.brokerInfo.get(entry).get(1), "Redirect", null);
                            out.writeObject(msg);
                            out.flush();
                            System.out.println("The key was not in this broker redirecting...");
                            in.close();
                            out.close();
                            client.close();
                            return;
                        }
                    }

                    msg = new Message("Server", msg.getKey(), "No results", null);
                    out.writeObject(msg);
                    out.flush();
                }
            } catch (IOException | ClassNotFoundException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        }
    }
}