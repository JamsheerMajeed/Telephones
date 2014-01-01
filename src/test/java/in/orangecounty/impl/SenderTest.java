package in.orangecounty.impl;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

/**
 * User: thomas
 * Date: 27/11/13
 * Time: 10:13 AM
 */
public class SenderTest {
    Logger log = LoggerFactory.getLogger(SenderTest.class);
    SenderImpl sender;
    SenderImpl si;
    OutputStream outputStream = mock(OutputStream.class);
    private final byte[] SELECTING_SEQUENCE = "\u0031\u0021\u0005".getBytes();
    private final byte[] ACK = new byte[]{4};
    private final byte[] ENQ = new byte[]{5};
    private final byte[] EOT = new byte[]{6};
    private final byte[] NAK = new byte[]{21};

    @Before
    public void setup(){
        si = new SenderImpl(outputStream);
        sender = si;
    }
    @After
    public void tearDown(){
        si = null;
        outputStream = null;
    }

    @Test
    public void testSendAck(){
        sender.sendACK();
        try {
            verify(outputStream).write(ACK);
            verify(outputStream).flush();
        } catch (IOException e) {
            log.debug("IOException :",e);
        }
    }

    @Test
    public void testActivateExtension(){
        byte[] expectedMessage = new byte[]{2, 49, 33, 76, 49, 54, 51, 52, 66, 50, 48, 50, 32, 32, 32, 32, 32, 32, 32, 32, 32, 50, 49, 84, 104, 111, 109, 97, 115, 32, 32, 32, 32, 32, 32, 32, 32, 32, 3, 2};
        si.sendMessage(expectedMessage);
        assertTrue(sender.isSending());
        try {
            Thread.sleep(10);
            verify(outputStream).write(SELECTING_SEQUENCE);
            assertTrue(sender.isSending());
            sender.ackReceived();
            Thread.sleep(10);
            verify(outputStream).write(expectedMessage);
            sender.ackReceived();
            assertFalse(sender.isSending());
            Thread.sleep(10);
            verify(outputStream).write(EOT);
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
            sender.ackReceived();
            Thread.sleep(33000);
            verify(outputStream, atLeast(32)).write(expectedMessage);
            verify(outputStream).write(EOT);
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
