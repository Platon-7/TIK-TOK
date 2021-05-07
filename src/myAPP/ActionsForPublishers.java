package myAPP;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ActionsForPublishers extends Thread{
    ObjectInputStream in;
    ObjectOutputStream out;
    Socket client;
    BrokerNode broker;

    public ActionsForPublishers(Socket connection,BrokerNode broker) {
        this.client=connection;
        this.broker=broker;
        //ο constructor αρχικοποιεί τα αντικείμενα-ροές για την επικοινωνία με τον αντίστοιχο πελάτη
        try {
            out = new ObjectOutputStream(client.getOutputStream());
            //out: για γράψιμο στον πελάτη

            in = new ObjectInputStream(client.getInputStream());
            //in: για διάβασμα από τον πελάτη
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        Message answer;
        try {
            do {
            answer = (Message) in.readObject();
            broker.topics.add(answer.getKey());
        }while(answer.getChunks()==1);
            broker.topics.add(answer.getChannelName());
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}
