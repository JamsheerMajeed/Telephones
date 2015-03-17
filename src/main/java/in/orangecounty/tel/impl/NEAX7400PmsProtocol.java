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

 public void sync(){
     CSVParser csvParser = null;

//     String filename ="/home/jamsheer/Desktop/final/kabini_extension_2.csv";
     String filename ="/home/jamsheer/sample_data/kabini_extension_2.csv";
     System.out.println();
     try {
         csvParser = CSVParser.parse(new File(filename), Charset.defaultCharset(), CSVFormat.EXCEL.withHeader());
         for(CSVRecord csvRecord : csvParser.getRecords()){
             Map<String, String> record = csvRecord.toMap();
             for(String key: record.keySet()){
                 setName(record.get("Extension"),record.get("Name"));
                 setRestriction(record.get("Extension"),record.get("Status"));
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
        while (sb.length() < 4){
            sb.append(" ");
        }
        return sb.toString();
    }

    private String modifyName(String name) {
        String newName;
        StringBuilder sb = new StringBuilder(name);
        if(sb.length() > 15){
            newName = sb.substring(0,16);
        }
        else {
            while (sb.length() < 15){
                sb.append(" ");
            }
            newName = sb.toString();
        }
        return newName;
    }


}
