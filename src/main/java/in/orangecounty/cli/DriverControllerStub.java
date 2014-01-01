package in.orangecounty.cli;

import in.orangecounty.DriverController;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

/**
 * Created by thomas on 31/12/13.
 */
public class DriverControllerStub implements DriverController {
    Logger log = LoggerFactory.getLogger(DriverControllerStub.class);

    public void start() {
        System.out.printf("\nStart Called\n\n");
    }

    public void stop() {
        System.out.printf("\nStop Called\n\n");
    }

    @Override
    public void checkIn(String extension, String name) {
        System.out.printf("\nCheck In Called \nextension : %s \nname : %s\n\n", extension, name);
    }

    @Override
    public void checkOut(String extension) {
        System.out.printf("\nCheck Out Called \nextension : %s \n\n", extension);

    }

    @Override
    public void sync(Map<String, String> extensions) {
        System.out.printf("\nSync Called \n Parameters %s \n\n", extensions.toString());
    }
}
