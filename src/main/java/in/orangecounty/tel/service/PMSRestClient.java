package in.orangecounty.tel.service;

import java.util.Map;

/**
 * Created by jamsheer on 3/31/15.
 */
public interface PMSRestClient {
    Map<Long,Map<String,String>> getExtensions();

    void updateCallCharges(Map<String, Map<String, String>> stringMapMap);
}
