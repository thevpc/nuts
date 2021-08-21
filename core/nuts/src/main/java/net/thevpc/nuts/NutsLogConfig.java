/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 *
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
 * @author thevpc
 * @since 0.5.4
 * @app.category Logging
 */
public class NutsLogConfig implements Serializable,Cloneable{
    private static final long serialVersionUID = 1;

    private Level logFileLevel = Level.OFF;
    private Filter logFileFilter = null;
    private Level logTermLevel = Level.OFF;
    private Filter logTermFilter = null;
    private int logFileSize = 0;
    private int logFileCount = 0;
    private String logFileName = null;
    private String logFileBase = null;
    @Deprecated
    private boolean logInherited = false;

    public NutsLogConfig() {
    }

    public NutsLogConfig(NutsLogConfig other) {
        if(other!=null){
            this.logFileLevel = other.logFileLevel;
            this.logTermLevel = other.logTermLevel;
            this.logFileFilter = other.logFileFilter;
            this.logTermFilter = other.logTermFilter;
            this.logFileSize = other.logFileSize;
            this.logFileCount = other.logFileCount;
            this.logFileName = other.logFileName;
            this.logFileBase = other.logFileBase;
            this.logInherited = other.logInherited;
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

    public int getLogFileSize() {
        return logFileSize;
    }

    public NutsLogConfig setLogFileSize(int logFileSize) {
        this.logFileSize = logFileSize;
        return this;
    }

    public int getLogFileCount() {
        return logFileCount;
    }

    public NutsLogConfig setLogFileCount(int logFileCount) {
        this.logFileCount = logFileCount;
        return this;
    }

    public String getLogFileName() {
        return logFileName;
    }

    public NutsLogConfig setLogFileName(String logFileName) {
        this.logFileName = logFileName;
        return this;
    }

    public String getLogFileBase() {
        return logFileBase;
    }

    public NutsLogConfig setLogFileBase(String logFileBase) {
        this.logFileBase = logFileBase;
        return this;
    }

    @Deprecated
    public boolean isLogInherited() {
        return logInherited;
    }

    @Deprecated
    public NutsLogConfig setLogInherited(boolean logInherited) {
        this.logInherited = logInherited;
        return this;
    }

    public NutsLogConfig copy(){
        try {
            return (NutsLogConfig) clone();
        } catch (CloneNotSupportedException e) {
            throw new NutsBootException(NutsMessage.plain("unsupported clone"));
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsLogConfig that = (NutsLogConfig) o;
        return logFileSize == that.logFileSize &&
                logFileCount == that.logFileCount &&
                logInherited == that.logInherited &&
                Objects.equals(logFileLevel, that.logFileLevel) &&
                Objects.equals(logTermLevel, that.logTermLevel) &&
                Objects.equals(logFileName, that.logFileName) &&
                Objects.equals(logFileBase, that.logFileBase);
    }

    @Override
    public int hashCode() {
        return Objects.hash(logFileLevel, logTermLevel, logFileSize, logFileCount, logFileName, logFileBase, logInherited);
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
                ", logInherited=" + logInherited +
                '}';
    }
}
