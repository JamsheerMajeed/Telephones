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

    /*   1 - Outgoing Restriction
    *    0 - No Restriction       */
    void setRestriction(String extension, String status);

    void setName(String extension, String name);
}
