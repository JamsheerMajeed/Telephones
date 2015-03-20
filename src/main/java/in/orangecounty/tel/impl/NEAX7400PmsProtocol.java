package in.orangecounty.tel.impl;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * Created by jamsheer on 3/16/15.
 */
public class NEAX7400PmsProtocol {

    public void checkIn(String guestName, String extension) {
        setRestriction(extension,"0");
        setName(extension,guestName);
    }

    public void checkOut(String extension) {
        setRestriction(extension,"1");
        setName(extension," ");
    }

    public void parseCallDetails(String message) {
        System.out.println("message in parse -- "+message.toString());
     int stationNumber,routeNumber,trunkNumber,subscriberNumber,hour,minute,second,duration;
        stationNumber = Integer.parseInt(message.substring(0,4));
        routeNumber = Integer.parseInt(message.substring(6,9));
        trunkNumber = Integer.parseInt(message.substring(9,12));
        subscriberNumber = Integer.parseInt(message.substring(12,28));
        hour = Integer.parseInt(message.substring(28,30));
        minute = Integer.parseInt(message.substring(30,32));
        second = Integer.parseInt(message.substring(32,34));
        duration = Integer.parseInt(message.substring(34,39));

        System.out.println("Station Number"+stationNumber);
        System.out.println("Route Number"+routeNumber);
        System.out.println("Trunk Number"+trunkNumber);
        System.out.println("Subscriber Number"+subscriberNumber);
        System.out.println("Hour"+hour);
        System.out.println("Minute"+minute);
        System.out.println("Second"+second);
        System.out.println("Duration"+duration);
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

    private void setRestriction(String extension, String status) {
        StringBuilder sb = new StringBuilder("1!L15141");
        String ext = modifyExtension(extension);
        String st = status.trim();
        sb.append(ext);
        sb.append("  ");
        sb.append(st);
        sb.append("  ");

    }

    private void setName(String extension, String name) {

        String extensionName = modifyName(name);
        String ext = modifyExtension(extension);
        StringBuilder sb = new StringBuilder("1!L21266");
        sb.append(ext);
        sb.append("  ");
        sb.append(extensionName);

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


}
