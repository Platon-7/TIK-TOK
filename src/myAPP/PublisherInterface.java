package myAPP;

import java.util.ArrayList;

public interface PublisherInterface extends Node {

    // channelName;

    void addHashTag(String hashTag);
    void removeHashTag(String hashTag);
    void getBrokerList();
    BrokerInterface hashTopic(String hashTag);
    void push(String hashTag, Value value);
    void notifyFailure(BrokerInterface broker);
    void notifyBrokersForHashTags(String hashTag);
    ArrayList<Value> generateChunks(String hashTag);
}
