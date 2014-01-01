package in.orangecounty.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.concurrent.*;


/**
 * User: Nagendra
 * Date: 11/12/13
 * Time: 12:58 PM
 * <p/>
 * Modified by: thomas
 * Modified on: 25/11/13
 * Modified at: 7:17 PM
 */
public class SenderImpl {
    final Logger log = LoggerFactory.getLogger(SenderImpl.class);
    private static final String INIT = "\u0031\u0021\u0005";
    private final byte[] ACK = new byte[]{4};
    private final byte[] ENQ = new byte[]{5};
    private final byte[] EOT = new byte[]{6};
    private final byte[] NAK = new byte[]{21};


    private byte[] currentMessage = null;
    private final Object receivingLock = new Object();

    private OutputStream os;

    private static final long TIMER_1_TIME_INTERVAL = 1000l;
    private static final int MAX_INIT_ATTEMPTS = 16;
    private static final int MAX_MSG_ATTEMPTS = 32;

    private static int counter = 0;

    private boolean msgSent = false;
    private boolean selectSequenceSent = false;
    private boolean receiving = false;
    ScheduledFuture future;
    ScheduledExecutorService ex;


    protected SenderImpl(OutputStream op) {
        this.os = op;
    }

    protected void stop() {
        stopScheduler();
    }

    //Private Methods

    private void initCommunication() {
        log.debug("Writing Selecting Sequence");
        selectSequenceSent = true;
        msgSent = false;
        startScheduler(INIT.getBytes(), MAX_INIT_ATTEMPTS);
    }

    private void write(byte[] payload) {
        log.debug("Writing to Output : " + new String(payload) + " : " + Arrays.toString(payload));
        try {
            os.write(payload);
            os.flush();
        } catch (IOException e) {
            log.error("IOException :", e);
        }
    }

    private void write(byte payload) {
        byte[] a = new byte[1];
        a[0] = payload;
        write(a);
    }

    //[Console Input] DEBUG in.orangecounty.impl.SenderImpl - Writing to Output :

    protected void ackReceived() {
        log.debug("Ack Received Called");
        if (selectSequenceSent) {
            stopScheduler();
            startScheduler(currentMessage, MAX_MSG_ATTEMPTS);
            selectSequenceSent = false;
            msgSent = true;
        } else if (msgSent) {
            stopScheduler();
            write(EOT);
            currentMessage = null;
            selectSequenceSent = false;
            msgSent = false;
        } else {
            log.error("Received Extra Ack We should not be here but writing EOT anyway");
            write(EOT);
        }
        //To change body of implemented methods use File | Settings | File Templates.
    }

    private void stopScheduler() {
        if (future != null) {
            future.cancel(true);
        }
        if (ex != null) {
            ex.shutdown();
        }
        counter = 0;
    }

    protected void sendACK() {
        log.debug("Writing ACK");
        write(ACK);
    }

    protected void sendNAK() {
        log.debug("Writing NAK");
        write(NAK);
    }

    protected void sendEOT() {
        log.debug("Writing EOT");
        write(EOT);
    }

    protected void setReceiving(boolean receiving) {
        synchronized (receivingLock) {
            this.receiving = receiving;
        }
    }

    protected void nakReceived() {
        log.debug("NAK Received ......");
        //TODO implement what has to be done when receiving a NAK
    }

    protected boolean isSending() {
        boolean rv = msgSent || selectSequenceSent;
        log.debug("Is Sending:" + rv);
        return rv;
    }

    protected void interrupt() {
        log.debug("Interrupt Received");
        sendEOT();
        receiving = false;
        selectSequenceSent = false;
        msgSent = false;
    }

    protected void sendEnq() {
        write(ENQ);
    }


    private void startScheduler(final byte[] payload, final int tries) {
        ex = Executors.newSingleThreadScheduledExecutor();
        future = ex.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (counter < tries) {
                    write(payload);
                    counter += 1;
                } else {
                    log.debug("Msg Write attempts reached maximum number;");
                    write(EOT);
                    currentMessage = null;
                    selectSequenceSent = false;
                    msgSent = false;
                    stopScheduler();
                }
            }
        }, 0l, TIMER_1_TIME_INTERVAL, TimeUnit.MILLISECONDS);
    }

    protected boolean sendMessage(byte[] payload) {
        synchronized (receivingLock) {
            if (!receiving && !isSending()) {
                currentMessage = payload;
                initCommunication();
                return true;
            } else {
                return false;
            }
        }
    }

    protected boolean canSend() {
        return !isSending() && !receiving;
    }

}

