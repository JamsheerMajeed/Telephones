package in.orangecounty.tel.impl;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by jamsheer on 3/16/15.
 */
public class NEAX7400PmsProtocolImpl {

    DataLinkProtocolImpl dataLinkProtocol = new DataLinkProtocolImpl();

    public void checkIn(String guestName, String extension) {
        setRestriction(extension,"0");
        setName(extension,guestName);
    }

    public void checkOut(String extension) {
        setRestriction(extension,"1");
        setName(extension," ");
    }

    public void parseCallDetails(String message) {
        Calendar cal = Calendar.getInstance();

         cal.set(Calendar.HOUR_OF_DAY, Integer.parseInt(message.substring(28,30)));
         cal.set(Calendar.MINUTE,Integer.parseInt(message.substring(30,32)));
         cal.set(Calendar.SECOND,Integer.parseInt(message.substring(32,34)));
         cal.set(Calendar.MILLISECOND,0);

        String stationNumber,routeNumber,trunkNumber,subscriberNumber,hour,minute,second,duration;
        stationNumber = message.substring(0,4);
        routeNumber = message.substring(6,9);
        trunkNumber = message.substring(9,12);
        subscriberNumber = message.substring(12,28);
        hour = message.substring(28,30);
        minute = message.substring(30,32);
        second = message.substring(32,34);
        duration = message.substring(34,39);


        System.out.println("Station Number"+stationNumber);
        System.out.println("Route Number"+routeNumber);
        System.out.println("Trunk Number"+trunkNumber);
        System.out.println("Subscriber Number"+subscriberNumber);
        System.out.println("Hour"+hour);
        System.out.println("Minute"+minute);
        System.out.println("Second"+second);
        System.out.println("Duration"+duration);
        System.out.println("Start Date "+cal.getTime());
        cal.add(Calendar.DATE,-1);
        System.out.println("Modified date "+cal.getTime());
        System.out.println("Current date "+new Date());
        System.out.println("Compare "+cal.getTime().equals(new Date()));
    }

    public void sync() {
        CSVParser csvParser = null;

//     String filename ="/home/jamsheer/Desktop/final/kabini_extension_2.csv";
        String filename = "/home/jamsheer/sample_data/kabini_extension_2.csv";
        System.out.println();
        try {
            csvParser = CSVParser.parse(new File(filename), Charset.defaultCharset(), CSVFormat.EXCEL.withHeader());
            for (CSVRecord csvRecord : csvParser.getRecords()) {
                Map<String, String> record = csvRecord.toMap();
                for (String key : record.keySet()) {
                    setName(record.get("Extension"), record.get("Name"));
                    setRestriction(record.get("Extension"), record.get("Status"));
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setRestriction(String extension, String status) {
        StringBuilder sb = new StringBuilder("1!L15141");
        String ext = modifyExtension(extension);
        String st = status.trim();
        sb.append(ext);
        sb.append("  ");
        sb.append(st);
        sb.append("  ");
        dataLinkProtocol.sendMessage(sb.toString());
    }

    public void setName(String extension, String name) {

        String extensionName = modifyName(name);
        String ext = modifyExtension(extension);
        StringBuilder sb = new StringBuilder("1!L21266");
        sb.append(ext);
        sb.append("  ");
        sb.append(extensionName);
        dataLinkProtocol.sendMessage(sb.toString());
    }

    private String modifyExtension(String extension) {

        StringBuilder sb = new StringBuilder(extension);
        while (sb.length() < 4) {
            sb.append(" ");
        }
        return sb.toString();
    }

    private String modifyName(String name) {
        String newName;
        StringBuilder sb = new StringBuilder(name);
        if (sb.length() > 15) {
            newName = sb.substring(0, 16);
        } else {
            while (sb.length() < 15) {
                sb.append(" ");
            }
            newName = sb.toString();
        }
        return newName;
    }

    public void start() {
        System.out.println("calling neax start ---- ");
       dataLinkProtocol.start();
       dataLinkProtocol.sendStatus();
       dataLinkProtocol.getExtensions();

    }

    public void stop() {
        System.out.println("--- calling neax stop");
        dataLinkProtocol.stop();
    }

    public void listPorts() {
        dataLinkProtocol.listPorts();
    }

    public void sendMessage(String message) {
        dataLinkProtocol.sendMessage(message);
    }
}
