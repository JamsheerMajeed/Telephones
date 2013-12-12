package in.orangecounty.impl;

import in.orangecounty.SenderInterface;
import in.orangecounty.TelephoneCommands;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static in.orangecounty.impl.Constants.*;

/**
 * User: thomas
 * Date: 3/12/13
 * Time: 9:05 AM
 */
public class TelephoneCommandImpl implements TelephoneCommands {
    Logger log = LoggerFactory.getLogger(TelephoneCommandImpl.class);

    private boolean running = true;
    private SenderInterface sender;
    private Queue<byte[]> messages = new LinkedList<byte[]>();
    private final ScheduledExecutorService scheduler;
    private ScheduledFuture scheduledFuture;



    public TelephoneCommandImpl(SenderInterface sender) {
        this.sender = sender;
        scheduler = Executors.newScheduledThreadPool(1);
        scheduledFuture = scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                sendMessages();
            }
        },0,1,TimeUnit.SECONDS);
    }

    private void sendMessages() {
        int count = 0;
        while(!messages.isEmpty()){
            synchronized (messages) {
                if (sender.sendMessage(messages.peek())) {
                    messages.poll();
                } else {
                    if(count > 10){
                        break;
                    }
                    count++;
                }
            }
        }
    }

    @Override
    public void checkIn(String extensionNumber, String guestName) {
        log.debug("checkIn Called");
        byte[] msg = parseActivateExtension(reSizeExtensionNumber(extensionNumber), reSizeGuestName(guestName));
        synchronized (messages) {
            messages.add(msg);
        }
    }

    @Override
    public void deActivateExtension(String extensionNumber) {
        sender.sendMessage(parseDeActivateExtension(extensionNumber));
    }

    @Override
    public void sync(HashMap<String, String> extensions) {
        //TODO Implement Sync Properly.
        log.debug("Sync Called" + extensions);
    }

    @Override
    public void statusEnquiry() {
        sender.sendMessage(STATUS_ENQUIRY);
    }

    @Override
    public void stop() {
        if(scheduledFuture!=null){
            scheduledFuture.cancel(true);
        }
        if(scheduler!=null){
            scheduler.shutdown();
        }
    }

    /**
     * A
     *
     * @param extensionNumber
     * @param guestName
     * @return
     */
    protected byte[] parseActivateExtension(String extensionNumber, String guestName) {
        log.debug("parseActivateExtension Called");
        // Standard Header STX, SA, UA
        byte[] msg = new byte[]{STX, SA, UA};
        //Adding the Entry Index "L", Feature Code Index "16", Message Counter "11", Function Code "B"
        msg = ArrayUtils.addAll(msg, "L1634B".getBytes());
        //Adding the Extension Number
        msg = ArrayUtils.addAll(msg, rightPad(extensionNumber, 4).getBytes());
        //Adding Unused Spaces, Language "2" for English and Rook Occupancy "1"
        msg = ArrayUtils.addAll(msg, "        21".getBytes());
        //Adding Guest Name
        msg = ArrayUtils.addAll(msg, rightPad(guestName, 15).getBytes());
        //Adding End of Transmission
        msg = ArrayUtils.addAll(msg, new byte[]{ETX});
        //Adding and returning lrc to the message
        return ArrayUtils.add(msg, lrc(msg));
    }

    private byte[] parseDeActivateExtension(String extensionNumber) {
        // Standard Header STX, SA, UA
        byte[] msg = new byte[]{STX, SA, UA};
        //Adding the Entry Index "L"
        msg = ArrayUtils.addAll(msg, "L".getBytes());
        //Adding the Feature Code Index "16"
        msg = ArrayUtils.addAll(msg, "16".getBytes());
        //Adding the Message Counter "11"
        msg = ArrayUtils.addAll(msg, "11".getBytes());
        //Adding the Function Code "2" (For checkout)
        msg = ArrayUtils.addAll(msg, "2".getBytes());
        //Adding the Extension Number
        msg = ArrayUtils.addAll(msg, rightPad(extensionNumber, 4).getBytes());
        //Adding Unused Spaces and End of Transmission
        msg = ArrayUtils.addAll(msg, new byte[]{32, 32, ETX});
        //Adding and returning lrc to the message
        return ArrayUtils.add(msg, lrc(Arrays.copyOfRange(msg, 1, msg.length)));
    }

//
//    @Override
//    public void statusEnquiry() {
//        byte[] statusEnquiry = new byte[]{2, 49, 33, 76, 55, 48, 48, 55, 70, 32, 32, 3, 25};
//        buffer.add(statusEnquiry);
//    }

    private String reSizeExtensionNumber(String extensionNumber) {
        return rightPad(extensionNumber, 4);
    }

    private String reSizeGuestName(String guestName) {
        return rightPad(guestName, 15);
    }

    public static String rightPad(String string, int size) {
        int len = string.length();
        if (len > size) {
            return (StringUtils.substring(string, 0, size));
        } else {
            return (StringUtils.rightPad(string, size, ' '));
        }
    }

    private byte lrc(final String msg) {
        byte lrc = 0;
        byte[] byteArray = msg.getBytes();
        for (int x = 1; x < byteArray.length; x++) {
            lrc = (byte) (lrc ^ byteArray[x]);
        }
        return lrc;
    }

    private byte lrc(final byte[] msg) {
        byte rv = 0;
        for (byte item : msg) {
            rv = (byte) (rv ^ item);
        }
        return rv;
    }
}
