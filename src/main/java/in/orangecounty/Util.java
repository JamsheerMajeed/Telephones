package in.orangecounty;

/**
 * User: thomas
 * Date: 26/11/13
 * Time: 12:43 PM
 */
public class Util {
    /**
     * This method checks the LRC (longitudinal redundancy check) of the message with the bcc (block check character)
     * @param msg The message that we need to check the bcc for
     * @param bcc The BCC to check against.
     * @return True if the bcc matches the lrc of the message else returns false
     */
    public static boolean checkBCC(byte[] msg, byte bcc){
        return calculateBCC(msg) ==  bcc;
    }

    /**
     * This method calculates the LRC (longitudinal redundancy check) of the
     * message and returns a bcc (block check character)
     * @param msg the message for which a bcc needs to be calculated
     * @return the bcc of the message
     */
    public static byte calculateBCC(byte[] msg){
        byte lrc = 0;
        for(byte item : msg){
            lrc = (byte)(0xff & (lrc ^ item));
        }
        return lrc;
    }
}
