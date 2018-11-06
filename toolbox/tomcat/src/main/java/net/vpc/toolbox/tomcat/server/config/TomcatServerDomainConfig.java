package net.vpc.toolbox.tomcat.server.config;

public class TomcatServerDomainConfig {
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
}
