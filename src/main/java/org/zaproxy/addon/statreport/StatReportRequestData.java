package org.zaproxy.addon.statreport;

import java.util.Objects;

public class StatReportRequestData {
    private String requestUrl;
    private String requestHost;
    private int requestPort;
    private String requestPath;
    private String requestMethod;
    private String requestQuery;
    private String requestBody;

    public StatReportRequestData(String requestUrl,
                                 String requestHost,
                                 int requestPort,
                                 String requestPath,
                                 String requestMethod,
                                 String requestQuery,
                                 String requestBody) {
        this.requestUrl = requestUrl;
        this.requestHost = requestHost;
        this.requestPort = requestPort;
        this.requestPath = requestPath;
        this.requestMethod = requestMethod;
        this.requestQuery = requestQuery;
        this.requestBody = requestBody;
    }

    public String getRequestUrl() {
        return requestUrl;
    }

    public void setRequestUrl(String requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getRequestHost() {
        return requestHost;
    }

    public void setRequestHost(String requestHost) {
        this.requestHost = requestHost;
    }

    public int getRequestPort() {
        return requestPort;
    }

    public void setRequestPort(int requestPort) {
        this.requestPort = requestPort;
    }

    public String getRequestPath() {
        return requestPath;
    }

    public void setRequestPath(String requestPath) {
        this.requestPath = requestPath;
    }

    public String getRequestMethod() {
        return requestMethod;
    }

    public void setRequestMethod(String requestMethod) {
        this.requestMethod = requestMethod;
    }

    public String getRequestQuery() {
        return requestQuery;
    }

    public void setRequestQuery(String requestQuery) {
        this.requestQuery = requestQuery;
    }

    public String getRequestBody() {
        return requestBody;
    }

    public void setRequestBody(String requestBody) {
        this.requestBody = requestBody;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StatReportRequestData that = (StatReportRequestData) o;
        return requestPort == that.requestPort &&
                requestHost.equals(that.requestHost) &&
                requestPath.equals(that.requestPath);
    }

    @Override
    public int hashCode() {
        return Objects.hash(requestHost, requestPort, requestPath);
    }
}
