package in.orangecounty.tel;

/**
 * Created by jamsheer on 3/23/15.
 */
public interface DataLinkProtocol {
    void start();

    void stop();

    void listPorts();

    void sendMessage(String message);

    void sendStatus();

}
