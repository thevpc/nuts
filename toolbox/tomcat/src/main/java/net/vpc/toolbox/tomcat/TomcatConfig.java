package net.vpc.toolbox.tomcat;

import java.util.HashMap;
import java.util.Map;

public class TomcatConfig {
    private String name;
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
    private Map<String,TomcatAppConfig> apps=new HashMap<>();
    private Map<String,TomcatDomainConfig> domains=new HashMap<>();

    public String getCatalinaVersion() {
        return catalinaVersion;
    }

    public TomcatConfig setCatalinaVersion(String catalinaVersion) {
        this.catalinaVersion = catalinaVersion;
        return this;
    }

    public String getName() {
        return name;
    }

    public TomcatConfig setName(String name) {
        this.name = name;
        return this;
    }

    public String getCatalinaBase() {
        return catalinaBase;
    }

    public TomcatConfig setCatalinaBase(String catalinaBase) {
        this.catalinaBase = catalinaBase;
        return this;
    }

    public int getShutdownWaitTime() {
        return shutdownWaitTime;
    }

    public TomcatConfig setShutdownWaitTime(int shutdownWaitTime) {
        this.shutdownWaitTime = shutdownWaitTime;
        return this;
    }

    public String getArchiveFolder() {
        return archiveFolder;
    }

    public TomcatConfig setArchiveFolder(String archiveFolder) {
        this.archiveFolder = archiveFolder;
        return this;
    }

    public String getRunningFolder() {
        return runningFolder;
    }

    public TomcatConfig setRunningFolder(String runningFolder) {
        this.runningFolder = runningFolder;
        return this;
    }

    public Map<String, TomcatAppConfig> getApps() {
        return apps;
    }

    public void setApps(Map<String, TomcatAppConfig> apps) {
        this.apps = apps;
    }

    public Map<String, TomcatDomainConfig> getDomains() {
        return domains;
    }

    public void setDomains(Map<String, TomcatDomainConfig> domains) {
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
