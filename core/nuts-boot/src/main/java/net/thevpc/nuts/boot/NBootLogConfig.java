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
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.boot;

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
public class NBootLogConfig implements Serializable, Cloneable {
    private static final long serialVersionUID = 1;
    public static NBootLogConfig BLANK=new NBootLogConfig().readOnly();

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
    private boolean readOnly;

    public NBootLogConfig() {
    }

    public NBootLogConfig(NBootLogConfig other, boolean readOnly) {
        this.readOnly=readOnly;
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

    public NBootLogConfig setLogFileFilter(Filter logFileFilter) {
        if(readOnly){
            throw new IllegalArgumentException("read only");
        }
        this.logFileFilter = logFileFilter;
        return this;
    }

    public Filter getLogTermFilter() {
        return logTermFilter;
    }

    public NBootLogConfig setLogTermFilter(Filter logTermFilter) {
        if(readOnly){
            throw new IllegalArgumentException("read only");
        }
        this.logTermFilter = logTermFilter;
        return this;
    }

    public Level getLogFileLevel() {
        return logFileLevel;
    }

    public NBootLogConfig setLogFileLevel(Level logFileLevel) {
        if(readOnly){
            throw new IllegalArgumentException("read only");
        }
        this.logFileLevel = logFileLevel;
        return this;
    }

    public Level getLogTermLevel() {
        return logTermLevel;
    }

    public NBootLogConfig setLogTermLevel(Level logTermLevel) {
        if(readOnly){
            throw new IllegalArgumentException("read only");
        }
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
    public NBootLogConfig setLogFileSize(int logFileSize) {
        if(readOnly){
            throw new IllegalArgumentException("read only");
        }
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
    public NBootLogConfig setLogFileCount(int logFileCount) {
        if(readOnly){
            throw new IllegalArgumentException("read only");
        }
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
    public NBootLogConfig setLogFileName(String logFileName) {
        if(readOnly){
            throw new IllegalArgumentException("read only");
        }
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
    public NBootLogConfig setLogFileBase(String logFileBase) {
        if(readOnly){
            throw new IllegalArgumentException("read only");
        }
        this.logFileBase = logFileBase;
        return this;
    }

    public NBootLogConfig copy() {
        return new NBootLogConfig(this,false);
    }

    public NBootLogConfig readOnly() {
        if(readOnly) {
            return this;
        }
        return new NBootLogConfig(this,true);
    }

    @Override
    public int hashCode() {
        return Objects.hash(logFileLevel, logTermLevel, logFileSize, logFileCount, logFileName, logFileBase);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NBootLogConfig that = (NBootLogConfig) o;
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
                ", readOnly=" + readOnly  +
                '}';
    }
}
