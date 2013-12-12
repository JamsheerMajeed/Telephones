package in.orangecounty;

/**
 * User: thomas
 * Date: 26/11/13
 * Time: 2:56 PM
 */
public interface ListenerSenderInterface {

    public void sendACK();

    public void ackReceived();

    public void sendNAK();

    public void sendEOT();

    public void setReceiving(boolean receiving);

    public void nakReceived();

    public boolean isSending();

    public void interrupt();

    void resendSelectSequence();
}
