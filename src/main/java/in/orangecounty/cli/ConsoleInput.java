package in.orangecounty.cli;

import gnu.io.*;
import in.orangecounty.ListenerInterface;
import in.orangecounty.TelephoneCommands;
import in.orangecounty.exp.SerialListener;
import in.orangecounty.impl.SenderImpl;
import in.orangecounty.impl.TelephoneCommandImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.TooManyListenersException;
import java.util.concurrent.*;

/**
 * Created by thomas on 9/12/13.
 * The ConsoleInput Class
 */
public class ConsoleInput implements Runnable {
    Logger log = LoggerFactory.getLogger(ConsoleInput.class);
    SerialPort serialPort;
    private boolean running;
    private TelephoneCommands telephoneCommands;
    OutputStream outputStream;
    InputStream inputStream;
    private SenderImpl sender;
    private ListenerInterface listener;
//    private ListenerThreadImpl listenerThread;
//    SerialHelper serialHelper;

    public void start() {
        connect();
//            serialHelper = new SerialHelper();
//            log.debug("Serial Helper Created");
//            serialHelper.connect("/dev/ttyS0");
//            log.debug("Serial Connect Called");
        sender = new SenderImpl(outputStream);
        log.debug("Sender Created");
        SerialListener serialListener = new SerialListener(inputStream);
        try {
            serialPort.removeEventListener();
            serialPort.addEventListener(serialListener);
            serialPort.notifyOnDataAvailable(true);
            serialPort.notifyOnCTS(true);
        } catch (TooManyListenersException e) {
            e.printStackTrace();
        }
        log.debug("Listener Created");
        telephoneCommands = new TelephoneCommandImpl(sender);
        log.debug("Telephone Command Created");
//            serialHelper.addDataAvailableListener(listenerSerialEvent);
    }

    private void stop() {
        if (sender != null) {
            sender.stop();
        }
        if (telephoneCommands != null) {
            telephoneCommands.stop();
        }
        serialPort.removeEventListener();
        serialPort.close();
//        if(listenerThread!=null){
//            listenerThread.stop();
//        }
        running = false;
    }


    public String readLine() {
        ExecutorService ex = Executors.newSingleThreadExecutor();
        String input = null;
        try {
            // start working
            while (true) {
                Future<String> result = ex.submit(
                        new ConsoleInputReadTask());
                try {
                    input = result.get(10, TimeUnit.SECONDS);
                    break;
                } catch (ExecutionException e) {
                    e.getCause().printStackTrace();
                } catch (TimeoutException e) {
                    log.debug("Cancelling reading task");
                    result.cancel(true);
                    log.debug("Thread cancelled. input is null");
                } catch (InterruptedException e) {
                    log.debug("InterruptedException", e);
                    break;
                }
            }
        } finally {
            ex.shutdownNow();
        }
        return input;
    }

    public void run() {
        running = true;
        start();
        while (running) {
            process(readLine());
        }
    }

    private void process(String command) {
        if (command.equals("exit")) {
            log.debug("Exit Called");
            stop();
        } else if (command.equals("enq")) {
            log.debug("enq Called");
            sender.sendEnq();
        } else if (command.equals("ack")) {
            log.debug("Ack Called");
            sender.sendACK();
        } else if (command.equals("nak")) {
            log.debug("Nak Called");
            sender.sendNAK();
        } else if (command.equals("resetbuf")) {
            log.debug("resetbuf Called");
            listener.resetBuffer();
        } else if (command.equals("fireCall")) {
            log.debug("fireCall Called");
        } else if (command.equals("activate")) {
            log.debug("activate Called");
            telephoneCommands.checkIn("202", "Thomas");
        } else if (command.equals("deactivate")) {
            log.debug("deactivate Called");
            telephoneCommands.deActivateExtension("202");
        } else if (command.equals("statusenq")) {
            log.debug("status Called");
            telephoneCommands.statusEnquiry();
        } else {
            printMessage();
        }
    }

    private void connect() {

        try {
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
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NoSuchPortException e) {
            e.printStackTrace();
        } catch (UnsupportedCommOperationException e) {
            e.printStackTrace();
        }
    }

    private void printMessage() {
        System.out.println("Possible Commands:\n exit\n activate\n deactivate\n statusenq");
    }

    public static void main(String[] args) {
        ConsoleInput consoleInput = new ConsoleInput();
        Thread thread = new Thread(consoleInput, "Console Input");
        thread.start();
    }


}
