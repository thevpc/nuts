package net.vpc.toolbox.tomcat.local.config;

import net.vpc.toolbox.tomcat.util.TomcatUtils;

import java.util.HashMap;
import java.util.Map;

public class LocalTomcatConfig {
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
    private int startupWaitTime = 20;
    private int shutdownWaitTime = 20;
    private boolean kill = true;
    private Map<String, LocalTomcatAppConfig> apps = new HashMap<>();
    private Map<String, LocalTomcatDomainConfig> domains = new HashMap<>();

    public String getCatalinaVersion() {
        return catalinaVersion;
    }

    public LocalTomcatConfig setCatalinaVersion(String catalinaVersion) {
        this.catalinaVersion = catalinaVersion;
        return this;
    }

    public String getCatalinaBase() {
        return catalinaBase;
    }

    public LocalTomcatConfig setCatalinaBase(String catalinaBase) {
        this.catalinaBase = catalinaBase;
        return this;
    }

    public int getShutdownWaitTime() {
        return shutdownWaitTime;
    }

    public LocalTomcatConfig setShutdownWaitTime(int shutdownWaitTime) {
        this.shutdownWaitTime = shutdownWaitTime;
        return this;
    }

    public String getArchiveFolder() {
        return archiveFolder;
    }

    public LocalTomcatConfig setArchiveFolder(String archiveFolder) {
        this.archiveFolder = archiveFolder;
        return this;
    }

    public String getRunningFolder() {
        return runningFolder;
    }

    public LocalTomcatConfig setRunningFolder(String runningFolder) {
        this.runningFolder = runningFolder;
        return this;
    }

    public Map<String, LocalTomcatAppConfig> getApps() {
        return apps;
    }

    public LocalTomcatConfig setApps(Map<String, LocalTomcatAppConfig> apps) {
        this.apps = apps;
        return this;
    }

    public Map<String, LocalTomcatDomainConfig> getDomains() {
        return domains;
    }

    public LocalTomcatConfig setDomains(Map<String, LocalTomcatDomainConfig> domains) {
        this.domains = domains;
        return this;
    }

    public String getStartupMessage() {
        return startupMessage;
    }

    public LocalTomcatConfig setStartupMessage(String startupMessage) {
        this.startupMessage = startupMessage;
        return this;
    }

    public String getShutdownMessage() {
        return shutdownMessage;
    }

    public LocalTomcatConfig setShutdownMessage(String shutdownMessage) {
        this.shutdownMessage = shutdownMessage;
        return this;
    }

    public String getLogFile() {
        return logFile;
    }

    public LocalTomcatConfig setLogFile(String logFile) {
        this.logFile = logFile;
        return this;
    }

    public String getJavaHome() {
        return javaHome;
    }

    public LocalTomcatConfig setJavaHome(String javaHome) {
        this.javaHome = javaHome;
        return this;
    }

    public int getStartupWaitTime() {
        return startupWaitTime;
    }

    public LocalTomcatConfig setStartupWaitTime(int startupWaitTime) {
        this.startupWaitTime = startupWaitTime;
        return this;
    }

    public boolean isKill() {
        return kill;
    }

    public LocalTomcatConfig setKill(boolean kill) {
        this.kill = kill;
        return this;
    }

    public String getJavaOptions() {
        return javaOptions;
    }

    public LocalTomcatConfig setJavaOptions(String javaOptions) {
        this.javaOptions = javaOptions;
        return this;
    }

    public String getCatalinaHome() {
        return catalinaHome;
    }

    public LocalTomcatConfig setCatalinaHome(String catalinaHome) {
        this.catalinaHome = catalinaHome;
        return this;
    }

    @Override
    public String toString() {
        return "LocalTomcatConfig{" +
                "catalinaVersion=" + TomcatUtils.toJsonString(catalinaVersion) +
                ", catalinaBase=" + TomcatUtils.toJsonString(catalinaBase) +
                ", catalinaHome=" + TomcatUtils.toJsonString(catalinaHome) +
                ", archiveFolder=" + TomcatUtils.toJsonString(archiveFolder) +
                ", runningFolder=" + TomcatUtils.toJsonString(runningFolder) +
                ", startupMessage=" + TomcatUtils.toJsonString(startupMessage) +
                ", shutdownMessage=" + TomcatUtils.toJsonString(shutdownMessage) +
                ", logFile=" + TomcatUtils.toJsonString(logFile) +
                ", javaHome=" + TomcatUtils.toJsonString(javaHome) +
                ", javaOptions=" + TomcatUtils.toJsonString(javaOptions) +
                ", startupWaitTime=" + TomcatUtils.toJsonString(startupWaitTime) +
                ", shutdownWaitTime=" + TomcatUtils.toJsonString(shutdownWaitTime) +
                ", kill=" + TomcatUtils.toJsonString(kill) +
                ", apps=" + TomcatUtils.toJsonString(apps) +
                ", domains=" + TomcatUtils.toJsonString(domains) +
                '}';
    }
}
