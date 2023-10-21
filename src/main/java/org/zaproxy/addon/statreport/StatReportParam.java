package org.zaproxy.addon.statreport;

import org.zaproxy.zap.model.Context;

import java.util.List;

public class StatReportParam {
    private String reportFileEncode;
    private String reportFile;
    private List<Context> reportContext;

    public StatReportParam(String reportFileEncode, String reportFile) {
        this.reportFileEncode = reportFileEncode;
        this.reportFile = reportFile;
    }

    public String getReportFileEncode() {
        return reportFileEncode;
    }

    public void setReportFileEncode(String reportFileEncode) {
        this.reportFileEncode = reportFileEncode;
    }

    public String getReportFile() {
        return reportFile;
    }

    public void setReportFile(String reportFile) {
        this.reportFile = reportFile;
    }

    public List<Context> getReportContext() {
        return reportContext;
    }

    public void setReportContext(List<Context> reportContext) {
        this.reportContext = reportContext;
    }
}
