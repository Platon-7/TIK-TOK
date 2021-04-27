package myAPP;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;

public class Consumer implements ConsumerInterface {
    Socket requestSocket = null;
    OutputStream out;
    //ροή για να παίρνεις δεδομένα από τον διακομιστή
    DataInputStream in;
    @Override
    public void register(BrokerInterface broker, String user) {

    }

    @Override
    public void disconnect(BrokerInterface broker, String user) {

    }

    @Override
    public void playData(String data, Value value) {

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
            requestSocket = new Socket("127.0.0.1", 4322);

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
        Consumer cons=new Consumer();
        cons.connect();
        int length= 0;
        try {
            length = cons.in.readInt();

        } catch (IOException e) {
            e.printStackTrace();
        }
        if(length>0){
            byte[] message = new byte[length];
            try {
                cons.in.readFully(message, 0, message.length); // read the message
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(message.length);
            File mp4=new File("prodVideo/attempt_2.mp4");
            OutputStream os= null;
            try {
                os = new FileOutputStream(mp4);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            try {
                os.write(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }
}
