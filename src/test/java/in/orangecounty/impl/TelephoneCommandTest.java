package in.orangecounty.impl;

import in.orangecounty.SenderInterface;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

/**
 * User: thomas
 * Date: 3/12/13
 * Time: 11:15 AM
 */
public class TelephoneCommandTest {
    TelephoneCommandImpl telephoneCommand;
    SenderInterface sender;

    @Before
    public void setup() {
        sender = mock(SenderInterface.class);
        telephoneCommand = new TelephoneCommandImpl(sender);
    }

    @Test
    public void testActivateExtension() {
        try {
            telephoneCommand.checkIn("200", "Thomas");
            doReturn(false).when(sender).sendMessage(any(byte[].class));
            doReturn(true).when(sender).sendMessage(any(byte[].class));
            Thread.sleep(2000);
            verify(sender).sendMessage(any(byte[].class));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testRightPad() {
        assert (TelephoneCommandImpl.rightPad("a", 4).equals("a   "));
        assert (TelephoneCommandImpl.rightPad("abcdef", 4).equals("abcd"));
    }
}
