package in.orangecounty.impl;

import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import in.orangecounty.ListenerSenderInterface;
import in.orangecounty.Util;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.Arrays;
import java.util.concurrent.*;

import static gnu.io.SerialPortEvent.DATA_AVAILABLE;
import static in.orangecounty.impl.Constants.*;

/**
 * User: Nagendra
 * Date: 11/12/13
 * Time: 12:57 PM
 * <p/>
 * <p/>
 * Modified by: thomas
 * Modified on: 29/11/13
 * Modified at: 11:53 AM
 */
public class ListenerSerialEventImpl implements SerialPortEventListener {
    Logger log = LoggerFactory.getLogger(ListenerSerialEventImpl.class);
    private InputStream in;
    private ListenerSenderInterface sender;
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture scheduledFuture;
    private byte[] message;

    /**
     * Initialize.
     */
    public ListenerSerialEventImpl(InputStream in, ListenerSenderInterface sender) {
        this.in = in;
        this.sender = sender;
        scheduler = Executors.newScheduledThreadPool(1);
    }

    private void validateMessage(byte[] input) {
        if (input != null &&
                (input[0] != ACK ||
                        input[0] != NAK ||
                        input[0] != ENQ ||
                        input[0] != EOT ||
                        input[0] != DLE ||
                        input[0] != STX ||
                        input[0] != SA
                )
                ) {
            message = null;

        } else {
            interpretMessage(input);
        }
    }


    private void interpretMessage(byte[] input) {
        log.debug("Interpreting Message" + Arrays.toString(input));
        if (input[0] == ACK) {
            log.debug("ACK Received");
            sender.ackReceived();
            message = null;
        } else if (input[0] == NAK) {
            log.debug("NAK Received");
            sender.nakReceived();
            message = null;
        } else if (input[0] == ENQ) {
            sender.sendEOT();
            log.error("ENQ Received.  We are not supposed to receive ENQ");
            message = null;
        } else if (input[0] == EOT) {
            log.debug("EOT Received");
            stopTimer();
            message = null;
        } else if (Arrays.equals(Arrays.copyOfRange(input, 0, 2), DLE_SEND)) {
            log.debug("Del Send Received");
            sender.interrupt();
            sender.sendEOT();
            message = null;
        } else if (Arrays.equals(Arrays.copyOfRange(input, 0, 2), DLE_STOP)) {
            log.debug("Select Sequence Received");
            sender.interrupt();
            sender.sendEOT();
            message = null;
        }
        if (Arrays.equals(Arrays.copyOfRange(input, 0, 3), SELECTING_SEQUENCE)) {
            log.debug("Select Sequence Received");
            if (sender.isSending()) {
                log.debug("Select Sequence Conflict Branch");
                sender.resendSelectSequence();
                message = null;
            } else {
                log.debug("Select Sequence Regular Branch");
                sender.sendACK();
                startTimer();
                message = null;
            }
        } else {
            log.debug("Message Received" + Arrays.toString(input));
            for (int x = 0; x < input.length - 1; x++) {
                if (input[x] == ETX) {
                    byte[] msg = Arrays.copyOfRange(input, 1, x + 1);
                    byte bcc = input[x + 1];
                    if (Util.checkBCC(msg, bcc)) {
                        sender.sendACK();
                        processMessage(msg);
                        stopTimer();
                        message = null;
                    } else {
                        log.debug("BCC Check Failed");
                        sender.sendNAK();
                        stopTimer();
                    }
                    message = null;
                    break;
                }
            }
        }
    }

    public void startTimer() {
        sender.setReceiving(true);
        scheduledFuture = scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                log.debug("Timeout Occurred");
                stopTimer();
            }
        }, 32, TimeUnit.SECONDS);
    }

    private void stopTimer() {
        sender.setReceiving(false);
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
    }


    private void processMessage(byte[] message) {
        //TODO do something meaningful with the message;
        log.debug(new String(message));
    }

    @Override
    public void serialEvent(SerialPortEvent serialPortEvent) {
        try {
            message = ArrayUtils.add(message, (byte) in.read());
        } catch (IOException e) {
            log.debug("IOException : ", e);
        }
        log.debug("Got Message " + Arrays.toString(message));
        validateMessage(message);
    }
}
