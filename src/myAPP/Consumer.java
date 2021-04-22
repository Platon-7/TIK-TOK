package myAPP;

public interface Consumer extends Node {

    void register(BrokerInterface broker, String user);
    void disconnect(BrokerInterface broker, String user);
    void playData(String data, Value value);
}
