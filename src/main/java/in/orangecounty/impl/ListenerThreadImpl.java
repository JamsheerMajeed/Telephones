package in.orangecounty.impl;

import in.orangecounty.ListenerSenderInterface;
//import in.orangecounty.Util;
//import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
//import java.util.concurrent.*;

//import static in.orangecounty.impl.Constants.*;

/**
 * ListenerThreadImpl
 * Created by thomas on 16/12/13.
 */
public class ListenerThreadImpl implements Runnable {
    Logger log = LoggerFactory.getLogger(ListenerSerialEventImpl.class);
    private InputStream in;
    private ListenerSenderInterface sender;
//    int[] buffer;
    byte[] buffer;

    /**
     * Initialize.
     */
    public ListenerThreadImpl(InputStream in, ListenerSenderInterface sender) {
        this.in = in;
        this.sender = sender;
    }

    public void run() {
        log.debug("ListenerThreadImpl Run Called");
        while (true) {
            try {
                int byteCount =  in.available();
                if (byteCount > 0) {
                    log.debug(String.format("%d bytes available for read", byteCount));
                    buffer = new byte[byteCount];
                    int rc = in.read(buffer, 0, byteCount);
                    if (rc < 0) {
                        log.debug("Error in read, breaking loop");
                        //break;
                    } else if (rc == 0) {
                        log.debug("No more data.");
                    }
                    log.debug(Arrays.toString(buffer));
                    //buffer = ArrayUtils.add(buffer);
                    //log.debug(Arrays.toString(buffer) + ":" + new String(buffer));
                }
//                else
//                    // Nothing to read
//                    continue;
            } catch (IOException e) {
                log.debug("IOException : ", e);
            }
        }
    }

    public void resetBuffer() {
        buffer = null;
    }

}
