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
public class ListenerThreadImpl implements Runnable {
    Logger log = LoggerFactory.getLogger(ListenerSerialEventImpl.class);
    private InputStream in;
    private ListenerSenderInterface sender;
    byte[] buffer;

    /**
     * Initialize.
     */
    public ListenerThreadImpl(InputStream in, ListenerSenderInterface sender) {
        this.in = in;
        this.sender = sender;
    }

    public void run(){
        while (true){
            try {
                buffer = ArrayUtils.add(buffer, (byte)in.read());
                log.debug(Arrays.toString(buffer));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
