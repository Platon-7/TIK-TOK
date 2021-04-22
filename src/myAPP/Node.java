package myAPP;

import java.util.List;
import org.apache.tika.Tika;

public interface Node {

    Tika tika = new Tika();
    List<Broker> brokers = null;

    void init();//eixe int orisma

    List<Broker> getBrokers();

    void connect();

    void disconnect();

    void updateNodes();
}
