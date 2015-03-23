package in.orangecounty.tel;

/**
 * Created by jamsheer on 3/23/15.
 */
public interface NEAX7400PmsProtocol {
    public void start();

    void stop();

    void listPorts();

    void sendMessage(String message);

    void sync();
}
