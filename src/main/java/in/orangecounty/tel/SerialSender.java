package in.orangecounty.tel;

import java.io.IOException;
import java.util.TooManyListenersException;

/**
 * Created by thomas on 6/3/15.
 */
public interface SerialSender {
    public void sendMessage(byte[] message) throws Exception;
    public void setSerialListener(SerialListener serialListener);
    public void start() throws IOException;
    public void stop();
    public void listPorts();
}
