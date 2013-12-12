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
public class ListenerSerialEventImpl implements SerialPortEventListener {
    Logger log = LoggerFactory.getLogger(ListenerSerialEventImpl.class);
    private InputStream in;
    private ListenerSenderInterface sender;
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture scheduledFuture;

    /**
     * Initialize.
     */
    public ListenerSerialEventImpl(InputStream in, ListenerSenderInterface sender) {
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
        log.debug("Serial Event" + serialPortEvent);
        switch (serialPortEvent.getEventType()) {
            case DATA_AVAILABLE:
                log.debug("ReadBuffer Called");
                byte[] msg = readSerial();
                log.debug("Got Message " + Arrays.toString(msg));
                interpretMessage(msg);
                log.debug("Process Message Called");
                break;
        }
    }

    /**
     * Buffer to hold the reading
     */


    private byte[] readSerial() {
        log.debug("In Read Serial");
        byte[] readBuffer = new byte[BUFFER_SIZE];
        int index = 0;
        while(true){
            log.debug("In While Loop");
            try {
                log.debug("Trying to Read from input Stream");
                readBuffer[index] = (byte) in.read();
                log.debug("Read" + readBuffer[index]);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (readBuffer[0] == ACK || readBuffer[0] == NAK || readBuffer[0] == ENQ || readBuffer[0] == EOT) {
                break;
            } else if (Arrays.equals(Arrays.copyOfRange(readBuffer, 0, 2), DLE_SEND)) {
                break;
            } else if (Arrays.equals(Arrays.copyOfRange(readBuffer, 0, 2), DLE_STOP)) {
                break;
            } else if (Arrays.equals(Arrays.copyOfRange(readBuffer, 0, 3), SELECTING_SEQUENCE)) {
                break;
            } else if(readBuffer[index-1] == ETX){
                break;
            }
            index++;
        }
        byte[] rv =Arrays.copyOf(readBuffer, index+1);
        log.debug("Returning from readSerial" + Arrays.toString(rv));
        return rv;
    }
}
