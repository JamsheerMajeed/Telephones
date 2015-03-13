package in.orangecounty.tel.cli;

import in.orangecounty.tel.impl.DataLinkProtocol;
import in.orangecounty.tel.impl.SerialImpl;
import in.orangecounty.tel.SerialSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Created by thomas on 6/3/15.
 */
public class Bootstrap {
    private static final Logger log = LoggerFactory.getLogger(Bootstrap.class);
    public static void main(String[] args) {

        final DataLinkProtocol dataLinkProtocol = new DataLinkProtocol();
        final SerialSender serialSender = new SerialImpl();
        serialSender.setSerialListener(dataLinkProtocol);
        dataLinkProtocol.setSerialSender(serialSender);

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
            /* Process input commands */
            private void processCommand(String command) {
                if(command.toUpperCase().equals("EXIT")){
                    serialSender.stop();
                    System.exit(0);
                } else if(command.toUpperCase().equals("STOP")){
                    serialSender.stop();
                } else if(command.toUpperCase().equals("LIST")){
                    serialSender.listPorts();
                } else if(command.toUpperCase().equals("ENQ")){
                    try {
                        serialSender.sendMessage(new byte[]{5});
                    } catch (IOException e) {
                        log.debug("IO Exception :", e);
                    }
                } else if(command.toUpperCase().equals("STATUS")){
                    dataLinkProtocol.sendMessage("1!L7007F  ");
                }else if(command.toUpperCase().equals("START")){
                    try {
                        serialSender.start();
                    } catch (IOException e) {
                        log.error("IO Exception", e);
                    }
                } else {
                    try {
                        dataLinkProtocol.sendMessage(command);
                    } catch (RuntimeException e) {
                        log.debug("Runtime Exception", e);
                    }
                }
                
            }
        });
        t2.start();

    }
}
