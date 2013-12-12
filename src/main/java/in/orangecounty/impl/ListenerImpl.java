package in.orangecounty.impl;

import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import in.orangecounty.ListenerSenderInterface;
import in.orangecounty.Util;
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
public class ListenerImpl implements SerialPortEventListener {
    Logger log = LoggerFactory.getLogger(ListenerImpl.class);
    private InputStream in;
    private ListenerSenderInterface sender;
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture scheduledFuture;

    /**
     * Initialize.
     */
    public ListenerImpl(InputStream in, ListenerSenderInterface sender) {
        this.in = in;
        this.sender = sender;
        scheduler = Executors.newScheduledThreadPool(1);
    }


    private void interpretMessage(byte[] input) {
        log.debug("Interpreting Message" + Arrays.toString(input));
        if (input[0] == ACK) {
            log.debug("ACK Received");
            sender.ackReceived();
        } else if (input[0] == NAK) {
            log.debug("NAK Received");
            sender.nakReceived();
        } else if (input[0] == ENQ) {
            sender.sendEOT();
            log.error("ENQ Received.  We are not supposed to receive ENQ");
        } else if (input[0] == EOT) {
            log.debug("EOT Received");
            stopTimer();
        } else if (Arrays.equals(Arrays.copyOfRange(input, 0, 2), DLE_SEND)) {
            log.debug("Del Send Received");
            sender.interrupt();
            sender.sendEOT();
        } else if (Arrays.equals(Arrays.copyOfRange(input, 0, 2), DLE_STOP)) {
            log.debug("Select Sequence Received");
            sender.interrupt();
            sender.sendEOT();
        }
        if (Arrays.equals(Arrays.copyOfRange(input, 0, 3), SELECTING_SEQUENCE)) {
            log.debug("Select Sequence Received");
            if (sender.isSending()) {
                log.debug("Select Sequence Conflict Branch");
                sender.resendSelectSequence();
            } else {
                log.debug("Select Sequence Regular Branch");
                sender.sendACK();
                startTimer();
            }
        } else {
            log.debug("Message Received" + Arrays.toString(input));
            for (int x = 0; x < BUFFER_SIZE; x++) {
                if (input[x] == ETX) {
                    byte[] msg = Arrays.copyOfRange(input, 1, x + 1);
                    byte bcc = input[x + 1];
                    if (Util.checkBCC(msg, bcc)) {
                        sender.sendACK();
                        processMessage(msg);
                        stopTimer();
                    } else {
                        log.debug("BCC Check Failed");
                        sender.sendNAK();
                        stopTimer();
                    }
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
        switch (serialPortEvent.getEventType()) {
            case DATA_AVAILABLE:
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    log.error("InterruptedException : ", e);
                }
                readSerial();
                break;
        }
    }

    /**
     * Buffer to hold the reading
     */
    private byte[] readBuffer = new byte[BUFFER_SIZE];

    private void readSerial() {
        try {
            int availableBytes = in.available();
            if (availableBytes > 0) {
                // Read the serial port
                in.read(readBuffer, 0, availableBytes);
                interpretMessage(readBuffer);
            }
        } catch (IOException e) {
            log.debug("IOException : ", e);
        }
    }
}
