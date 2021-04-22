package myAPP;

import java.util.List;

public class Broker implements BrokerInterface{
    @Override
    public void calculateKeys() {

    }

    @Override
    public Publisher acceptConnection(Publisher publisher) {
        return null;
    }

    @Override
    public Consumer acceptConnection(Consumer consumer) {
        return null;
    }

    @Override
    public void notifyPublisher(String consumer) {

    }

    @Override
    public void notifyBrokersOnChanges() {

    }

    @Override
    public void pull(String a) {

    }

    @Override
    public void filterConsumers(String a) {

    }

    @Override
    public void init() {

    }

    @Override
    public List<BrokerInterface> getBrokers() {
        return null;
    }

    @Override
    public void connect() {

    }

    @Override
    public void disconnect() {

    }

    @Override
    public void updateNodes() {

    }
}
