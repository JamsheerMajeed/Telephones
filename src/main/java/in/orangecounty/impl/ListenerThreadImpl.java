package in.orangecounty.impl;

import in.orangecounty.ListenerSenderInterface;
import in.orangecounty.Util;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.concurrent.*;

import static in.orangecounty.impl.Constants.*;

/**
 * ListenerThreadImpl
 * Created by thomas on 16/12/13.
 */
public class ListenerThreadImpl {
    Logger log = LoggerFactory.getLogger(ListenerSerialEventImpl.class);
    private InputStream in;
    private ListenerSenderInterface sender;
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture scheduledFuture;
    private ScheduledFuture timerScheduledFuture;
    ExecutorService ex;
    Future<byte[]> result;
    byte[] input = null;

    /**
     * Initialize.
     */
    public ListenerThreadImpl(InputStream in, ListenerSenderInterface sender) {
        this.in = in;
        this.sender = sender;
        scheduler = Executors.newScheduledThreadPool(2);
        ex = Executors.newSingleThreadExecutor();
        result = ex.submit(new SerialInputReadTask());
        scheduledFuture = scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                input = ArrayUtils.addAll(input, readInput());
                interpretMessage(input);
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
    }

    private void interpretMessage(byte[] input) {
        if(input==null){
//            log.debug("Null Input Received");
            return;
        }
        log.debug("Interpreting Message" + Arrays.toString(input));
        if (input.length > 0 && input[0] == ACK) {
            log.debug("ACK Received");
            sender.ackReceived();
        } else if (input.length > 0 && input[0] == NAK) {
            log.debug("NAK Received");
            sender.nakReceived();
        } else if (input.length > 0 && input[0] == ENQ) {
            sender.sendEOT();
            log.error("ENQ Received.  We are not supposed to receive ENQ");
        } else if (input.length > 0 && input[0] == EOT) {
            log.debug("EOT Received");
            stopTimer();
        } else if (input.length > 2 && Arrays.equals(Arrays.copyOfRange(input, 0, 2), DLE_SEND)) {
            log.debug("Del Send Received");
            sender.interrupt();
            sender.sendEOT();
        } else if (input.length > 2 && Arrays.equals(Arrays.copyOfRange(input, 0, 2), DLE_STOP)) {
            log.debug("Select Sequence Received");
            sender.interrupt();
            sender.sendEOT();
        }
        if (input.length > 3 && Arrays.equals(Arrays.copyOfRange(input, 0, 3), SELECTING_SEQUENCE)) {
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
            for (int x = 0; x < input.length; x++) {
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

    private void processMessage(byte[] message){
        log.debug(" Message Received : " + Arrays.toString(message));
    }

    private void startTimer() {
        sender.setReceiving(true);
        timerScheduledFuture = scheduler.schedule(new Runnable() {
            @Override
            public void run() {
                log.debug("Timeout Occurred");
                stopTimer();
            }
        }, 32, TimeUnit.SECONDS);
    }

    private void stopTimer() {
        sender.setReceiving(false);
        if (timerScheduledFuture != null) {
            timerScheduledFuture.cancel(true);
        }
    }

    private byte[] readInput() {
        byte[] input = null;
        try {
            input = result.get(100, TimeUnit.MILLISECONDS);
        } catch (ExecutionException e) {
            log.debug("ExecutionException : ", e);
            result.cancel(true);
        } catch (TimeoutException e) {
//            log.debug("Cancelling reading task");
            result.cancel(true);
//            log.debug("Thread cancelled. input is null");
        } catch (InterruptedException e) {
            log.debug("InterruptedException", e);
            result.cancel(true);
        } finally {
            ex.shutdownNow();
        }
        return input;
    }

    public void stop() {
        if(timerScheduledFuture!=null){
            timerScheduledFuture.cancel(true);
        }
        if(scheduledFuture!=null){
            scheduledFuture.cancel(true);
        }
        if(result!=null){
            result.cancel(true);
        }
        if(ex!=null){
            ex.shutdown();
        }
        if(scheduler!=null){
            scheduler.shutdown();
        }
    }


    public class SerialInputReadTask implements Callable<byte[]> {
        public byte[] call() throws IOException {
            byte[] rv = new byte[Constants.BUFFER_SIZE];
            int length = in.read(rv);
            return Arrays.copyOf(rv, length);
        }
    }
}
