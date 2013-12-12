package in.orangecounty;

/**
 * User: thomas
 * Date: 26/11/13
 * Time: 2:56 PM
 */
public interface SenderInterface {

    public boolean sendMessage(byte[] message);

    public boolean canSend();

}
