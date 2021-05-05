package myAPP;

import opennlp.tools.parser.Cons;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class BrokerNode implements Broker{
    ServerSocket providerSocket;
    Socket connection = null;
    private ObjectOutputStream output; // output stream to client
    private ObjectInputStream input; // input stream from client
    ArrayList<ActionsForConsumer> consumers=new ArrayList<>();
    ArrayList<ActionsForPublishers> publishers=new ArrayList<>();

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
                int k=1;
                do{
                    try {
                        answer= (Message) consumers.get(i).in.readObject();
                        System.out.println("read "+k++);
                        chunks.add(answer.getData());
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }while(answer.getChunks()==1);
               /* request=new Message("Server",key,1,null);
                try {
                    consumers.get(i).out.writeObject(request);
                    consumers.get(i).out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }*/
                System.out.println("finished sent");
                for (int j=0;j<chunks.size();j++){
                    if(j==chunks.size()-1) {
                        request = new Message("Server", key, 0, chunks.get(i));
                    }else{
                        request = new Message("Server", key, 1, chunks.get(i));
                    }
                    System.out.println("sent chunk");
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
        BrokerNode broker=new BrokerNode();
        broker.connect();
    }
}