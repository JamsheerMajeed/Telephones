package in.orangecounty.tel.service.impl;

import in.orangecounty.tel.service.PMSRestClient;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.type.TypeReference;


import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by jamsheer on 3/31/15.
 */
public class PMSRestClientImpl implements PMSRestClient {
    protected final transient Log log = LogFactory.getLog(getClass());
    private String pmsURL="localhost:8082/services/api/telephoneService/";
    private HttpClient client = new HttpClient();
    private HttpMethod method;
    @Override
    public Map<Long,Map<String,String>> getExtensions() {
        Map<Long,Map<String,String>> map = new HashMap<Long, Map<String, String>>();
        int statusCode = 0;
        String result = null;
//        method = new GetMethod("http://localhost:8082/services/api/telephoneService/activeExtensions");
        method = new GetMethod("http://pmskabini.orangecounty.in/services/api/telephoneService/activeExtensions");
        try{
            statusCode = client.executeMethod(method);
        }
         catch (IOException e) {
            e.printStackTrace();
        }
        ObjectMapper mapper = new ObjectMapper();
        System.out.println("\n\n\n ---- status code ---- "+statusCode+"\n\n\n");
        if(statusCode != HttpStatus.SC_OK){
            log.debug(" login failed "+statusCode);
        }else {
            try {
                result = method.getResponseBodyAsString();
                map = mapper.readValue(result,new TypeReference<HashMap<Long,HashMap<String,String>>>(){});
            } catch (JsonMappingException e) {
                e.printStackTrace();
            } catch (JsonParseException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
//        System.out.println("-- here map "+map);
//       for(Map.Entry<Long,Map<String,String>> rs:map.entrySet()){
//           System.out.println("keys ---"+rs.getKey());
//       }
     return map;

    }

    @Override
    public void updateCallCharges(Map<String, Map<String, String>> item) {
        PostMethod postMethod = new PostMethod("http://pmskabini.orangecounty.in/services/api/telephoneService/updateCallCharges");
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            postMethod.addParameter("callCharges",objectMapper.writeValueAsString(item));
        } catch (IOException e) {
//            e.printStackTrace();
            System.out.println(" Exception ");
        }
        int statusCode = 0;
        String result = null;
        try {
            statusCode = client.executeMethod(postMethod);
        }  catch (IOException e) {
            e.printStackTrace();
        }
        if(statusCode != HttpStatus.SC_OK){
            //log.debug("lo");
            System.out.println("Login failed "+statusCode);
        }else {
            System.out.println(" Posted success fully ");
        }

    }
}
