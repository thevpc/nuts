package net.vpc.toolbox.tomcat.server.config;

import java.util.HashMap;
import java.util.Map;

public class TomcatServerConfig {
    private String catalinaVersion;
    private String catalinaBase;
    private String archiveFolder;
    private String runningFolder;
    private String startupMessage;
    private String shutdownMessage;
    private String logFile;
    private String javaHome;
    private int startupWaitTime=20;
    private int shutdownWaitTime=20;
    private boolean kill=true;
    private Map<String,TomcatServerAppConfig> apps=new HashMap<>();
    private Map<String,TomcatServerDomainConfig> domains=new HashMap<>();

    public String getCatalinaVersion() {
        return catalinaVersion;
    }

    public TomcatServerConfig setCatalinaVersion(String catalinaVersion) {
        this.catalinaVersion = catalinaVersion;
        return this;
    }

    public String getCatalinaBase() {
        return catalinaBase;
    }

    public TomcatServerConfig setCatalinaBase(String catalinaBase) {
        this.catalinaBase = catalinaBase;
        return this;
    }

    public int getShutdownWaitTime() {
        return shutdownWaitTime;
    }

    public TomcatServerConfig setShutdownWaitTime(int shutdownWaitTime) {
        this.shutdownWaitTime = shutdownWaitTime;
        return this;
    }

    public String getArchiveFolder() {
        return archiveFolder;
    }

    public TomcatServerConfig setArchiveFolder(String archiveFolder) {
        this.archiveFolder = archiveFolder;
        return this;
    }

    public String getRunningFolder() {
        return runningFolder;
    }

    public TomcatServerConfig setRunningFolder(String runningFolder) {
        this.runningFolder = runningFolder;
        return this;
    }

    public Map<String, TomcatServerAppConfig> getApps() {
        return apps;
    }

    public void setApps(Map<String, TomcatServerAppConfig> apps) {
        this.apps = apps;
    }

    public Map<String, TomcatServerDomainConfig> getDomains() {
        return domains;
    }

    public void setDomains(Map<String, TomcatServerDomainConfig> domains) {
        this.domains = domains;
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

    public String getJavaHome() {
        return javaHome;
    }

    public void setJavaHome(String javaHome) {
        this.javaHome = javaHome;
    }

    public int getStartupWaitTime() {
        return startupWaitTime;
    }

    public void setStartupWaitTime(int startupWaitTime) {
        this.startupWaitTime = startupWaitTime;
    }

    public boolean isKill() {
        return kill;
    }

    public void setKill(boolean kill) {
        this.kill = kill;
    }
}
