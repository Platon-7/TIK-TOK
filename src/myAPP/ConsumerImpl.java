package myAPP;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.List;
import java.util.Scanner;

public class ConsumerImpl implements Consumer {
    Socket requestSocket = null;
    DataOutputStream out;
    //ροή για να παίρνεις δεδομένα από τον διακομιστή
    DataInputStream in;
    @Override
    public void register(Broker broker, String user) {

    }

    @Override
    public void disconnect(Broker broker, String user) {

    }

    @Override
    public void playData(String data, Value value) {
        System.out.println("ENTERED PLAY DATA");
        int length= 0;
        try {

            length = in.readInt();

        } catch (IOException e) {
            e.printStackTrace();
        }
        if(length>0){
            byte[] message = new byte[length];
            try {
                in.readFully(message, 0, message.length); // read the message
            } catch (IOException e) {
                e.printStackTrace();
            }
            System.out.println(message.length);
            File mp4=new File("prodVideo/attempt_3.mp4");
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

    @Override
    public void init() {
        Scanner in=new Scanner(System.in);
       // do{
           // System.out.println("Please enter video search: ");
            //String key=in.nextLine();
           // System.out.println(key);
            try {
                out.writeUTF("#victory");
                System.out.println("SENT key");

            } catch (IOException e) {
                e.printStackTrace();
            }
            playData("#victory",null);
       // }while(!in.nextLine().equals("end"));
    }

    @Override
    public List<Broker> getBrokers() {
        return null;
    }

    @Override
    public void connect() {
        try {

            //Δημιουργία υποδοχής που θα συνδεθεί με τη θύρα 4321 στο διακομιστή
            //ο οποίος βρίσκεται στην διεύθυνση με IP 127.0.0.1
            requestSocket = new Socket("127.0.0.1", 4330);

            //Obtain Socket’s OutputStream and use it to initialize ObjectOutputStream
            out = new DataOutputStream(requestSocket.getOutputStream());

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
        ConsumerImpl cons=new ConsumerImpl();
        cons.connect();
        cons.init();

    }
}
