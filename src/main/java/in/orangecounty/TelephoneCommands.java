package in.orangecounty;

import java.util.HashMap;

/**
 * User: thomas
 * Date: 27/11/13
 * Time: 6:05 PM
 */
public interface TelephoneCommands {
    /**
     *
     * @param extensionNumber
     * @param guestName
     */
    public void checkIn(String extensionNumber, String guestName);

    /**
     *
     * @param extensionNumber
     */
    public void deActivateExtension(String extensionNumber);

    public void sync(HashMap<String, String> extensions);

    public void statusEnquiry();

    public void stop();
}
