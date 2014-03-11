package in.orangecounty.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;


/**
 * Telephone Command Implementation
 * User: thomas
 * Date: 3/12/13
 * Time: 9:05 AM
 */
public class TelephoneCommandImpl {
    Logger log = LoggerFactory.getLogger(TelephoneCommandImpl.class);
    private final String MSG_70_F_CRC = "\u00021!L7007F  \u0003\u0019";
    private final String MSG_70_F = "\u00021!L7007F  \u0003";
    private final String MSG_70_3 = "\u00021!L70073  \u0003";
    private final String MSG_70_4 = "\u00021!L70074  \u0003";
    /** Checkout Message Template requires 1 argument Extension Number */
    private final String MSG_16_2= "\u00021!L16112%-4s  \u0003";
    /** Checkin Message Template requires 2 arguments Extension Number and Guest Name */
    private final String MSG_16_B = "\u00021!L1634B%-4s        21%-15s\u0003";


    private SenderImpl sender;
    private final byte[] msg70FCrc = MSG_70_F_CRC.getBytes();
    private Queue<byte[]> messages = new ConcurrentLinkedQueue<byte[]>();
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture messageScheduledFuture;
    private ScheduledFuture statusEnquiryScheduledFuture;


    protected TelephoneCommandImpl(SenderImpl sender) {

        this.sender = sender;

        scheduler = Executors.newScheduledThreadPool(2);
        messageScheduledFuture = scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                sendMessages();
            }
        }, 0, 1, TimeUnit.SECONDS);

        statusEnquiryScheduledFuture = scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                log.debug("Status Enquiry Scheduler");
                if (!messages.contains(msg70FCrc)) {
                    messages.add(msg70FCrc);
                }
            }
        }, 0, 55, TimeUnit.SECONDS);
    }

    private void sendMessages() {
        int count = 0;
        while (!messages.isEmpty()) {
            if (sender.sendMessage(messages.peek())) {
                log.debug("Poll Called");
                messages.poll();
            } else {
                if (count > 10) {
                    break;
                }
                count++;
            }
        }
    }

    protected void checkIn(String extensionNumber, String guestName) {
        log.debug("checkIn Called");
        String msg = String.format(MSG_16_B, truncate(extensionNumber, 4), truncate(guestName, 15));
        queueMessage(msg);
    }

    private void queueMessage(String message) {
        log.debug("Queue Messages Called");
        message = message + (char) lrc(message);
        messages.add(message.getBytes());
        log.debug("Messages Size : " + messages.size());
    }

    protected void checkOut(String extensionNumber) {
        String msg1 = paresCheckOut(extensionNumber);
        String msg2 = String.format(MSG_16_2, extensionNumber);
        log.debug(msg1);
        log.debug(msg2);
        queueMessage(msg1);
    }

    protected String truncate(String string, int length){
        String rv;
        if(string.length() < length){
            rv = string;
        } else {
            rv = string.substring(0, length);
        }
        return rv;
    }

    protected void sync(Map<String, String> extensions) {
        //Queue 70.3 Message
        queueMessage(MSG_70_3);
        // For Each Extension queue 17.B Message
        for (Map.Entry<String, String> entry : extensions.entrySet()){
            queueMessage(String.format("%s%s", entry.getKey(), entry.getValue()));
        }
        //Queue 70.4 Message
        queueMessage(MSG_70_4);
    }

    public String parseRoomImage(String extension, String name) {
        String rv = null;
        if (name != null) {

        } else {

        }
        return rv;
    }


    protected void stop() {
        if (messageScheduledFuture != null) {
            messageScheduledFuture.cancel(true);
        }
        if (statusEnquiryScheduledFuture != null) {
            statusEnquiryScheduledFuture.cancel(true);
        }
        scheduler.shutdown();
    }

    public String parseActivateExtension(String extensionNumber, String guestName) {
        guestName = reSizeGuestName(guestName);
        extensionNumber = reSizeExtensionNumber(extensionNumber);
        return "\u00021!L1634B" + extensionNumber + "        21" + guestName + "\u0003";
    }

    public String paresCheckOut(String extensionNumber) {
        extensionNumber = reSizeExtensionNumber(extensionNumber);
        return "\u00021!L16112" + extensionNumber + "  \u0003";
    }


    private String reSizeExtensionNumber(String extensionNumber) {
        if (extensionNumber.length() < 4) {
            int count = 4 - extensionNumber.length();
            for (int j = 0; j < count; j++) {
                extensionNumber = extensionNumber.concat(" ");
            }
        }
        return extensionNumber;
    }

    private String reSizeGuestName(String guestName) {
        if (guestName.length() < 15) {
            int count = 15 - guestName.length();
            for (int i = 0; i < count; i++) {
                guestName = guestName.concat(" ");
            }
        } else {
            guestName = guestName.substring(0, 15);
        }
        return guestName;
    }

    private byte lrc(final byte[] msg) {
        byte lrc = 0;
        for (int x = 1; x < msg.length; x++) {
            lrc = (byte) (lrc ^ msg[x]);
        }
        return lrc;

    }


    private byte lrc(final String msg) {
        return lrc(msg.getBytes());
    }

}
