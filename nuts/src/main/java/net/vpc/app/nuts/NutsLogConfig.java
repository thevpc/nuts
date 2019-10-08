/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.io.Serializable;
import java.util.logging.Level;

/**
 *
 * @author vpc
 * @since 0.5.4
 */
public class NutsLogConfig implements Serializable{
    private static final long serialVersionUID = 1;

    private Level logFileLevel = Level.OFF;
    private Level logTermLevel = Level.OFF;
    private int logFileSize = 0;
    private int logFileCount = 0;
    private String logFileName = null;
    private String logFileBase = null;
    @Deprecated
    private boolean logInherited = false;

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
}
