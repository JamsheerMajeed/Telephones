package in.orangecounty.tel.impl;

import in.orangecounty.tel.service.PMSRestClient;
import in.orangecounty.tel.service.impl.PMSRestClientImpl;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * Created by jamsheer on 3/16/15.
 */
public class NEAX7400PmsProtocolImpl {

    private static final Logger log = LoggerFactory.getLogger(NEAX7400PmsProtocolImpl.class);
    DataLinkProtocolImpl dataLinkProtocol = new DataLinkProtocolImpl();
    ScheduledExecutorService extensionScheduler = Executors.newScheduledThreadPool(1);
    ScheduledFuture extensionFuture;
    private PMSRestClient pmsRestClient;
    public void checkIn(String guestName, String extension) {
        setRestriction(extension,"0");
        setName(extension,guestName);
    }

    public void checkOut(String extension) {
        setRestriction(extension,"1");
        setName(extension," ");
    }




    private void setExtensionProperies(Map<Long, Map<String, String>> extensions) {
//        System.out.println("\n\n extensions  --- "+extensions);
        String restrictionLevel="0";
        String restrictionStatus = "";
        for(Map.Entry<Long,Map<String,String>> outerEntry : extensions.entrySet()){
            for (Map.Entry<String,String> innerEntry : outerEntry.getValue().entrySet()){

                restrictionStatus = innerEntry.getKey();
                if(innerEntry.getKey().trim().equals("true")){
                    restrictionLevel="0";
                }
                else if (restrictionStatus.trim().equals("false")){
                    restrictionLevel="1";
                }
                setName(outerEntry.getKey().toString(),innerEntry.getValue());
                setRestriction(outerEntry.getKey().toString(),restrictionLevel);
            }
        }
    }


    public void sync() {
        CSVParser csvParser = null;

//     String filename ="/home/jamsheer/Desktop/final/kabini_extension_2.csv";
        String filename = "/home/jamsheer/sample_data/kabini_extension_2.csv";
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

    /* 0 - No restriction
    *  1 - Outward Restriction */
    public void setRestriction(String extension, String status) {
        System.out.println("Set restriction "+extension+" to "+status);
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

        System.out.println("set name "+extension+" to "+name);
        String extensionName = modifyName(name);
        String ext = modifyExtension(extension);
        StringBuilder sb = new StringBuilder("1!L21266");
        sb.append(ext);
        sb.append("  ");
        sb.append(extensionName);
        dataLinkProtocol.sendMessage(sb.toString());
    }

    private String modifyExtension(String extension) {
        extension=extension.trim();
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
        log.warn("calling neax start ---- ");
       dataLinkProtocol.start();
       dataLinkProtocol.sendStatus();
        pmsRestClient = new PMSRestClientImpl();
        extensionFuture = extensionScheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                System.out.println("\n\n---- get extensions --"+Calendar.getInstance().getTime()+"---\n\n");
                    setExtensionProperies(pmsRestClient.getExtensions());
            }
        },0,2, TimeUnit.MINUTES);
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
