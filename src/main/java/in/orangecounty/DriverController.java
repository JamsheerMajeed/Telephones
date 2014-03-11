package in.orangecounty;

import java.util.Map;

/**
 * Created by thomas on 31/12/13.
 */
public interface DriverController {
    void start();

    void stop();

    void checkIn(String extension, String name);

    void checkOut(String extension);

    /**
     * A Method to synchronize the Extensions with the PBX.
     *
     * @param extensions The Extensions would be a Map. The Maps keys would be the extension number and
     *                   the value would the name of the guest or the user at the extensions.
     *                   If the extension is not used the value would be NULL.
     */
    void sync(Map<String, String> extensions);
}
