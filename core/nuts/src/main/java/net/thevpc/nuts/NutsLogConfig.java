/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts;

import java.io.Serializable;
import java.util.Objects;
import java.util.logging.Filter;
import java.util.logging.Level;

/**
 * log configuration for running nuts
 *
 * @author thevpc
 * @app.category Logging
 * @since 0.5.4
 */
public class NutsLogConfig implements Serializable, Cloneable {
    private static final long serialVersionUID = 1;

    private Level logFileLevel = Level.OFF;
    private Filter logFileFilter = null;
    private Level logTermLevel = Level.OFF;
    private Filter logTermFilter = null;
    /**
     * Log File Size in Mega Bytes
     */
    private int logFileSize = 0;
    private int logFileCount = 0;
    private String logFileName = null;
    private String logFileBase = null;

    public NutsLogConfig() {
    }

    public NutsLogConfig(NutsLogConfig other) {
        if (other != null) {
            this.logFileLevel = other.logFileLevel;
            this.logTermLevel = other.logTermLevel;
            this.logFileFilter = other.logFileFilter;
            this.logTermFilter = other.logTermFilter;
            this.logFileSize = other.logFileSize;
            this.logFileCount = other.logFileCount;
            this.logFileName = other.logFileName;
            this.logFileBase = other.logFileBase;
        }
    }

    public Filter getLogFileFilter() {
        return logFileFilter;
    }

    public NutsLogConfig setLogFileFilter(Filter logFileFilter) {
        this.logFileFilter = logFileFilter;
        return this;
    }

    public Filter getLogTermFilter() {
        return logTermFilter;
    }

    public NutsLogConfig setLogTermFilter(Filter logTermFilter) {
        this.logTermFilter = logTermFilter;
        return this;
    }

    public Level getLogFileLevel() {
        return logFileLevel;
    }

    public NutsLogConfig setLogFileLevel(Level logFileLevel) {
        this.logFileLevel = logFileLevel;
        return this;
    }

    public Level getLogTermLevel() {
        return logTermLevel;
    }

    public NutsLogConfig setLogTermLevel(Level logTermLevel) {
        this.logTermLevel = logTermLevel;
        return this;
    }

    /**
     * Log File Size in Mega Bytes
     *
     * @return log file size
     */
    public int getLogFileSize() {
        return logFileSize;
    }

    /**
     * update Log File Size in Mega Bytes
     *
     * @param logFileSize Log File Size in Mega Bytes
     * @return {@code this} instance
     */
    public NutsLogConfig setLogFileSize(int logFileSize) {
        this.logFileSize = logFileSize;
        return this;
    }

    /**
     * Log File rotation count
     *
     * @return log rotation files count
     */
    public int getLogFileCount() {
        return logFileCount;
    }

    /**
     * update Log File rotation count
     *
     * @param logFileCount Log File rotation count
     * @return {@code this} instance
     */
    public NutsLogConfig setLogFileCount(int logFileCount) {
        this.logFileCount = logFileCount;
        return this;
    }

    /**
     * Log File Name pattern
     *
     * @return Log File Name pattern
     */
    public String getLogFileName() {
        return logFileName;
    }

    /**
     * update Log File Name pattern
     *
     * @param logFileName Log File Name pattern
     * @return {@code this} instance
     */
    public NutsLogConfig setLogFileName(String logFileName) {
        this.logFileName = logFileName;
        return this;
    }

    /**
     * Log File Base directory
     *
     * @return Log File Base directory
     */
    public String getLogFileBase() {
        return logFileBase;
    }

    /**
     * update Log File Base directory
     *
     * @param logFileBase Log File Base directory
     * @return {@code this} instance
     */
    public NutsLogConfig setLogFileBase(String logFileBase) {
        this.logFileBase = logFileBase;
        return this;
    }

    public NutsLogConfig copy() {
        try {
            return (NutsLogConfig) clone();
        } catch (CloneNotSupportedException e) {
            throw new NutsBootException(NutsMessage.plain("unsupported clone"));
        }
    }

    @Override
    public int hashCode() {
        return Objects.hash(logFileLevel, logTermLevel, logFileSize, logFileCount, logFileName, logFileBase);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsLogConfig that = (NutsLogConfig) o;
        return logFileSize == that.logFileSize &&
                logFileCount == that.logFileCount &&
                Objects.equals(logFileLevel, that.logFileLevel) &&
                Objects.equals(logTermLevel, that.logTermLevel) &&
                Objects.equals(logFileName, that.logFileName) &&
                Objects.equals(logFileBase, that.logFileBase);
    }

    @Override
    public String toString() {
        return "NutsLogConfig{" +
                "logFileLevel=" + logFileLevel +
                ", logTermLevel=" + logTermLevel +
                ", logFileSize=" + logFileSize +
                ", logFileCount=" + logFileCount +
                ", logFileName='" + logFileName + '\'' +
                ", logFileBase='" + logFileBase + '\'' +
                '}';
    }
}
