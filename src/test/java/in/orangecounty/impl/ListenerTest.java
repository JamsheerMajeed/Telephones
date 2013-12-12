package in.orangecounty.impl;

import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import in.orangecounty.ListenerSenderInterface;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

import static org.mockito.Mockito.*;
import static in.orangecounty.impl.Constants.*;

import static org.junit.Assert.assertTrue;

/**
 * User: thomas
 * Date: 25/11/13
 * Time: 7:32 PM
 * ListenerTest Class
 */
public class ListenerTest {
    Logger log = LoggerFactory.getLogger(ListenerTest.class);
    OutputStream os;
    ListenerSenderInterface sender;
    ListenerImpl listener;
    SerialPortEventListener spel;
    @Before
    public void setup(){
        try {
            PipedInputStream in = new PipedInputStream();
            os = new PipedOutputStream(in);
            sender = mock(ListenerSenderInterface.class);
            listener = new ListenerImpl(in, sender);
            spel = listener;
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }
    @After
    public void tearDown(){
    }

    @Test
    public void testAckReceived(){
        log.debug("Testing testAckReceived");
        try {
            os.write(ACK);
            os.flush();
            SerialPortEvent spe = mock(SerialPortEvent.class);
            doReturn(SerialPortEvent.DATA_AVAILABLE).when(spe).getEventType();
            spel.serialEvent(spe);
            verify(sender).ackReceived();
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Test
    public void testSendInitSequence(){
        log.debug("Testing testSendInitSequence");
        try {
            os.write(SELECTING_SEQUENCE);
            os.flush();
            doReturn(false).when(sender).isSending();
            SerialPortEvent spe = mock(SerialPortEvent.class);
            doReturn(SerialPortEvent.DATA_AVAILABLE).when(spe).getEventType();
            spel.serialEvent(spe);
            verify(sender).sendACK();
            verify(sender).setReceiving(true);
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    @Test
    public void testInitSequenceConflict(){
        log.debug("Testing testInitSequenceConflict");
        try {
            os.write(SELECTING_SEQUENCE);
            os.flush();
            doReturn(true).when(sender).isSending();
            SerialPortEvent spe = mock(SerialPortEvent.class);
            doReturn(SerialPortEvent.DATA_AVAILABLE).when(spe).getEventType();
            spel.serialEvent(spe);
            verify(sender).resendSelectSequence();
        } catch (IOException e) {
            log.debug("IOException : ", e);
        }
    }

//    @Test
    public void testListenerTimeout(){
        log.debug("Testing testListenerTimeout");
        try {
            os.write(SELECTING_SEQUENCE);
            os.flush();
            SerialPortEvent spe = mock(SerialPortEvent.class);
            doReturn(SerialPortEvent.DATA_AVAILABLE).when(spe).getEventType();
            spel.serialEvent(spe);
            doReturn(false).when(sender).isSending();
            verify(sender).sendACK();
            verify(sender).setReceiving(true);
            Thread.sleep(32500);
            verify(sender).setReceiving(false);
        } catch (IOException e) {
            log.debug("IOException : ", e);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testMessage(){
        log.debug("Testing testMessage");
        try {
            os.write("\u00021!L14502333   00000409886894936     1100360000100000 \u0003H".getBytes());
            os.flush();
            SerialPortEvent spe = mock(SerialPortEvent.class);
            doReturn(SerialPortEvent.DATA_AVAILABLE).when(spe).getEventType();
            spel.serialEvent(spe);
            verify(sender).sendACK();
        } catch (IOException e) {
            log.debug("IOException : ", e);
        }
    }

    @Test
    public void testMessageBadBcc(){
        log.debug("Testing testMessageBadBcc");
        try {
            os.write("\u00021!L14502333   00000409886894936     1100360000100000 \u0003G".getBytes());
            os.flush();
            SerialPortEvent spe = mock(SerialPortEvent.class);
            doReturn(SerialPortEvent.DATA_AVAILABLE).when(spe).getEventType();
            spel.serialEvent(spe);
            verify(sender).sendNAK();
        } catch (IOException e) {
            log.debug("IOException : ", e);
        }
    }

    @Test
    public void testEOTReceived(){
        log.debug("Testing testEOTReceived");
        try {
            os.write(EOT);
            os.flush();
            SerialPortEvent spe = mock(SerialPortEvent.class);
            doReturn(SerialPortEvent.DATA_AVAILABLE).when(spe).getEventType();
            spel.serialEvent(spe);
            verify(sender).setReceiving(false);
        } catch (IOException e) {
            log.debug("IOException : ", e);
        }
    }
}
