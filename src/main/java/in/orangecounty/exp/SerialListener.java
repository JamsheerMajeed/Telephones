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

/**
 * Created by thomas on 19/12/13.
 */
public class SerialListener implements SerialPortEventListener {
    Logger log = LoggerFactory.getLogger(SerialListener.class);
    /**
     * Start of Transmission.
     */
    private static final int STX = 2;
    /**
     * End of Transmission.
     */
    private static final int ETX = 3;
    /**
     * PositiveAcknowledge.
     */
    private static final int ACK = 6;
    /**
     * Negative Acknowlegement.
     */
    private static final int NAK = 21;
    /**
     * Enquiry.
     */
    private static final int ENQ = 5;
    /**
     * End of Transmission.
     */
    private static final int EOT = 4;
    /**
     * Permission to Send Char.
     */
    private static final int PSC = 60;
    /**
     * Stop Transmission Char.
     */
    private static final int STC = 124;
    /**
     * Initialize.
     */



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
                    process(inputStream.read());
                } catch (IOException e) {
                    log.error("IOException : ", e);
                }
                break;
            default:
                log.debug("Unhandled Event Received");
        }
    }

    private void process(int b){
        log.debug("Received int message " + b);
        buffer.append((char) b);
        if (buffer.length() > 0 && buffer.charAt(buffer.length() - 2) == ETX) {
            log.warn("message with BCC" + buffer.toString());
            log.warn("check bcc:" + checkLrc(buffer.toString()));
            log.warn("check count:" + checkCount(buffer.toString()));
            if (checkLrc(buffer.toString()) && checkCount(buffer.toString())) {
                sender.sendACK();
                processMessage(buffer.toString());
            } else {
                sender.sendNAK();
            }
            resetBuffer();
            return;
        }
        switch (b) {
            case (ACK):
                log.debug("ACK Case");
                sender.ackReceived();
                break;
            case (NAK):
                log.warn("NAK Case");
                sender.nakReceived();
                break;
            case (ENQ):
                log.warn("ENQ Case");
                sender.sendACK();
                sender.setReceiving(true);
                resetBuffer();
                break;
            case (EOT):
                log.debug("EOT Case");
                sender.setReceiving(false);
                break;
            case (PSC):
                log.debug("PSC Case");
                if (buffer.toString().equals("16")) {
                    sender.sendEOT();
                }
                break;
            default:
                log.debug("Default Case: Doing nothing");
        }

    }

    private void resetBuffer() {
        buffer = new StringBuilder();
    }

    private boolean checkCount(String msg) {
        return ((msg.length() - 6) == Integer.parseInt(msg.substring(6, 8)));
    }

    private boolean checkLrc(final String msg) {
        byte lrc = 0;
        byte[] byteArray = msg.getBytes();
        for (int x = 1; x < byteArray.length - 1; x++) {
            lrc = (byte) (lrc ^ byteArray[x]);
        }
        return lrc == byteArray[byteArray.length - 1];

    }

    private void processMessage(String s) {
        log.debug("Message Received : " + s);
    }
}
