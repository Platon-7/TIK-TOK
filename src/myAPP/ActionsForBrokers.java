package myAPP;



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
        Message key;
        try {
            Message info = new Message(client.getInetAddress().getHostAddress(), String.valueOf(broker.PORT), "Broker", null);
            out.writeObject(info);
            out.flush();
            System.out.println("Waiting  for messages from Server");
            while(true) {
                key = (Message) in.readObject();
                if (key.getFlag().equals("hash")) {
                    broker.topics.add(key.getKey());
                    System.out.println("RECEIVED hashtag" + key.getKey());
                } else if(key.getFlag().equals("Publisher"))
                {
                    broker.ConnectToPublisher(key.channelName,key.getKey());
                }else{
                    List<String> temp=new ArrayList<>();
                    int counter=Integer.parseInt(key.getFlag());
                    while(counter>0){
                        temp.add(key.getKey());
                        counter--;
                        if(counter==0) break;
                        key = (Message) in.readObject();
                    }
                    System.out.println("HASHMAP "+key.getChannelName()+" , ");
                    for(int i=0;i<temp.size();i++)
                    {
                        System.out.println("has this hash" +temp.get(i));
                    }
                    broker.brokerInfo.put(key.getChannelName(),temp);
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}