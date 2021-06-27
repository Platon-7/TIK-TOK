package com.example.myAPP;

import java.io.*;
import java.net.*;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class BrokerNode implements Broker{
    HashMap<String, Long> hashBrokers = new HashMap<>();
    List brokersList=new ArrayList();
    ArrayList<String> topics = new ArrayList<>();
    ArrayList<String> newHashtags = new ArrayList<>();
    ArrayList<String> info=new ArrayList<>();
    Socket requestSocket = null;
    HashMap<String,List<String>> brokerInfo=new HashMap<>();
    int PORT;
    ServerSocket providerSocket;
    Socket connection = null;
    ArrayList<ActionsForConsumer> consumers=new ArrayList<>();
    ArrayList<ActionsForPublishers> publishers=new ArrayList<>();
    ArrayList<ActionsForBrokers> brokers=new ArrayList<>();
    int brokerInt;
    boolean brokerFlag=false;
    String publisherIp="";
    String publisherPort="";
    HashMap<String,List<String>> listOfpubs=new HashMap<>();

    public BrokerNode(String PORT) {
        this.PORT=Integer.parseInt(PORT);
        connect();
    }

    public  void ConnectToPublisher(String ip,String port){

        try {
            requestSocket=new Socket(ip,Integer.parseInt(port));
        } catch (IOException e) {
            e.printStackTrace();
        }
        ActionsForPublishers pubThread = new ActionsForPublishers(requestSocket, this);
        pubThread.start();
        publishers.add(pubThread);
        publisherIp=ip;
        publisherPort=port;
        System.out.println("Connected to "+ip+" in port "+port);
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

        boolean someoneHadvideo=false;
        Iterator<ActionsForConsumer> iterator = consumers.iterator();
        while (iterator.hasNext()) {
            ActionsForConsumer thread = iterator.next();
            if (!thread.isAlive()) {
                iterator.remove();
            }
        }
        Iterator<ActionsForPublishers> iteratorPub = publishers.iterator();
        Message msg=new Message("Server",key,null,null);
        for(int i=0;i<consumers.size();i++){
            if (consumers.get(i).equals(Thread.currentThread())) {
                for (int j = 0; j < publishers.size(); j++) {
                    if (!consumers.get(i).getHashCode().equals(publishers.get(j).getHashCode())) {
                        try {
                            publishers.get(j).out.writeObject(msg);
                            publishers.get(j).out.flush();
                            msg = (Message) publishers.get(j).in.readObject();
                        } catch (IOException | ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                        if (!msg.getFlag().equals("No results")) {
                            try {
                                consumers.get(i).out.writeObject(msg);
                                consumers.get(i).out.flush();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            someoneHadvideo = true;
                            int counter = Integer.parseInt(msg.getFlag());
                            do {
                                do {
                                    try {
                                        msg = (Message) publishers.get(j).in.readObject();
                                        consumers.get(i).out.writeObject(msg);
                                        consumers.get(i).out.flush();
                                    } catch (IOException | ClassNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                } while (Integer.parseInt(msg.getFlag()) > 0);
                                counter--;
                                if(counter>0)
                                System.out.println("PULLED AND SENT VIDEO "+ msg.getKey());
                            } while (counter > 0);
                        }
                    }
                }
                if (!someoneHadvideo) {
                    try {
                        consumers.get(i).out.writeObject(msg);
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
        // edw tha mpoyn ola ta hashtags
        Message sendHash;
        List<Integer> taken = new ArrayList<>();
        List<Integer> pos =new ArrayList<>();
        long value;
        Collections.sort(brokersList);
        for (int j = 0; j < brokersList.size(); j++) {
            for (int i = 0; i < newHashtags.size(); i++) {
                String tempHash = null;
                if (!taken.contains(i)) {
                    try {
                        tempHash = Broker.hashFunction(newHashtags.get(i));
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    value = Long.valueOf(tempHash, 16);
                    if ((long) brokersList.get(j) > value) {
                        if (brokersList.get(j) == hashBrokers.get("Server")) {
                            topics.add(newHashtags.get(i));
                            System.out.println("RECEIVED topic" + newHashtags.get(i));
                        } else {
                            sendHash = new Message("Server", newHashtags.get(i), "hash", null);
                            for (int h = 0; h < hashBrokers.size() - 1; h++) {
                                if (brokersList.get(j) == hashBrokers.get("Broker" + h))
                                    try {
                                        pos.add(h);
                                        brokers.get(h).out.writeObject(sendHash);
                                        brokers.get(h).out.flush();
                                        if(!brokerInfo.get("Broker" + h).contains(newHashtags.get(i))) {
                                            brokerInfo.get("Broker" + h).add(newHashtags.get(i));
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                            }
                        }
                        taken.add(i);
                    } else {
                        if (j == hashBrokers.size() - 1) {
                            System.out.println("RECEIVED topic: " + newHashtags.get(i));
                            topics.add(newHashtags.get(i));
                        }
                    }
                }
            }
        }
        info.addAll(topics);
        brokerInfo.put("Server",info);
        try {
            brokerInfo.get("Server").add(0,InetAddress.getLocalHost().getHostAddress());
            brokerInfo.get("Server").add(1,String.valueOf(PORT));
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

        sendHash = new Message(publisherIp, publisherPort, "Publisher", null);
        for(int i=0;i<brokersList.size()-1;i++){
                if (pos.contains(i)) {
                    try {
                        brokers.get(i).out.writeObject(sendHash);
                        brokers.get(i).out.flush();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
        }
        for(int i=0;i<brokerInfo.size()-1;i++) {
            int counter=brokerInfo.get("Broker" + i).size();
            for (int j = 0; j < brokerInfo.get("Broker" + i).size(); j++) {
                for (int k = 0; k < brokerInfo.size() -1;k++) {
                    if(k!=i) {
                        try {
                            sendHash = new Message("Broker" + i, brokerInfo.get("Broker" + i).get(j), String.valueOf(counter), null);
                            brokers.get(k).out.writeObject(sendHash);
                            brokers.get(k).out.flush();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                counter--;
            }
            counter=brokerInfo.get("Server").size();
            for (int k = 0; k < brokerInfo.get("Server").size(); k++) {
                sendHash = new Message("Server", brokerInfo.get("Server").get(k), String.valueOf(counter), null);
                try {
                    brokers.get(i).out.writeObject(sendHash);
                    brokers.get(i).out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                counter--;
            }
        }
        newHashtags.removeAll(newHashtags);
    }
        @Override
    public List<Broker> getBrokers() {
        return null;
    }

    @Override
    public void connect() {
        int temp = 0;
        Scanner input = new Scanner(System.in);
        do {
            System.out.println("Is this the main server? yes/no:");
            String an = input.nextLine();
            if (an.equals("no")) {
                brokerInt = -1;
                brokerFlag = true;
                break;
            } else if (an.equals("yes")){
                System.out.println("How many other servers will connect? ");
                brokerInt = input.nextInt();

                try {
                    hashBrokers.put("Server", Long.valueOf(Broker.hashFunction("127.0.0.1" + PORT), 16));
                    brokersList.add(hashBrokers.get("Server"));
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                }
                break;
            }
        }while(true);

        Message answer;
        try {
            providerSocket = new ServerSocket(PORT);
            while (true) {
                if (brokerFlag&&temp==0) {
                    try {
                        requestSocket = new Socket("127.0.0.1", 4321);
                        ActionsForBrokers broker = new ActionsForBrokers(requestSocket, this);
                        broker.start();
                        temp++;

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }else{
                    System.out.println("Waiting for connection");
                    connection = providerSocket.accept();
                    System.out.println("Connection received from: " + connection.getInetAddress().getHostAddress());
                    if(temp>=brokerInt) {
                        ActionsForConsumer consThread = new ActionsForConsumer(connection, this);
                        consumers.add(consThread);
                        consThread.start();
                    }else {
                        List<String> info=new ArrayList<>();
                        ActionsForBrokers broker = new ActionsForBrokers(connection,this);
                        brokers.add(broker);
                        answer = (Message) broker.in.readObject();
                        hashBrokers.put("Broker"+temp,Long.valueOf(Broker.hashFunction(answer.channelName+answer.getKey()),16));
                        info.add(answer.channelName);
                        info.add(answer.getKey());
                        brokersList.add(hashBrokers.get("Broker"+temp));
                        brokerInfo.put("Broker"+temp,info);
                        temp++;
                    }
                }
            }
        } catch (IOException | ClassNotFoundException ioException) {

        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }finally {
            try {
                providerSocket.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
    }

    @Override
    public void disconnect() {

    }

    public void updateHashtags(){
        Message sendHash;
        List<Integer> taken = new ArrayList<>();
        List<Integer> pos =new ArrayList<>();
        long value;
        for (int j = 0; j < brokersList.size(); j++) {
            for (int i = 0; i < newHashtags.size(); i++) {
                String tempHash = null;
                if (!taken.contains(i)) {
                    try {
                        tempHash = Broker.hashFunction(newHashtags.get(i));
                    } catch (NoSuchAlgorithmException e) {
                        e.printStackTrace();
                    }
                    value = Long.valueOf(tempHash, 16);
                    if ((long) brokersList.get(j) > value) {
                        if (brokersList.get(j) == hashBrokers.get("Server")) {
                            topics.add(newHashtags.get(i));
                            brokerInfo.get("Server").add(newHashtags.get(i));
                        } else {
                            sendHash = new Message("Server", newHashtags.get(i), "hash", null);
                            for (int h = 0; h < hashBrokers.size() - 1; h++) {
                                if (brokersList.get(j) == hashBrokers.get("Broker" + h))
                                    try {
                                        pos.add(h);
                                        brokers.get(h).out.writeObject(sendHash);
                                        brokers.get(h).out.flush();
                                        if(!brokerInfo.get("Broker" + h).contains(newHashtags.get(i))) {
                                            brokerInfo.get("Broker" + h).add(newHashtags.get(i));
                                        }
                                    } catch (IOException e) {
                                        e.printStackTrace();
                                    }
                            }
                        }
                        taken.add(i);
                    } else {
                        if (j == hashBrokers.size() - 1) {

                            topics.add(newHashtags.get(i));
                            brokerInfo.get("Server").add(newHashtags.get(i));

                        }
                    }
                }
            }
        }
        sendHash = new Message(publisherIp, publisherPort, "Publisher", null);
        for(int i=0;i<brokersList.size()-1;i++){
            if (pos.contains(i)) {
                try {
                    brokers.get(i).out.writeObject(sendHash);
                    brokers.get(i).out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        for(int i=0;i<brokerInfo.size()-1;i++) {
            int counter=brokerInfo.get("Broker" + i).size();
            for (int j = 0; j < brokerInfo.get("Broker" + i).size(); j++) {
                for (int k = 0; k < brokerInfo.size() -1;k++) {
                    if(k!=i) {
                        try {
                            sendHash = new Message("Broker" + i, brokerInfo.get("Broker" + i).get(j), String.valueOf(counter), null);
                            brokers.get(k).out.writeObject(sendHash);
                            brokers.get(k).out.flush();

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }
                counter--;
            }
            counter=brokerInfo.get("Server").size();
            for (int k = 0; k < brokerInfo.get("Server").size(); k++) {

                sendHash = new Message("Server", brokerInfo.get("Server").get(k), String.valueOf(counter), null);
                try {
                    brokers.get(i).out.writeObject(sendHash);
                    brokers.get(i).out.flush();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                counter--;
            }
        }

        newHashtags.removeAll(newHashtags);
    }
    @Override
    public ArrayList<String> updateNodes() {
        return null;
    }
    public static void main(String[] args)  {
        new BrokerNode(args[0]);

    }
}