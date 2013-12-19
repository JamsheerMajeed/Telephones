package in.orangecounty.exp;

import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;

/**
 * Created by thomas on 19/12/13.
 */
public class SerialListener implements SerialPortEventListener {
    Logger log = LoggerFactory.getLogger(SerialListener.class);
    InputStream inputStream;
    char[] buffer;

    public SerialListener(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    @Override
    public void serialEvent(SerialPortEvent ev) {
        log.debug("Serial Event Received :" + ev);
        switch (ev.getEventType()){
            case(SerialPortEvent.DATA_AVAILABLE):
                try {
                    ArrayUtils.add(buffer, (char)inputStream.read());
                    log.debug("String:" + new String(buffer));
                    log.debug("Value:" + Arrays.toString(buffer));
                } catch (IOException e) {
                    e.printStackTrace();
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
