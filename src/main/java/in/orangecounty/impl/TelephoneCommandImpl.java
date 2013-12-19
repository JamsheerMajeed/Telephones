package in.orangecounty.impl;

import in.orangecounty.SenderInterface;
import in.orangecounty.TelephoneCommands;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;


/**
 * User: thomas
 * Date: 3/12/13
 * Time: 9:05 AM
 */
public class TelephoneCommandImpl implements TelephoneCommands {
    Logger log = LoggerFactory.getLogger(TelephoneCommandImpl.class);
    private static final String STATUS_ENQUIRY = "\u00021!L7007F  \u0003\u0019";


    private SenderInterface sender;
    private final Object messageReadFlag = new Object();
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
        }, 0, 1, TimeUnit.SECONDS);
    }

    private void sendMessages() {
        int count = 0;
        while (!messages.isEmpty()) {
            synchronized (messageReadFlag) {
                if (sender.sendMessage(messages.peek())) {
                    messages.poll();
                } else {
                    if (count > 10) {
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
        synchronized (messageReadFlag) {
            messages.add(msg);
        }
    }

    @Override
    public void deActivateExtension(String extensionNumber) {
        sender.sendMessage(parseDeActivateExtension(extensionNumber));
    }

    @Override
    public void sync(HashMap<String, String> extensions) {

    }


    @Override
    public void statusEnquiry() {
        sender.sendMessage(STATUS_ENQUIRY.getBytes());
    }

    @Override
    public void stop() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
        scheduler.shutdown();
    }

    public byte[] parseActivateExtension(String extensionNumber, String guestName) {
        guestName = reSizeGuestName(guestName);
        extensionNumber = reSizeExtensionNumber(extensionNumber);
        String message = "\u00021!L1634B" + extensionNumber + "        21" + guestName + "\u0003";
        message = message + (char) lrc(message);
        return message.getBytes();
    }

    public byte[] parseDeActivateExtension(String extensionNumber) {
        extensionNumber = reSizeExtensionNumber(extensionNumber);
        String message = "\u00021!L16112" + extensionNumber + "  \u0003";
        message = message + (char) lrc(message);
        return message.getBytes();
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

    private byte lrc(final String msg) {
        byte lrc = 0;
        byte[] byteArray = msg.getBytes();
        for (int x = 1; x < byteArray.length; x++) {
            lrc = (byte) (lrc ^ byteArray[x]);
        }
        return lrc;
    }
}
