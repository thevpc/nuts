package net.thevpc.nuts.toolbox.tomcat.local.config;

import net.thevpc.nuts.toolbox.tomcat.util.TomcatUtils;

public class LocalTomcatDomainConfig {

    private String logFile;
    private String startupMessage;
    private String shutdownMessage;
    private String deployPath;

    public String getLogFile() {
        return logFile;
    }

    public void setLogFile(String logFile) {
        this.logFile = logFile;
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

    public String getDeployPath() {
        return deployPath;
    }

    public void setDeployPath(String deployPath) {
        this.deployPath = deployPath;
    }

    @Override
    public String toString() {
        return "{"
                + "logFile=" + TomcatUtils.toJsonString(logFile)
                + ", startupMessage=" + TomcatUtils.toJsonString(startupMessage)
                + ", shutdownMessage=" + TomcatUtils.toJsonString(shutdownMessage)
                + ", deployPath=" + TomcatUtils.toJsonString(deployPath)
                + '}';
    }
}
