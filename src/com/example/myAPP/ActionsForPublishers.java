package com.example.myAPP;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;


public class ActionsForPublishers extends Thread{
    ObjectInputStream in;
    ObjectOutputStream out;
    Socket client;
    BrokerNode broker;
    String hashCode;

    public ActionsForPublishers(Socket connection,BrokerNode broker) {
        this.client=connection;
        this.broker=broker;

        try {
            out = new ObjectOutputStream(client.getOutputStream());
            in = new ObjectInputStream(client.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getHashCode() {
        return hashCode;
    }
    public void run() {
        Message msg;
        if (broker.brokerFlag) {
            msg = new Message("broker", null, null, null);
            try {
                out.writeObject(msg);
                out.flush();
                msg= (Message) in.readObject();
                hashCode=Broker.hashFunction(msg.channelName);
            } catch (IOException | ClassNotFoundException | NoSuchAlgorithmException e) {
                e.printStackTrace();
            }
        } else {
            msg = new Message("Server", null, null, null);
            try {
                out.writeObject(msg);
                out.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                do {

                    msg = (Message) in.readObject();
                    if(Integer.parseInt(msg.getFlag())==-1) break;
                    broker.newHashtags.add(msg.getKey());
                } while (Integer.parseInt(msg.getFlag()) > 0);
                broker.newHashtags.add(msg.getChannelName());
                hashCode=Broker.hashFunction(msg.channelName);
                broker.init();
            } catch (IOException | ClassNotFoundException | NoSuchAlgorithmException e) {
                try {
                    in.close();
                    out.close();
                    client.close();
                } catch (IOException ioException) {
                    ioException.printStackTrace();
                }
            }

        }
    }
}
