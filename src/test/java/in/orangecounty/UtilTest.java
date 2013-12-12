package in.orangecounty;

import org.junit.Test;

import java.util.Arrays;

import static org.junit.Assert.assertTrue;
import static in.orangecounty.Util.checkBCC;

/**
 * User: thomas
 * Date: 26/11/13
 * Time: 12:47 PM
 */
public class UtilTest {
    @Test
    public void testBCC(){
        byte[] msg = new byte[]{49,33,76,49,54,49,49,50,49,49,50,32,32,32,3};
        assertTrue(checkBCC(msg, (byte)120));

        msg = new byte[]{49,33,76,55,48,48,55,70,32,32,3};
        assertTrue(checkBCC(msg, (byte)25));
        msg = new String("1!L14502333   00000409886894936     1100360000100000 \u0003").getBytes();
        System.out.println(Arrays.toString(msg));
        assertTrue(checkBCC(msg, (byte) 'H'));
        msg = new String("1!L70070  \u0003").getBytes();
        System.out.println(Arrays.toString(msg));
        assertTrue(checkBCC(msg, (byte) 'o'));
        msg = new String("1!L14502250   00000009901772599     0908180002300000 \u0003").getBytes();
        System.out.println(Arrays.toString(msg));
        assertTrue(checkBCC(msg,(byte)'E'));
    }
}
