package net.vpc.toolbox.tomcat.local.config;

import net.vpc.toolbox.tomcat.util.TomcatUtils;

public class LocalTomcatAppConfig {
    private String sourceFilePath;
    private String deployName;
    private String startupMessage;
    private String shutdownMessage;
    private String domain;
    private String logFile;


    public String getSourceFilePath() {
        return sourceFilePath;
    }

    public void setSourceFilePath(String sourceFilePath) {
        this.sourceFilePath = sourceFilePath;
    }

    public String getDeployName() {
        return deployName;
    }

    public void setDeployName(String deployName) {
        this.deployName = deployName;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getStartupMessage() {
        return startupMessage;
    }

    public void setStartupMessage(String startupMessage) {
        this.startupMessage = startupMessage;
    }

    public String getShutdownMessage() {
        return shutdownMessage;
    }

    public void setShutdownMessage(String shutdownMessage) {
        this.shutdownMessage = shutdownMessage;
    }

    public String getLogFile() {
        return logFile;
    }

    public void setLogFile(String logFile) {
        this.logFile = logFile;
    }

    @Override
    public String toString() {
        return "{" +
                "sourceFilePath=" + TomcatUtils.toJsonString(sourceFilePath) +
                ", deployName=" + TomcatUtils.toJsonString(deployName) +
                ", startupMessage=" + TomcatUtils.toJsonString(startupMessage) +
                ", shutdownMessage=" + TomcatUtils.toJsonString(shutdownMessage) +
                ", domain=" + TomcatUtils.toJsonString(domain) +
                ", logFile=" + TomcatUtils.toJsonString(logFile) +
                '}';
    }
}
