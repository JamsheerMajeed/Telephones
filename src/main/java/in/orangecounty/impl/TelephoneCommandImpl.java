package in.orangecounty.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 * Telephone Command Implementation
 * User: thomas
 * Date: 3/12/13
 * Time: 9:05 AM
 */
public class TelephoneCommandImpl {
    private Logger log = LoggerFactory.getLogger(TelephoneCommandImpl.class);

    /**
     * Feature Code 21 Function Code 6<br/>
     * This feature provides a convenient way for the PMS to change room status data in the PBX.
     * <br/>
     * Change the guest name data.
     * <br/>
     * Arguments:<br/>
     * 1 - Station Number <br/>
     * 2 - Guest Name
     */
    private static final String MSG_21_6 = "\u00021!L2126%-4s  %-15s\u0003";


    private SenderImpl sender;
    private final byte[] msg70FCrc = "\u00021!L7007F  \u0003\u0019".getBytes();
    private Queue<byte[]> messages = new ConcurrentLinkedQueue<byte[]>();
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture messageScheduledFuture;
    private ScheduledFuture statusEnquiryScheduledFuture;

    protected final void setMessages(Queue<byte[]> messages){
        this.messages = messages;
    }


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

    /**
     * Parse Room Image Message MSG_17_B
     *
     * @param extNum        Station Number : 4 Digit Station Number<br/><br/>
     * @param vacant        Vacant/Occupied<br/>
     *                      Possible Values<br/>
     *                      0 - Vacant<br/>
     *                      1 - Occupied<br/><br/>
     * @param mwLamp        MW lamp status<br/>
     *                      Possible Values<br/>
     *                      0 - Off<br/>
     *                      1 - On.<br/><br/>
     * @param restriction   Restriction : Single digit ie: 0, 1, 5, 6<br/>
     *                      Possible Values<br/>
     *                      0 - No Restriction<br/>
     *                      1 - Outward restriction: denies all local and toll calling from the room tele-phone (Room Cut-off).<br/>
     *                      5 - Termination restriction: denies all incoming calls to the room telephone (Do Not Disturb).<br/>
     *                      6 - Both outgoing and incoming restriction.<br/><br/>
     * @param roomOccupancy Room occupancy<br/>
     *                      Possible Values<br/>
     *                      1 Not Vip and Keys In room<br/>
     *                      2 VIP and Keys In room<br/>
     *                      3 Not VIP and Keys in Front Desk<br/>
     *                      4 VIP and Keys in Front Desk<br/><br/>
     * @param reserved      Reservation<br/>
     *                      Possible Values<br/>
     *                      0 - Not Reserved<br/>
     *                      1 - Reserved<br/><br/>
     * @param language      Language : Single digit ie: 1 - 7<br/>
     *                      Possible Values<br/>
     *                      1 - Japanese<br/>
     *                      2 - English<br/>
     *                      3 - German<br/>
     *                      4 - French<br/>
     *                      5 - Spanish<br/>
     *                      6 - Chinese<br/>
     *                      7 - Russian<br/><br/>
     * @param maidStatus    Maid status<br/>
     *                      This item consists of one (1) digit, ranging in value from 1 to 8, the room status. The exact meanings
     *                      of these codes are determined by the PBX system manager.<br/>
     *                      A sample set of values is listed below:<br/>
     *                      1 - Cleaning is necessary for the specified room.<br/>
     *                      2 - The specified room is cleaned.<br/>
     *                      3 - The specified room is ready for reservation.<br/>
     *                      4 - The specified room is out of service.<br/>
     *                      5 - The specified room needs repair.<br/>
     *                      6 - The specified room is repaired.<br/><br/>
     * @param wakeUpResult  Wake-up result<br/>
     *                      Possible Values<br/>
     *                      1 - Answer<br/>
     *                      2 - Busy<br/>
     *                      3 - No answer<br/>
     *                      4 - Blocked<br/><br/>
     * @param wakeUpTime    Wake-up time<br/>
     *                      This item consists of four (4) digits indicating the wake-up hour and minute as shown below:<br/>
     *                      0 8     0 0<br/>
     *                      Hour    Minute<br/>
     *                      (24-hour form)<br/><br/>
     * @return The parsed String to be added to the queue.<br/>
     */
    protected final String parseRoomImage(int extNum, int vacant, int mwLamp, int restriction, int roomOccupancy,
                                    int reserved, int language, int maidStatus, int wakeUpResult, Date wakeUpTime) {
        if (extNum < 0 || extNum > 9999) {
            throw new IllegalArgumentException("Extension Number not in range [0..9999]: " + extNum);
        }
        if (vacant < 0 || vacant > 1) {
            throw new IllegalArgumentException("Vacant not in range [0..1]: " + vacant);
        }
        if (mwLamp < 0 || mwLamp > 1) {
            throw new IllegalArgumentException("Message Waiting Lamp not in range [0..1]: " + mwLamp);
        }
        if (restriction != 0 && restriction != 1 && restriction != 5 && restriction != 6) {
            throw new IllegalArgumentException("Restriction not in range [0 1 5 6]: " + restriction);
        }
        if (roomOccupancy < 1 || roomOccupancy > 4) {
            throw new IllegalArgumentException("Vacant not in range [1..4]: " + roomOccupancy);
        }
        if (reserved < 0 || reserved > 1) {
            throw new IllegalArgumentException("Reserved Waiting Lamp not in range [0..1]: " + reserved);
        }
        if (language < 1 || language > 7) {
            throw new IllegalArgumentException("Language not in range [1..7]: " + language);
        }
        if (maidStatus < 1 || maidStatus > 8) {
            throw new IllegalArgumentException("Maid Status not in range [1..8]: " + maidStatus);
        }
        if (wakeUpResult < 1 || wakeUpResult > 4) {
            throw new IllegalArgumentException("Wake Up Result not in range [1..4]: " + wakeUpResult);
        }
        if (wakeUpTime == null) {
            wakeUpTime = new Date();
        }
        String rv;
        String MSG_17_B = "\u00021!L1738B%1$-4s  %2$1d%3$1d%4$1d%5$1d%6$1d%7$1d%8$1d%9$1d%10$tH%10$tM000000000000000\u0003";
        rv = String.format(MSG_17_B, extNum, vacant, mwLamp, restriction, roomOccupancy, reserved, language,
                maidStatus, wakeUpResult, wakeUpTime);
        return rv;
    }
    protected final String parseRoomImage(Map<RoomImageKey, Object> roomImageValues) {
        for(Map.Entry<RoomImageKey, Object> element : roomImageValues.entrySet()){
            switch (element.getKey()){
                case EXTENSION_NUMBER:
                    break;
                case IS_RESERVED:
                    break;
                case IS_VACANT:
                    break;
                case LANGUAGE:
                    break;
                case MAID_STATUS:
                    break;
                case MW_LAMP_STATUS:
                    break;
                case RESTRICTION:
                    break;
                case ROOM_OCCUPANCY:
                    break;
                case WAKE_UP_RESULT:
                    break;
                case WAKE_UP_TIME:
                    break;
                default:
                    break;
            }

        }
        return "";
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

    protected final void checkIn(int extNumber, String guestName) {
        log.debug("checkIn Called");
        if (extNumber > 9999) {
            throw new IllegalArgumentException("Extension Number more than 4 digits");
        }
        /** Checkin Message Template requires 2 arguments Extension Number and Guest Name */
        String MSG_16_B = "\u00021!L1634B%-4s        21%-15s\u0003";
        String msg = String.format(MSG_16_B, extNumber, truncate(guestName, 15));
        queueMessage(msg);
    }

    private void queueMessage(String message) {
        log.debug("Queue Messages Called");
        message = message + (char) lrc(message);
        messages.add(message.getBytes());
        log.debug("Messages Size : " + messages.size());
    }

    /**
     * Checkout Method
     *
     * @param extensionNumber should be a integer less than 9999
     */
    protected final void checkOut(int extensionNumber) {
        /** Checkout Message Template requires 1 argument Extension Number */
        String MSG_16_2 = "\u00021!L16112%-4s  \u0003";
        if (extensionNumber > 9999) {
            throw new IllegalArgumentException("Extension Number more than 4 digits");
        }
        String msg2 = String.format(MSG_16_2, extensionNumber);
        log.debug(msg2);
        queueMessage(msg2);
    }

    protected final String truncate(String string, int length) {
        if(string == null){
            return null;
        }
        String rv;
        if (string.length() < length) {
            rv = string;
        } else {
            rv = string.substring(0, length);
        }
        return rv;
    }

    protected final void sync(Map<String, String> extensions) {
        String MSG_70_3 = "\u00021!L70073  \u0003";
        String MSG_70_4 = "\u00021!L70074  \u0003";
        Map<Integer, String> extMap = new HashMap<Integer, String>();
        for(Map.Entry<String, String> entry: extensions.entrySet()){
            Integer key;
            try {
                key = Integer.parseInt(entry.getKey());
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Cannot Pass Map with invalid Keys : " + entry.getKey(), e);
            }
            extMap.put(key, truncate(entry.getValue(), 15));
        }

        //Queue 70.3 Message
        queueMessage(MSG_70_3);
        // For Each Extension queue 17.B Message
        for (Map.Entry<String, String> entry : extensions.entrySet()) {
            queueMessage(String.format("%s%s", entry.getKey(), entry.getValue()));
        }
        //Queue 70.4 Message
        queueMessage(MSG_70_4);
        for (Map.Entry<String, String> entry : extensions.entrySet()) {
            queueMessage(String.format(MSG_21_6, truncate(entry.getKey(), 4), truncate(entry.getValue(), 15)));
        }
    }

    protected final void stop() {
        if (messageScheduledFuture != null) {
            messageScheduledFuture.cancel(true);
        }
        if (statusEnquiryScheduledFuture != null) {
            statusEnquiryScheduledFuture.cancel(true);
        }
        scheduler.shutdown();
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

    private enum RoomImageKey{
        EXTENSION_NUMBER,
        IS_VACANT,
        MW_LAMP_STATUS,
        RESTRICTION,
        ROOM_OCCUPANCY,
        IS_RESERVED,
        LANGUAGE,
        MAID_STATUS,
        WAKE_UP_RESULT,
        WAKE_UP_TIME
    }
}
