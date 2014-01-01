package in.orangecounty.cli;

import in.orangecounty.DriverController;
import in.orangecounty.impl.DriverControllerImpl;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by thomas on 9/12/13.
 * The ConsoleInput Class
 */
public class ConsoleInput implements Runnable {
    Logger log = LoggerFactory.getLogger(ConsoleInput.class);
    private boolean running = false;
    DriverController driverController = new DriverControllerImpl();

    private enum Command{
        exit, checkin, checkout, sync, start;
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
                    input = result.get(60, TimeUnit.SECONDS);
                    break;
                } catch (ExecutionException e) {
                    e.getCause().printStackTrace();
                } catch (TimeoutException e) {
                    result.cancel(true);
                    log.debug("Thread cancelled. input is null");
                } catch (InterruptedException e) {
                    log.debug("InterruptedException", e);
                    break;
                }
                Thread.yield();
            }
        } finally {
            ex.shutdownNow();
        }
        return input;
    }

    public void run() {
        running = true;
        driverController.start();
        while (running) {
            process(readLine());
        }
    }

    private void process(String input) {
        List<String> list = new ArrayList<String>();
        Matcher m = Pattern.compile("([^\"]\\S*|\".+?\")\\s*").matcher(input);
        while (m.find()){
            list.add(m.group(1).replace("\"", "")); // Add .replace("\"", "") to remove surrounding quotes.
        }
        Command command = null;
        try {
            command = Command.valueOf(list.get(0));
        } catch (IllegalArgumentException e) {
            System.out.printf("\n%s is not a recognised command\n\n", list.get(0));
            printMessage();
            return;
        }
        switch(command){
            case exit:
                driverController.stop();
                running = false;
                break;
            case checkin:
                //Not Checking Input
                if (list.size()>2) {
                    driverController.checkIn(list.get(1), list.get(2));
                } else {
                    System.out.println("\nUsage: checkin <extension> <Guest Name>\n");
                }
                break;
            case checkout:
                if (list.size()>1) {
                    driverController.checkOut(list.get(1));
                } else {
                    System.out.println("\nUsage: checkout <extension>\n");
                }
                break;
            case sync:
                Map<String, String> extensions = new HashMap<String, String>();
                for(String item : list){
                    if(item.matches("^[^:]+:[^:]+$")){
                        String[] keyValue = item.split(":");
                        extensions.put(keyValue[0], keyValue[1]);
                    }
                }
                driverController.sync(extensions);
                break;
            case start:
                driverController.start();
                break;
            default:
                printMessage();
                break;
        }
    }

    private void printMessage() {
        System.out.println("Possible Commands:");
        for(Command c : Command.values()){
            System.out.println(c.name());
        }
        System.out.println();
    }

    public static void main(String[] args) {
        ConsoleInput consoleInput = new ConsoleInput();
        Thread thread = new Thread(consoleInput, "Console Input");
        thread.start();
    }


}
