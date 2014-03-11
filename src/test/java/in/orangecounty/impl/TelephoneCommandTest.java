package in.orangecounty.impl;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * User: thomas
 * Date: 3/12/13
 * Time: 11:15 AM
 */
public class TelephoneCommandTest {
    private final String MSG_70_F_CRC = "\u00021!L7007F  \u0003\u0019";
    private final String MSG_70_F = "\u00021!L7007F  \u0003";
    private final String MSG_70_3 = "\u00021!L70073  \u0003";
    private final String MSG_70_4 = "\u00021!L70074  \u0003";
    /** Checkout Message Template requires 1 argument Extension Number */
    private final String MSG_16_2= "\u00021!L16112%-4s  \u0003";
    /** Checkin Message Template requires 2 arguments Extension Number and Guest Name */
    private final String MSG_16_B = "\u00021!L1634B%-4s        21%-15s\u0003";
    private Logger log = LoggerFactory.getLogger(TelephoneCommandTest.class);
    TelephoneCommandImpl telephoneCommand;
    SenderImpl sender;

    @Before
    public void setup() {
        sender = mock(SenderImpl.class);
        telephoneCommand = new TelephoneCommandImpl(sender);
    }

//    @Test
//    public void testActivateExtension() {
//        try {
//            telephoneCommand.checkIn("200", "Thomas");
//            doReturn(false).when(sender).sendMessage(any(byte[].class));
//            doReturn(true).when(sender).sendMessage(any(byte[].class));
//            Thread.sleep(2000);
//
//            verify(sender, atLeastOnce()).sendMessage(any(byte[].class));
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
//    }

    @Test
    public void testPresCheckOut() throws Exception {
        String extension, msg1, msg2;
        extension = "202245";
        msg1 = telephoneCommand.paresCheckOut(extension);
        msg2 = String.format(MSG_16_2, extension);
        log.debug(msg1);
        log.debug(msg2);
        Assert.assertEquals(msg1, msg2);
        extension = "202";
        msg1 = telephoneCommand.paresCheckOut(extension);
        msg2 = String.format(MSG_16_2, extension);
        log.debug(msg1);
        log.debug(msg2);
        Assert.assertEquals(msg1, msg2);

    }

    @Test
    public void testTruncate() throws Exception {
        String s = "This is a long String";
        Assert.assertTrue("String Length Should be 10", telephoneCommand.truncate(s, 10).length() == 10);
        Assert.assertFalse("String Length should not be 10", telephoneCommand.truncate(s, 30).length() == 10);
    }
}
