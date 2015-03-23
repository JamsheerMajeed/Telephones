package in.orangecounty.tel.cli;

import in.orangecounty.tel.NEAX7400PmsProtocol;
import in.orangecounty.tel.impl.DataLinkProtocolImpl;
import in.orangecounty.tel.impl.NEAX7400PmsProtocolImpl;
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

        final DataLinkProtocolImpl dataLinkProtocolImpl = new DataLinkProtocolImpl();
        final NEAX7400PmsProtocol neax7400PmsProtocol = new NEAX7400PmsProtocolImpl();


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
//                    serialSender.stop();
                    System.exit(0);
                } else if(command.toUpperCase().equals("STOP")){
//                    serialSender.stop();
                } else if(command.toUpperCase().equals("LIST")){
//                    serialSender.listPorts();
                } else if(command.toUpperCase().equals("ENQ")){
                    try {
//                        serialSender.sendMessage(new byte[]{5});
                    } catch (Exception e) {
                        log.debug("Exception on Send Message", e);
                    }
                } else if(command.toUpperCase().equals("STATUS")){
                    dataLinkProtocolImpl.sendMessage("1!L7007F  ");
                } else if(command.toUpperCase().equals("CHECKIN")){
                    dataLinkProtocolImpl.sendMessage("1!L1634B333         21serverroom     ");
//                    dataLinkProtocol.sendMessage("1!L21266300   SERVERROOM     ");
                    dataLinkProtocolImpl.sendMessage("1!L21266333   serverroom     ");

                } else if(command.toUpperCase().equals("REMOVE")){
                    dataLinkProtocolImpl.sendMessage("1!L21266333                  ");

                } else if(command.toUpperCase().equals("SET")){
                    dataLinkProtocolImpl.sendMessage("1!L15141333   1  ");

                } else if(command.toUpperCase().equals("UNSET")){
                    dataLinkProtocolImpl.sendMessage("1!L15141333   0  ");

                } else if(command.toUpperCase().equals("SYNC")){
//                    neax7400PmsProtocolImpl.sync();

                } else if(command.toUpperCase().equals("CHANGE")){
                    dataLinkProtocolImpl.sendMessage("1!L21266333   333            ");


                } else if(command.toUpperCase().equals("CHANGE2")){
                    dataLinkProtocolImpl.sendMessage("1!L21266333   guestguest     ");

                } else if(command.toUpperCase().equals("CHANGEC")){
                    dataLinkProtocolImpl.sendMessage("1!L21266333   GUESTGUESTGUEST");

                } else if(command.toUpperCase().equals("CHECKINTWO")){
                    dataLinkProtocolImpl.sendMessage("1!L16111333   ");
                    dataLinkProtocolImpl.sendMessage("1!L21266333   serverroom     ");
                } else if(command.toUpperCase().equals("CHECKOUT")){
                    dataLinkProtocolImpl.sendMessage("1!L16112333   ");
                } else if(command.toUpperCase().equals("GETDATA")){
                    dataLinkProtocolImpl.sendMessage("1!L70078  ");
                }else if(command.toUpperCase().equals("START")){

//                        serialSender.start();

                        neax7400PmsProtocol.start();


                } else {
                    try {
                        dataLinkProtocolImpl.sendMessage(command);
                    } catch (RuntimeException e) {
                        log.debug("Runtime Exception", e);
                    }
                }
                
            }
        });
        t2.start();

    }
}
