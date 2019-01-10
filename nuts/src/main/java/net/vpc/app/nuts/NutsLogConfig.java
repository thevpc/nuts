package net.vpc.app.nuts;

import java.util.logging.Level;

public class NutsLogConfig {
    private Level logLevel = null;
    private int logSize = 0;
    private int logCount = 0;
    private String logName = null;
    private String logFolder = null;
    private boolean logInherited = false;

    public Level getLogLevel() {
        return logLevel;
    }

    public NutsLogConfig setLogLevel(Level logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    public int getLogSize() {
        return logSize;
    }

    public NutsLogConfig setLogSize(int logSize) {
        this.logSize = logSize;
        return this;
    }

    public int getLogCount() {
        return logCount;
    }

    public NutsLogConfig setLogCount(int logCount) {
        this.logCount = logCount;
        return this;
    }

    public String getLogName() {
        return logName;
    }

    public NutsLogConfig setLogName(String logName) {
        this.logName = logName;
        return this;
    }

    public String getLogFolder() {
        return logFolder;
    }

    public NutsLogConfig setLogFolder(String logFolder) {
        this.logFolder = logFolder;
        return this;
    }

    public boolean isLogInherited() {
        return logInherited;
    }

    public NutsLogConfig setLogInherited(boolean logInherited) {
        this.logInherited = logInherited;
        return this;
    }
}
