package com.example.myAPP;

import java.util.ArrayList;
import java.util.List;


public interface Node {


    List<Broker> brokers = null;

    void init();//eixe int orisma

    List<Broker> getBrokers();

    void connect();

    void disconnect();

    ArrayList<String> updateNodes();
}
