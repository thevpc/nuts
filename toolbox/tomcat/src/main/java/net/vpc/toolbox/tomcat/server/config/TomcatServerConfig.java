package net.vpc.toolbox.tomcat.server.config;

import java.util.HashMap;
import java.util.Map;

public class TomcatServerConfig {
    private String catalinaVersion;
    private String catalinaBase;
    private String catalinaHome;
    private String archiveFolder;
    private String runningFolder;
    private String startupMessage;
    private String shutdownMessage;
    private String logFile;
    private String javaHome;
    private String javaOptions;
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

    public TomcatServerConfig setApps(Map<String, TomcatServerAppConfig> apps) {
        this.apps = apps;
        return this;
    }

    public Map<String, TomcatServerDomainConfig> getDomains() {
        return domains;
    }

    public TomcatServerConfig setDomains(Map<String, TomcatServerDomainConfig> domains) {
        this.domains = domains;
        return this;
    }

    public String getStartupMessage() {
        return startupMessage;
    }

    public TomcatServerConfig setStartupMessage(String startupMessage) {
        this.startupMessage = startupMessage;
        return this;
    }

    public String getShutdownMessage() {
        return shutdownMessage;
    }

    public TomcatServerConfig setShutdownMessage(String shutdownMessage) {
        this.shutdownMessage = shutdownMessage;
        return this;
    }

    public String getLogFile() {
        return logFile;
    }

    public TomcatServerConfig setLogFile(String logFile) {
        this.logFile = logFile;
        return this;
    }

    public String getJavaHome() {
        return javaHome;
    }

    public TomcatServerConfig setJavaHome(String javaHome) {
        this.javaHome = javaHome;
        return this;
    }

    public int getStartupWaitTime() {
        return startupWaitTime;
    }

    public TomcatServerConfig setStartupWaitTime(int startupWaitTime) {
        this.startupWaitTime = startupWaitTime;
        return this;
    }

    public boolean isKill() {
        return kill;
    }

    public TomcatServerConfig setKill(boolean kill) {
        this.kill = kill;
        return this;
    }

    public String getJavaOptions() {
        return javaOptions;
    }

    public TomcatServerConfig setJavaOptions(String javaOptions) {
        this.javaOptions = javaOptions;
        return this;
    }

    public String getCatalinaHome() {
        return catalinaHome;
    }

    public TomcatServerConfig setCatalinaHome(String catalinaHome) {
        this.catalinaHome = catalinaHome;
        return this;
    }
}
