package in.orangecounty.impl;

import gnu.io.*;
import in.orangecounty.DriverController;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.TooManyListenersException;

/**
 * DriverControllerImpl is the Front Facing Class.  Driver Controller has all the methods of the PBX and also two methods
 * Start and Stop.
 * Start sets up all the required resources
 * Stop cleans up all the resources.
 * Created by thomas on 30/12/13.
 */
public class DriverControllerImpl implements DriverController {
    Logger log = LoggerFactory.getLogger(DriverControllerImpl.class);

    SerialPort serialPort;
    private boolean running;
    private TelephoneCommandImpl telephoneCommands;
    OutputStream outputStream;
    InputStream inputStream;
    private SenderImpl sender;
    private SerialListener listener;
//    private ListenerThreadImpl listenerThread;
//    SerialHelper serialHelper;

    @Override
    public void start() {
        connect();
        sender = new SenderImpl(outputStream);
        log.debug("Sender Created");
        SerialListener serialListener = new SerialListener(inputStream, sender);
        try {
            serialPort.addEventListener(serialListener);
        } catch (TooManyListenersException e) {
            e.printStackTrace();
        }
        log.debug("Listener Created");
        telephoneCommands = new TelephoneCommandImpl(sender);
        log.debug("Telephone Command Created");
    }

    private void connect() {
        try {
            //TODO currently /dev/ttyS0 is hard coded find a way to pass it as a parameter.
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier("/dev/ttyS0");
            if (portIdentifier.isCurrentlyOwned()) {
                log.error("Port In Use");
            } else {
                // points who owns the port and connection timeout
                serialPort = (SerialPort) portIdentifier.open("TelApp", 2000);
                // setup connection parameters
                serialPort.setSerialPortParams(
                        1200,
                        SerialPort.DATABITS_7,
                        SerialPort.STOPBITS_1,
                        SerialPort.PARITY_EVEN);
                serialPort.notifyOnDataAvailable(true);
                outputStream = serialPort.getOutputStream();
                inputStream = serialPort.getInputStream();
            }
        } catch (PortInUseException e) {
            log.error("PortInUseException Thrown : ", e);
        } catch (IOException e) {
            log.error("IOException Thrown : ", e);
        } catch (NoSuchPortException e) {
            log.error("NoSuchPortException Thrown : ", e);
        } catch (UnsupportedCommOperationException e) {
            log.error("UnsupportedCommOperationException Thrown : ", e);
        }
    }

    @Override
    public void stop() {
        if (sender != null) {
            sender.stop();
        }
        if (telephoneCommands != null) {
            telephoneCommands.stop();
        }
        serialPort.removeEventListener();
        serialPort.close();
        running = false;
    }

    @Override
    public void checkIn(String extension, String name){
        telephoneCommands.checkIn(extension, name);
    }

    @Override
    public void checkOut(String extension){
        telephoneCommands.checkOut(extension);
    }

    @Override
    public void sync(Map<String, String> extensions){
        telephoneCommands.sync(extensions);
    }
}
