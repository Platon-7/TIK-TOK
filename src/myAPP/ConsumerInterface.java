package myAPP;

import java.util.List;

public interface ConsumerInterface extends AppNodeInterface {

    void register(BrokerInterface broker, String user);
    void disconnect(BrokerInterface broker, String user);
    void playData(String data, Value value);

    @Override
    void init();

    @Override
    List<BrokerInterface> getBrokers();

    @Override
    void connect();

    @Override
    void disconnect();

    @Override
    void updateNodes();
}
