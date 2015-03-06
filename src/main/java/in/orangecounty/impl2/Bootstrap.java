package in.orangecounty.impl2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.TooManyListenersException;

/**
 * Created by thomas on 6/3/15.
 */
public class Bootstrap {
    private static final Logger log = LoggerFactory.getLogger(Bootstrap.class);
    public static void main(String[] args) {
        final MessageSender messageSender = new SerialMessageSenderImpl();
        Thread t2 = new Thread(new Runnable() {
            @Override
            public void run() {
                BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
                log.debug("In Run");
                String command=null;
                try {
                    while((command=in.readLine())!=null){
                        processCommand(command);
                    }
                } catch (IOException e) {
                }
            }

            private void processCommand(String command) {
                if(command.toUpperCase().equals("EXIT")){
                    messageSender.stop();
                    System.exit(0);
                } else if(command.toUpperCase().equals("LIST")){
                    messageSender.listPorts();
                }else if(command.toUpperCase().equals("START")){
                    try {
                        messageSender.start();
                    } catch (IOException e) {
                        log.error("IO Exception", e);
                    }
                } else {
                    try {
                        messageSender.sendMessage(command.getBytes());
                    } catch (IOException e) {
                        log.debug("IO Exception", e);
                    }
                }
                
            }
        });
        t2.start();

    }
}
