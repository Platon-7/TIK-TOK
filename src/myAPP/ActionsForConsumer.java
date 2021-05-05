package myAPP;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.*;

public class ActionsForConsumer extends Thread {
    ObjectInputStream in;
    ObjectOutputStream out;
    Socket client;
    BrokerNode broker;

    public ActionsForConsumer(Socket connection,BrokerNode broker) {
        this.client=connection;
        this.broker=broker;
        System.out.println(currentThread().getId());
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
        //τα αντικείμενα in και out έχουν ήδη αρχικοποιηθεί
        //κατά την δημιουργία του αντικειμένου (στον constructor)

        try {
            Message key =(Message) in.readObject();
            broker.pull(key.getKey());
            System.out.println("RECEIVED KEY");

            
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
}