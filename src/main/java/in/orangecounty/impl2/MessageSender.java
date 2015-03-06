package in.orangecounty.impl2;

import java.io.IOException;
import java.util.TooManyListenersException;

/**
 * Created by thomas on 6/3/15.
 */
public interface MessageSender {
    public void sendMessage(byte[] message) throws IOException;
    public void start() throws IOException;
    public void stop();
    public void listPorts();
}
