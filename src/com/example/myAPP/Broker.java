package com.example.myAPP;


import org.apache.commons.codec.digest.DigestUtils;

import java.security.NoSuchAlgorithmException;
import java.util.List;

public interface Broker extends Node {
    List <Consumer> registeredUsers = null;
    List <Publisher> registeredPublishers = null;


    void calculateKeys();

    Publisher acceptConnection(Publisher publisher);

    Consumer acceptConnection(Consumer consumer);

    void notifyPublisher(String consumer);

    void notifyBrokersOnChanges();

    static String hashFunction(String hashtag) throws NoSuchAlgorithmException {

        return DigestUtils.sha1Hex(hashtag).substring(0,15);

    }

    void pull(String a);

    void filterConsumers(String a);
}
