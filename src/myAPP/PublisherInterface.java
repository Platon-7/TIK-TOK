package myAPP;

import java.util.ArrayList;

public interface PublisherInterface extends Node {

    // channelName;

    void addHashTag(String hashTag);
    void removeHashTag(String hashTag);
    void getBrokerList();
    Broker hashTopic(String hashTag);
    void push(String hashTag, Value value);
    void notifyFailure(Broker broker);
    void notifyBrokersForHashTags(String hashTag);
    ArrayList<Value> generateChunks(String hashTag);
}
