package in.orangecounty.exp;

import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import in.orangecounty.impl.SenderImpl;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import static in.orangecounty.impl.Constants.*;

/**
 * Created by thomas on 19/12/13.
 */
public class SerialListener implements SerialPortEventListener {
    Logger log = LoggerFactory.getLogger(SerialListener.class);
    InputStream inputStream;
    SenderImpl sender;
    StringBuilder buffer = new StringBuilder();

    public SerialListener(InputStream inputStream, SenderImpl sender) {
        this.inputStream = inputStream;
        this.sender = sender;
    }

    @Override
    public void serialEvent(SerialPortEvent ev) {
        log.debug("Serial Event Received :" + ev.getEventType());
        switch (ev.getEventType()){
            case(SerialPortEvent.DATA_AVAILABLE):
                log.debug("Data Available Event Received");
                try {
                    int x = inputStream.read();
                    buffer.append((char)x);
                    if(x == 6){
                        sender.ackReceived();
                    }
                    log.debug("String:" + buffer.toString());
                    log.debug("Value:" + Arrays.toString(buffer.toString().getBytes()));
                } catch (IOException e) {
                    log.error("IOException : ", e);
                }
                break;
            case(SerialPortEvent.CTS):
                log.debug("CTS Received");
                break;
            default:
                log.debug("Unhandled Event Received");
        }
    }
}
