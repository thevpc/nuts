package net.thevpc.nuts.toolbox.ndb.sql.nmysql.local.config;

import java.util.HashMap;
import java.util.Map;

public class LocalMysqlConfig {

    private String backupFolder;
    private String runningFolder;
    private String logFile;
    private String mysqlCommand;
    private String mysqldumpCommand;
    private int startupWaitTime = 20;
    private int shutdownWaitTime = 20;
    private boolean kill = true;
    private Map<String, LocalMysqlDatabaseConfig> databases = new HashMap<>();

    public int getShutdownWaitTime() {
        return shutdownWaitTime;
    }

    public LocalMysqlConfig setShutdownWaitTime(int shutdownWaitTime) {
        this.shutdownWaitTime = shutdownWaitTime;
        return this;
    }

    public String getBackupFolder() {
        return backupFolder;
    }

    public LocalMysqlConfig setBackupFolder(String archiveFolder) {
        this.backupFolder = archiveFolder;
        return this;
    }

    public String getRunningFolder() {
        return runningFolder;
    }

    public LocalMysqlConfig setRunningFolder(String runningFolder) {
        this.runningFolder = runningFolder;
        return this;
    }

    public Map<String, LocalMysqlDatabaseConfig> getDatabases() {
        return databases;
    }

    public LocalMysqlConfig setDatabases(Map<String, LocalMysqlDatabaseConfig> apps) {
        this.databases = apps;
        return this;
    }

    public String getLogFile() {
        return logFile;
    }

    public LocalMysqlConfig setLogFile(String logFile) {
        this.logFile = logFile;
        return this;
    }

    public int getStartupWaitTime() {
        return startupWaitTime;
    }

    public LocalMysqlConfig setStartupWaitTime(int startupWaitTime) {
        this.startupWaitTime = startupWaitTime;
        return this;
    }

    public boolean isKill() {
        return kill;
    }

    public LocalMysqlConfig setKill(boolean kill) {
        this.kill = kill;
        return this;
    }

    public String getMysqlCommand() {
        return mysqlCommand;
    }

    public void setMysqlCommand(String mysqlCommand) {
        this.mysqlCommand = mysqlCommand;
    }

    public String getMysqldumpCommand() {
        return mysqldumpCommand;
    }

    public void setMysqldumpCommand(String mysqldumpCommand) {
        this.mysqldumpCommand = mysqldumpCommand;
    }
}
