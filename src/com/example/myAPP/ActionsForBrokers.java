package com.example.myAPP;



import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ActionsForBrokers extends Thread {
    ObjectInputStream in;
    ObjectOutputStream out;
    Socket client;
    BrokerNode broker;

    public ActionsForBrokers(Socket connection,BrokerNode broker) {
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
        Message msg;
        try {
            Message info = new Message(client.getInetAddress().getHostAddress(), String.valueOf(broker.PORT), "Broker", null);
            out.writeObject(info);
            out.flush();
            System.out.println("Waiting  for messages from Server");
            while(true) {
                msg = (Message) in.readObject();
                if (msg.getFlag().equals("hash")) {
                    broker.topics.add(msg.getKey());
                    System.out.println("RECEIVED hashtag " + msg.getKey());
                } else if(msg.getFlag().equals("Publisher"))
                {
                    broker.ConnectToPublisher(msg.channelName,msg.getKey());
                }else{
                    List<String> temp=new ArrayList<>();
                    int counter=Integer.parseInt(msg.getFlag());
                    while(counter>0){
                        temp.add(msg.getKey());
                        counter--;
                        if(counter==0) break;
                        msg = (Message) in.readObject();
                    }
                    broker.brokerInfo.put(msg.getChannelName(),temp);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
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