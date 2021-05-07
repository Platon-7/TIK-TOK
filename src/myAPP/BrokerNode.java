package myAPP;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class BrokerNode implements Broker{
    ArrayList<String> topics = new ArrayList<>();
    Socket requestSocket = null;
    ObjectOutputStream out;
    //ροή για να παίρνεις δεδομένα από τον διακομιστή
    ObjectInputStream in;
    int PORT;
    ServerSocket providerSocket;
    Socket connection = null;
    ArrayList<ActionsForConsumer> consumers=new ArrayList<>();
    ArrayList<ActionsForPublishers> publishers=new ArrayList<>();

    public BrokerNode(String PORT) {
        this.PORT=Integer.parseInt(PORT);
        init();
    }

    @Override
    public void calculateKeys() {

    }

    @Override
    public Publisher acceptConnection(Publisher publisher)
    {
        return null;
    }

    @Override
    public Consumer acceptConnection(Consumer consumer) {

        return null;
    }

    @Override
    public void notifyPublisher(String channelName) {

    }

    @Override
    public void notifyBrokersOnChanges() {

    }

    @Override
    public void pull(String key) {
        System.out.println("pull with" +key);
        List<byte[]> chunks=new ArrayList<>();

        Message answer=new Message(null,null,0,null);
        Message request=new Message("Server",key,0,null);
        for(int i=0;i<publishers.size();i++){
            if (consumers.get(i).equals(Thread.currentThread())){
                try {
                    publishers.get(i).out.writeObject(request);
                    publishers.get(i).out.flush();
                    System.out.println("sent request");
                } catch (IOException e) {
                    e.printStackTrace();
                }
                do{
                    try {
                        answer= (Message) consumers.get(i).in.readObject();
                        chunks.add(answer.getData());
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }while(answer.getChunks()==1);

                System.out.println("finished read");
                for (int j=0;j<chunks.size();j++){
                    if(j==chunks.size()-1) {
                        request = new Message("Server", answer.getKey(), 0, chunks.get(j));
                    }else{
                        request = new Message("Server", answer.getKey(), 1, chunks.get(j));
                    }
                    try {
                        consumers.get(i).out.writeObject(request);
                        consumers.get(i).out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    @Override
    public void filterConsumers(String channelName) {

    }

    @Override
    public void init() {
        Scanner input = new Scanner(System.in);
        System.out.println("Connect to next server? y/n :");
        String key = input.nextLine();
        System.out.println(key);
        if (key.equals("y")) {
        try {
            requestSocket = new Socket("127.0.0.1", 4322);
            //Obtain Socket’s OutputStream and use it to initialize ObjectOutputStream
            out = new ObjectOutputStream(requestSocket.getOutputStream());

            //Obtain Socket’s InputStream and use it to initialize ObjectInputStream
            in = new ObjectInputStream(requestSocket.getInputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }
     }
        // edw tha mpoyn ola ta hashtags
        int counter1 = 0, counter2 = 0, counter3 = 0, counter4 =0;
    for (int i = 0; i < topics.size(); i++) {
        String tempHash = null;
        try {
            tempHash = Broker.hashFunction(topics.get(i));
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
        if (tempHash.compareTo(hashBrokers.get(0)) == -1) {
            counter1++;
            //Distribution to element1
        } else if (tempHash.compareTo(hashBrokers.get(1)) == -1) {
            counter2++;
            //Distribution to element2
        } else if (tempHash.compareTo(hashBrokers.get(2)) == -1) {
            counter3++;
            //Distribution to element3
        } else {
            System.out.println("Cant go to broker: " + topics.get(i));
            counter4++;
        }
    }
    System.out.println(counter1 + "  " + counter2 + "  " + counter3 + "  " + counter4);
    }


    @Override
    public List<Broker> getBrokers() {
        return null;
    }

    @Override
    public void connect() {

        try {
            //Δημιουργία serverSocket που ακούει στην πόρτα 4321
            //και έχει μέγεθος ουράς 10
            providerSocket = new ServerSocket(4321);
            //Αναμονή για σύνδεση με πελάτη
            int i=0;
            while (true) {
                System.out.println("Waiting for connection");
                connection = providerSocket.accept();
                System.out.println("Connection received from: " +
                        connection.getInetAddress().getHostName());

                i++;
                if(i%2==1) {
                    ActionsForPublishers pubThread = new ActionsForPublishers(connection,this);
                    publishers.add(pubThread);
                    pubThread.start();
                }else{
                    ActionsForConsumer consThread = new ActionsForConsumer(connection,this);
                    consumers.add(consThread);
                    consThread.start();
                }
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    @Override
    public void disconnect() {

    }

    @Override
    public void updateNodes() {

    }
    public static void main(String[] args){

        BrokerNode broker=new BrokerNode(args[0]);

        broker.connect();
    }
}