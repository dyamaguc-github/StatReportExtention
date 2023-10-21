package org.zaproxy.addon.statreport;

import org.parosproxy.paros.core.scanner.Plugin;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StatReportStatData {

    private String requestContext;
    private StatReportRequestData requestData;

    private Map<String,Integer> alartCount = new HashMap<>();

    public StatReportStatData() {}

    public StatReportStatData(String requestContext, StatReportRequestData requestData, List<Plugin> scanPolicies) {
        this.requestContext = requestContext;
        this.requestData = requestData;
        this.initAlartCount(scanPolicies);
    }

    private void initAlartCount(List<Plugin> plugins){
        this.alartCount.clear();
        for(Plugin p : plugins){
            this.alartCount.put(p.getName(),0);
        }
        this.alartCount.put("Others",0);
    }

    public void inclementAlartCount(String key){
        var currentCount = this.getAlartCount().get(key);
        this.getAlartCount().replace(key, currentCount+1);
    }

    public String getRequestContext() {
        return requestContext;
    }

    public void setRequestContext(String requestContext) {
        this.requestContext = requestContext;
    }

    public StatReportRequestData getRequestData() {
        return requestData;
    }

    public void setRequestData(StatReportRequestData requestData) {
        this.requestData = requestData;
    }

    public Map<String, Integer> getAlartCount() {
        return alartCount;
    }

    public void setAlartCount(Map<String, Integer> alartCount) {
        this.alartCount = alartCount;
    }
}
