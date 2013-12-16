package in.orangecounty.cli;

import in.orangecounty.TelephoneCommands;
import in.orangecounty.helper.SerialHelper;
import in.orangecounty.impl.ListenerSerialEventImpl;
import in.orangecounty.impl.ListenerThreadImpl;
import in.orangecounty.impl.SenderImpl;
import in.orangecounty.impl.TelephoneCommandImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.TooManyListenersException;
import java.util.concurrent.*;

/**
 * Created by thomas on 9/12/13.
 * The ConsoleInput Class
 */
public class ConsoleInput implements Runnable {
    Logger log = LoggerFactory.getLogger(ConsoleInput.class);
    private boolean running;
    private TelephoneCommands telephoneCommands;
    private SenderImpl sender;
    private ListenerThreadImpl listenerThread;
    SerialHelper serialHelper;

    public void start() {
        try {
            serialHelper = new SerialHelper();
            log.debug("Serial Helper Created");
            serialHelper.connect("/dev/ttyS0");
            log.debug("Serial Connect Called");
            sender = new SenderImpl(serialHelper.getSerialOutputStream());
            log.debug("Sender Created");
            new Thread(new ListenerThreadImpl(serialHelper.getSerialInputStream(), sender)).start();
//            l
//            listenerSerialEvent = new ListenerSerialEventImpl(serialHelper.getSerialInputStream(), sender);
            log.debug("Listener Created");
            telephoneCommands = new TelephoneCommandImpl(sender);
            log.debug("Telephone Command Created");
//            serialHelper.addDataAvailableListener(listenerSerialEvent);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stop() {
        if(sender!=null){
            sender.stop();
        }
        if(telephoneCommands!=null){
            telephoneCommands.stop();
        }
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

    private void printMessage() {
        System.out.println("Possible Commands:\n exit\n activate\n deactivate\n statusenq");
    }

    public static void main(String[] args) {
        ConsoleInput consoleInput = new ConsoleInput();
        Thread thread = new Thread(consoleInput, "Console Input");
        thread.start();
    }



}
