package myAPP;

import java.util.List;
import org.apache.tika.Tika;


public interface AppNodeInterface {

    Tika tika = new Tika();
    List<BrokerInterface> brokers = null;

    void init();//eixe int orisma

    List<BrokerInterface> getBrokers();

    void connect();

    void disconnect();

    void updateNodes();
}
