package in.orangecounty.impl;

/**
 * User: thomas
 * Date: 26/11/13
 * Time: 6:28 PM
 */
public class Constants {
    public static final byte[] SELECTING_SEQUENCE = {49, 33, 5};

    public static final byte[] DLE_SEND = {16, 60};

    public static final byte[] DLE_STOP = {16, 124};

    public static final byte[] STATUS_ENQUIRY = {2,49,33,76,55,48,48,55,48,32,32,3,111};

    public static final int BUFFER_SIZE = 256;


    /**
     * Enquiry.
     */
    public static final byte ENQ = 5;
    /**
     * Start of Transmission.
     */
    public static final byte STX = 2;
    /**
     * End of Transmission.
     */
    public static final byte ETX = 3;
    /**
     * Positive Acknowledge.
     */
    public static final byte ACK = 6;
    /**
     * Negative Acknowledgement.
     */
    public static final byte NAK = 21;
    /**
     * End of Transmission.
     */
    public static final byte EOT = 4;
    /**
     * DLE.
     */
    public static final byte DLE = 16;
    /**
     * Permission to Send Char.
     */
    public static final byte PSC = 60;
    /**
     * Stop Transmission Char.
     */
    public static final byte STC = 124;

    public static final byte SA = 29;

    public static final byte UA = 33;

}
