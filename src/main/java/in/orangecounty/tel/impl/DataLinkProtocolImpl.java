package in.orangecounty.tel.impl;

import in.orangecounty.tel.SerialListener;
import in.orangecounty.tel.service.PMSRestClient;
import in.orangecounty.tel.service.impl.PMSRestClientImpl;
import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.*;
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
public class DataLinkProtocolImpl implements SerialListener{

    private static final Logger log = LoggerFactory.getLogger(DataLinkProtocolImpl.class);
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
    ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(3);

    ScheduledFuture initFuture;
    ScheduledFuture messageFuture;
    ScheduledFuture statusFuture;
    private int messageCounter = 0;
    private NEAX7400PmsProtocolImpl neax7400PmsProtocolImpl;
    private PMSRestClient pmsRestClient;



    SerialImpl serialSender;
    private boolean receiving = false;

    public DataLinkProtocolImpl() {
        serialSender = new SerialImpl();
        serialSender.setSerialListener(this);
    }

    public void setSerialSender(SerialImpl serialSender) {
        this.serialSender = serialSender;
    }

    public void sendMessage(String message) throws RuntimeException{
        log.debug("Send Message called with {}", message);
        if(phase != 1 & receiving == true){
            throw new RuntimeException("Cannot Send Message Now");
        }else {
            this.messageToSend = message;
            sendInit();
        }
        log.debug("Send Message Over");
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
            byte[] payload = Arrays.copyOfRange(message, 1, message.length - 1);
            byte bcc = getBCC(payload);
            log.debug("Message BCC {} | Calculated BCC {}", message[message.length - 1], bcc);
            if(message[message.length-1] == bcc){
                log.info("Receive Message : {}", new String(payload));
                log.info("---- Received station message "+Arrays.copyOfRange(message,1,9)+" to string"+Arrays.copyOfRange(message,1,9).toString());

                if((Arrays.equals(Arrays.copyOfRange(message,1,9),new byte[]{'1','!','L','1','4','5','0','2'}))){
                    System.out.print(" equals");
                    neax7400PmsProtocolImpl = new NEAX7400PmsProtocolImpl();
                    pmsRestClient = new PMSRestClientImpl();
                    pmsRestClient.updateCallCharges(parseCallDetails(new String(Arrays.copyOfRange(message, 9, 48))));


                /*//Stop Timer 2-1 (32 Seconds)
                sendACK();
                //Start Timer 2-2 (32 Seconds)*/
                    sendACK();
                }
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
//                    sendMessageHeader("Hello");
                } else {
                    messageFuture.cancel(true);
                    sendEOT();
                }
            }
        } else if(Arrays.equals(message,new byte[]{DLE, '<'})){
            /*//Stop Timer 1-1*/
            initFuture.cancel(true);
            sendEOT();
//        } else if((message[0] == STX) &&(Arrays.equals(Arrays.copyOfRange(message,1,9),new byte[]{'1','!','L','1','4','5','0','2'}))){
//
//                log.debug("---- Received station message ");
//                neax7400PmsProtocol = new NEAX7400PmsProtocol();
//                neax7400PmsProtocol.parseCallDetails(Arrays.copyOfRange(message,9,48).toString());
        } else {
            log.debug("Received : {}", new String(message));
        }
    }

    private void sendInit(){
        if(initFuture!=null && !initFuture.isCancelled()){
            return;
        } else {
            initFuture = scheduler.scheduleAtFixedRate(new Runnable() {
                private int counter = 0;
                @Override
                public void run() {
                    if(counter < 16){
                /* Send init */
                        try {
                            log.info("inside sendInit");
                            serialSender.sendMessage(INIT.getBytes());
                            counter++;
                        } catch (Exception e) {
                            log.debug("IO Exception", e);
                        }
                    } else {
                        sendEOT();
                        initFuture.cancel(true);
                    }
                }
            },0l,1l, TimeUnit.SECONDS);
        }
    }


    public void sendStatus(){
        statusFuture = scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                log.info("-- phase --"+phase);
                log.info("-- receiving -- "+receiving);
                if(receiving == false){
                    try{
                        sendMessage("1!L7007F  ");
                        log.info("Sending Status");
                    } catch (RuntimeException e){
                        log.info("Runtime Exception", e);
                    }
                }
                 else {
                    log.info("Sending Status Interrupted");
                }

            }
        },1l,1l,TimeUnit.MINUTES);
    }

    private void sendMessageHeader(final String message){
        byte[] msg = ArrayUtils.add(message.getBytes(), ETX);
                /* Sent message */
        byte bcc = getBCC(msg);
        msg = ArrayUtils.add(msg, bcc);
        msg = ArrayUtils.add(msg, 0, STX);
        try {
            serialSender.sendMessage(msg);
        } catch (Exception e) {
            log.debug("Exception on Send Message", e);
        }
        messageFuture = scheduler.scheduleAtFixedRate(new Runnable() {
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
        } catch (Exception e) {
            log.debug("Exception on Send Message", e);
        }
    }

    private void sendACK() {
        try {
            serialSender.sendMessage(new byte[]{ACK});
        } catch (Exception e) {
            log.debug("Exception on Send Message", e);
        }
    }

    private void sendEOT() {
        phase = 1;
        messageCounter = 0;
        try {
            serialSender.sendMessage(new byte[]{EOT});
        } catch (Exception e) {
            log.debug("Exception on Send Message", e);
        }
    }

    private void sendENQ() {
        try {
            serialSender.sendMessage(new byte[]{ENQ});
        } catch (Exception e) {
            log.debug("Exception on Send Message", e);
        }
    }


    private byte getBCC(byte[] msg) {
        byte lrc = 0;
        for (int x = 0; x < msg.length; x++) {
            lrc = (byte) (lrc ^ msg[x]);
        }
        return lrc;
    }

    public void start() {
        try {

            serialSender.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        serialSender.stop();
    }

    public void listPorts() {
        serialSender.listPorts();
    }



    public Map<String,Map<String,String>> parseCallDetails(String message) {
        Calendar cal = Calendar.getInstance();
        Map<String,Map<String,String>> map = new HashMap<String, Map<String, String>>();
        Map<String,String> callDetailsMap = new HashMap<String, String>();
        String startTime="";
        cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(message.substring(28,30)));
        cal.set(Calendar.MINUTE,Integer.parseInt(message.substring(30,32)));
        cal.set(Calendar.SECOND,Integer.parseInt(message.substring(32,34)));
        cal.set(Calendar.MILLISECOND,0);

        String stationNumber,routeNumber,trunkNumber,subscriberNumber,hour,minute,second,duration;
        stationNumber = message.substring(0,4);
        routeNumber = message.substring(6,9);
        trunkNumber = message.substring(9,12);
        subscriberNumber = message.substring(12,28);
        hour = message.substring(28,30);
        minute = message.substring(30,32);
        second = message.substring(32,34);
        duration = message.substring(34,39);

        startTime = message.substring(28,30)+":"+message.substring(30,32)+":"+message.substring(32,34);

        if(Calendar.getInstance().getTime().before(cal.getTime())){
            cal.add(Calendar.DATE,-1);
        }



        callDetailsMap.put("CALLED_NO",subscriberNumber);

        callDetailsMap.put("START_TIME",startTime);

        callDetailsMap.put("CALL_DURATION",duration);
        callDetailsMap.put("DATE_OF_CALL",cal.getTime().toString()+" "+startTime);

        map.put(stationNumber,callDetailsMap);

//        System.out.println("Station Number"+stationNumber);
//        System.out.println("Route Number"+routeNumber);
//        System.out.println("Trunk Number"+trunkNumber);
//        System.out.println("Subscriber Number"+subscriberNumber);
//        System.out.println("Hour"+hour);
//        System.out.println("Minute"+minute);
//        System.out.println("Second"+second);
//        System.out.println("Duration"+duration);
//        System.out.println("Start Date "+cal.getTime());
//
//        System.out.println("Modified date "+cal.getTime());
//        System.out.println("Current date "+new Date());
//        System.out.println("Compare "+cal.getTime().equals(new Date()));


        return map;


    }

}
