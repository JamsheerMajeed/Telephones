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

    void sync(Map<String, String> extensions);
}
