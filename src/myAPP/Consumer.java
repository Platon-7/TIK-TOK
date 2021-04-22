package myAPP;

import myAPP.Broker;
import myAPP.Node;

public interface Consumer extends Node {

    void register(Broker broker, String user);
    void disconnect(Broker broker, String user);
    void playData(String data, Value value);
}
