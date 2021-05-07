package myAPP;

import java.math.BigInteger;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public interface Broker extends Node {
    List <String> hashBrokers = null;
    List <Consumer> registeredUsers = null;
    List <Publisher> registeredPublishers = null;


    void calculateKeys();

    Publisher acceptConnection(Publisher publisher);

    Consumer acceptConnection(Consumer consumer);

    void notifyPublisher(String consumer);

    void notifyBrokersOnChanges();

    static String hashFunction(String hashtag) throws NoSuchAlgorithmException {
        MessageDigest md = MessageDigest.getInstance("SHA-1");
        byte[] encodedhash = md.digest(hashtag.getBytes(StandardCharsets.UTF_8));
        BigInteger no = new BigInteger(1, encodedhash);
        String hashtext = no.toString(16);
        while (hashtext.length() < 32) {
            hashtext = "0" + hashtext;
        }
        return hashtext;
    }
    static void hashBrokers(String IP, String port) throws NoSuchAlgorithmException {
        String total = IP+port;
        hashBrokers.add(hashFunction(total));
    }

    void pull(String a);

    void filterConsumers(String a);
}
