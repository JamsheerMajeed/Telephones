package in.orangecounty.impl;

import in.orangecounty.ListenerSenderInterface;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static in.orangecounty.impl.Constants.*;
import static org.mockito.Mockito.*;

/**
 * User: thomas
 * Date: 27/11/13
 * Time: 10:13 AM
 */
public class SenderTest {
    Logger log = LoggerFactory.getLogger(SenderTest.class);
    ListenerSenderInterface listenerSenderInterface;
    SenderImpl si;
    OutputStream outputStream = mock(OutputStream.class);

    @Before
    public void setup(){
        si = new SenderImpl(outputStream);
        listenerSenderInterface = si;
    }
    @After
    public void tearDown(){
        si = null;
        outputStream = null;
    }

    @Test
    public void testSendAck(){
        listenerSenderInterface.sendACK();
        try {
            byte[] ackArray = new byte[]{ACK};
            verify(outputStream).write(ackArray);
            verify(outputStream).flush();
        } catch (IOException e) {
            log.debug("IOException :",e);
        }
    }

    @Test
    public void testActivateExtension(){
        byte[] expectedMessage = new byte[]{2, 49, 33, 76, 49, 54, 51, 52, 66, 50, 48, 50, 32, 32, 32, 32, 32, 32, 32, 32, 32, 50, 49, 84, 104, 111, 109, 97, 115, 32, 32, 32, 32, 32, 32, 32, 32, 32, 3, 2};
        si.sendMessage(expectedMessage);
        assertTrue(listenerSenderInterface.isSending());
        try {
            Thread.sleep(10);
            verify(outputStream).write(SELECTING_SEQUENCE);
            assertTrue(listenerSenderInterface.isSending());
            listenerSenderInterface.ackReceived();
            Thread.sleep(10);
            verify(outputStream).write(expectedMessage);
            listenerSenderInterface.ackReceived();
            assertFalse(listenerSenderInterface.isSending());
            byte[] eotArray = new byte[]{EOT};
            Thread.sleep(10);
            verify(outputStream).write(eotArray);
        } catch (IOException e) {
            log.debug("IOException", e);
        } catch (InterruptedException e) {
            log.debug("InterruptedException");
        }

    }

    @Test
    public void testInitTimerFunctionality(){
        byte[] expectedMessage = new byte[]{2, 49, 33, 76, 49, 54, 51, 52, 66, 50, 48, 50, 32, 32, 32, 32, 32, 32, 32, 32, 32, 50, 49, 84, 104, 111, 109, 97, 115, 32, 32, 32, 32, 32, 32, 32, 32, 32, 3, 2};
        si.sendMessage(expectedMessage);
        try {
            Thread.sleep(17000);
            verify(outputStream, atLeast(16)).write(SELECTING_SEQUENCE);
        } catch (IOException e) {
            log.debug("IOException", e);
        } catch (InterruptedException e) {
            log.debug("InterruptedException");
        }
    }

    @Test
    public void testMsgTimerFunctionality(){
        byte[] expectedMessage = new byte[]{2, 49, 33, 76, 49, 54, 51, 52, 66, 50, 48, 50, 32, 32, 32, 32, 32, 32, 32, 32, 32, 50, 49, 84, 104, 111, 109, 97, 115, 32, 32, 32, 32, 32, 32, 32, 32, 32, 3, 2};
        si.sendMessage(expectedMessage);
        try {
            Thread.sleep(100);
            listenerSenderInterface.ackReceived();
            Thread.sleep(33000);
            verify(outputStream, atLeast(32)).write(expectedMessage);
            byte[] eotArray = new byte[]{EOT};
            verify(outputStream).write(eotArray);
        } catch (InterruptedException e) {
            log.error("InterruptedException", e);
        } catch (IOException e) {
            log.debug("IOException", e);
        }
    }

    @Test
    public void testSendMultipleMessages() {
        byte[] msg1 = new byte[]{2, 49, 33, 76, 49, 54, 51, 52, 66, 50, 48, 50, 32, 32, 32, 32, 32, 32, 32, 32, 32, 50, 49, 84, 104, 111, 109, 97, 115, 32, 32, 32, 32, 32, 32, 32, 32, 32, 3, 2};
        byte[] msg2 = new byte[]{2, 49, 33, 76, 49, 54, 51, 52, 66, 50, 48, 50, 32, 32, 32, 32, 32, 32, 32, 32, 32, 50, 49, 84, 104, 111, 109, 97, 115, 32, 32, 32, 32, 32, 32, 32, 32, 32, 3, 2};

        assertTrue(si.sendMessage(msg1));
        assertFalse(si.sendMessage(msg2));
    }

    @Test
    public void testSendWhenReceiving() {
        byte[] msg = new byte[]{2, 49, 33, 76, 49, 54, 51, 52, 66, 50, 48, 50, 32, 32, 32, 32, 32, 32, 32, 32, 32, 50, 49, 84, 104, 111, 109, 97, 115, 32, 32, 32, 32, 32, 32, 32, 32, 32, 3, 2};
        si.setReceiving(true);
        assertFalse(si.sendMessage(msg));
    }
}
