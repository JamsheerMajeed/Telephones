package in.orangecounty.cli;

import in.orangecounty.DriverController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by thomas on 31/12/13.
 */
public class DriverControllerStub implements DriverController {
    private Logger log = LoggerFactory.getLogger(DriverControllerStub.class);

    public final void start() {
        log.debug("Start Called");
    }

    public final void stop() {
        log.debug("Stop Called");
    }

    @Override
    public final void checkIn(String extension, String name) {
        log.debug(String.format("Check in called with entension : %s | name : %s", extension, name));
    }

    @Override
    public final void checkOut(String extension) {
        log.debug(String.format("Check out called with extension : %s", extension));
    }

    @Override
    public final void sync(Map<String, String> extensions) {
        System.out.printf("\nSync Called \n Parameters %s \n\n", extensions.toString());
    }
}
