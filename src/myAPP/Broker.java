package myAPP;

import java.util.List;

public interface Broker extends Node {
    List <Consumer> registeredUsers = null;
    List <Publisher> registeredPublishers = null;

    void calculateKeys();

    Publisher acceptConnection(Publisher publisher);

    Consumer acceptConnection(Consumer consumer);

    void notifyPublisher(String consumer);

    void notifyBrokersOnChanges();

    void pull(String a);

    void filterConsumers(String a);
}
