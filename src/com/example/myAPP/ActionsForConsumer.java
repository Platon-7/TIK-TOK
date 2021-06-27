package com.example.myAPP;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;

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
                    hashCode = Broker.hashFunction(msg.channelName);
                    if (msg.getFlag().equals("PublisherInfo")) {
                        if(!broker.listOfpubs.containsKey(hashCode)) {
                            ArrayList<String> info=new ArrayList<>();
                            broker.ConnectToPublisher(client.getInetAddress().getHostAddress(), msg.getKey());
                            info.add(msg.getChannelName());
                            info.add(msg.getKey());
                            broker.listOfpubs.put(hashCode, info);
                        }
                    } else if (msg.getFlag().equals("new Videos")) {
                        do {
                            msg = (Message) in.readObject();
                            broker.newHashtags.add(msg.getKey());
                        } while (Integer.parseInt(msg.getFlag()) > 0);
                        broker.updateHashtags();
                    }else if(msg.getFlag().equals("new hashtag")){
                        broker.newHashtags.add(msg.getKey());
                        broker.updateHashtags();
                    }
                }while (!msg.getFlag().equals("Consumer"));
                System.out.println("RECEIVED KEY " + msg.getKey());
                if (broker.topics.contains(msg.getKey())) {
                    broker.pull(msg.getKey());
                } else {
                    for (String entry : broker.brokerInfo.keySet()) {
                        if (broker.brokerInfo.get(entry).contains(msg.getKey())) {
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
                try {
                    in.close();
                    out.close();
                    client.close();
                    return;
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }
        }
    }
}