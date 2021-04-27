package myAPP;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

public class Broker implements BrokerInterface{
    Socket requestSocket = null;
    OutputStream out;
    //ροή για να παίρνεις δεδομένα από τον διακομιστή
    DataInputStream in;
    ServerSocket providerSocket;
    Socket connection = null;
    private DataOutputStream output; // output stream to client
    private InputStream input; // input stream from client
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
        try {
        //Δημιουργία serverSocket που ακούει στην πόρτα 4321
        //και έχει μέγεθος ουράς 10
        providerSocket = new ServerSocket(4322, 10);

        //Αναμονή για σύνδεση με πελάτη
        System.out.println("Waiting for connection");
        connection = providerSocket.accept();
        System.out.println( "Connection received from: " +
                connection.getInetAddress().getHostName() );
        output = new DataOutputStream(connection.getOutputStream());
        output.flush(); // flush output buffer to send header information
        input =  connection.getInputStream() ;

    } catch (IOException ioException) {
        ioException.printStackTrace();
    }
        return null;
    }

    @Override
    public void notifyPublisher(String consumer) {

    }

    @Override
    public void notifyBrokersOnChanges() {

    }

    @Override
    public void pull(String a) {
        //try {
           // output.writeObject(a);
       // } catch (IOException e) {
       //     e.printStackTrace();
       // }
    }

    @Override
    public void filterConsumers(String a) {

    }

    @Override
    public void init() {

    }

    @Override
    public List<BrokerInterface> getBrokers() {
        return null;
    }

    @Override
    public void connect() {

        try {

            //Δημιουργία υποδοχής που θα συνδεθεί με τη θύρα 4321 στο διακομιστή
            //ο οποίος βρίσκεται στην διεύθυνση με IP 127.0.0.1
            requestSocket = new Socket("127.0.0.1", 4321);

            //Obtain Socket’s OutputStream and use it to initialize ObjectOutputStream
            out = requestSocket.getOutputStream();

            //Obtain Socket’s InputStream and use it to initialize ObjectInputStream
            in = new DataInputStream(requestSocket.getInputStream());


        } catch (UnknownHostException unknownHost) {
            System.err.println("You are trying to connect to an unknown host!");
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
        Broker broker=new Broker();
        Consumer cons=new Consumer();
        broker.connect();
        int length= 0;
        try {
            length = broker.in.readInt();

        } catch (IOException e) {
            e.printStackTrace();
        }
        if(length>0){
            byte[] message = new byte[length];
            try {
                broker.in.readFully(message, 0, message.length); // read the message
            } catch (IOException e) {
                e.printStackTrace();
            }
            broker.acceptConnection(cons);
            try {
                System.out.println("lenth "+ message);
                broker.output.writeInt(message.length);
                broker.output.write(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }


}
