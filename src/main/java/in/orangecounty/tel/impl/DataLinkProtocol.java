package in.orangecounty.tel.impl;

import in.orangecounty.tel.SerialListener;
import in.orangecounty.tel.SerialSender;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * The DataLinkProtocol uses the SerailImpl as the physical layer to send and receive Messages.  
 * The DataLinkProtocol is the 2nd layer.  It takes care of error correction using CRC and sends the necessary
 * Positive Acknowledgement (ACK) or Negative Acknowledgement(NAK)
 *
 * Created by jamsheer on 3/6/15.
 */
public class DataLinkProtocol implements SerialListener {
    private static final Logger log = LoggerFactory.getLogger(DataLinkProtocol.class);
    private static final String INIT = "\u0031\u0021\u0005";
    private static final byte ACK = 6;
    private static final byte STX = 2;
    private static final byte ENQ = 5;
    private static final byte NAK = 21;
    private static final byte EOT = 4;
    private static final byte DLE = 16;
    private static final byte ETX = 3;
    private static int COUNTER = 0;
    private int phase = 1;
    private String messageToSend = null;
    ScheduledExecutorService initScheduler = Executors.newSingleThreadScheduledExecutor();
    ScheduledExecutorService messageScheduler = Executors.newSingleThreadScheduledExecutor();
    ScheduledFuture initFuture;
    ScheduledFuture messageFuture;
    private int messageCounter = 0;


    SerialSender serialSender;
    private boolean receiving = false;


    public void setSerialSender(SerialSender serialSender) {
        this.serialSender = serialSender;
    }

    public void sendMessage(String message) throws RuntimeException{
        if(phase != 1 & receiving == true){
            throw new RuntimeException("Cannot Send Message Now");
        }else {
            this.messageToSend = message;
            sendInit();
        }
    }

    @Override
    public void onMessage(final byte[] message) {
        log.debug("Received message {} converted it to {}", message, new String(message));
        log.debug("Checking message {} with {}", message, INIT.getBytes());
        if(Arrays.equals(message, INIT.getBytes())){
            sendACK();
            /* Start Timer 2-1 (32 Seconds) */
        } else if ((message[0] == STX) && (message[message.length-2]== ETX)){
            log.debug("Received a  message ");
            /*Check BCC and Send ACK/NAK and change the phase to 0
            getBCC(message)*/
            byte bcc = getBCC(Arrays.copyOfRange(message, 1, message.length - 1));
            log.debug("Message BCC {} | Calculated BCC {}", message[message.length - 1], bcc);
            if(message[message.length-1] == bcc){
                /*//Stop Timer 2-1 (32 Seconds)
                sendACK();
                //Start Timer 2-2 (32 Seconds)*/
                sendACK();
            }
            else {
                sendNAK();
            }
        } else if(Arrays.equals(message, new byte[]{EOT})){
            /*//stop Timer 2-2 (32 Seconds)*/
        }else if(Arrays.equals(message,new byte[]{ACK})){
            if(phase == 1){
                /*//Stop Timer 1-1*/
                initFuture.cancel(true);
                sendMessageHeader(messageToSend);
                phase=2;
                /*//Close Future, Schedule Message change Phase = 2*/
            } else if (phase == 2){
                /*//Close Future, Send EOT change Phase = 0*/
                messageFuture.cancel(true);
                sendEOT();
            } else {
                log.error("Received {} in phase {}", message, phase);

            }
        } else if(Arrays.equals(message,new byte[]{NAK})){
            if(phase==2){
                if(messageCounter<4){
                    messageFuture.cancel(true);
                    sendMessageHeader("Hello");
                } else {
                    messageFuture.cancel(true);
                    sendEOT();
                }
            }
        } else if(Arrays.equals(message,new byte[]{DLE, '<'})){
            /*//Stop Timer 1-1*/
            initFuture.cancel(true);
            sendEOT();
        } else {
            log.debug("Received : {}", new String(message));
        }
    }

    private void sendInit(){
        initFuture = initScheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                /* Send init */

                try {
                    serialSender.sendMessage(INIT.getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        },0l,1l, TimeUnit.SECONDS);
    }

    private void sendMessageHeader(final String message){
        byte[] msg = ArrayUtils.add(message.getBytes(), ETX);
                /* Sent message */
        byte bcc = getBCC(msg);
        msg = ArrayUtils.add(msg, bcc);
        msg = ArrayUtils.add(msg, 0, STX);
        try {
            serialSender.sendMessage(msg);
        } catch (IOException e) {
            e.printStackTrace();
        }
        messageFuture = messageScheduler.scheduleAtFixedRate(new Runnable() {
            int currentCount = 0;
            @Override
            public void run() {
                if(currentCount< 32){
                    sendENQ();
                } else {
                    messageFuture.cancel(true);
                    sendEOT();
                    phase = 1;
                }
            }
        },1l,1l,TimeUnit.SECONDS);
    }

    private void sendNAK() {
        try {
            serialSender.sendMessage(new byte[]{NAK});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendACK() {
        try {
            serialSender.sendMessage(new byte[]{ACK});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendEOT() {
        phase = 1;
        messageCounter = 0;
        try {
            serialSender.sendMessage(new byte[]{EOT});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void sendENQ() {
        try {
            serialSender.sendMessage(new byte[]{ENQ});
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private byte getBCC(byte[] msg) {
        byte lrc = 0;
        for (int x = 0; x < msg.length; x++) {
            lrc = (byte) (lrc ^ msg[x]);
        }
        return lrc;
    }
}
