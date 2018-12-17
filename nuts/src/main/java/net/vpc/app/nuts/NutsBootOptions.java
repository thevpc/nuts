/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;
import java.util.logging.Level;

/**
 *
 * @author vpc
 */
public final class NutsBootOptions implements Serializable, Cloneable {

    private String home;
    private String bootRuntime;
    private String bootJavaCommand;
    private String bootJavaOptions;
    private String bootRuntimeSourceURL;
    private String logFolder = null;
    private String logName = null;
    private Level logLevel = null;
    private int logSize = 0;
    private int logCount = 0;
    private String[] bootArguments;
    private String[] applicationArguments;
    private boolean perf = false;

    private NutsClassLoaderProvider classLoaderProvider;

    public String getLogName() {
        return logName;
    }

    public void setLogName(String logName) {
        this.logName = logName;
    }

    public String getHome() {
        return home;
    }

    public NutsBootOptions setHome(String home) {
        this.home = home;
        return this;
    }

    public String getBootRuntime() {
        return bootRuntime;
    }

    public NutsBootOptions setBootRuntime(String bootRuntime) {
        this.bootRuntime = bootRuntime;
        return this;
    }

    public String getBootRuntimeSourceURL() {
        return bootRuntimeSourceURL;
    }

    public NutsBootOptions setBootRuntimeSourceURL(String bootRuntimeSourceURL) {
        this.bootRuntimeSourceURL = bootRuntimeSourceURL;
        return this;
    }

    public NutsClassLoaderProvider getClassLoaderProvider() {
        return classLoaderProvider;
    }

    public NutsBootOptions setClassLoaderProvider(NutsClassLoaderProvider provider) {
        this.classLoaderProvider = provider;
        return this;
    }

    public String getLogFolder() {
        return logFolder;
    }

    public NutsBootOptions setLogFolder(String logFolder) {
        this.logFolder = logFolder;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("NutsBootOptions(");
        boolean empty = true;
        if (home != null) {
            sb.append("home=").append(home);
            empty = false;
        }
        if (bootRuntime != null) {
            if (!empty) {
                sb.append(", ");
            }
            sb.append("bootRuntime=").append(bootRuntime);
            empty = false;
        }
        if (bootRuntimeSourceURL != null) {
            if (!empty) {
                sb.append(", ");
            }
            sb.append("bootRuntimeSourceURL=").append(bootRuntimeSourceURL);
            empty = false;
        }
        if (classLoaderProvider != null) {
            if (!empty) {
                sb.append(", ");
            }
            sb.append("classLoaderProvider=").append(classLoaderProvider);
            empty = false;
        }
        sb.append(")");
        return sb.toString();
    }

    public Level getLogLevel() {
        return logLevel;
    }

    public NutsBootOptions setLogLevel(Level logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    public int getLogSize() {
        return logSize;
    }

    public NutsBootOptions setLogSize(int logSize) {
        this.logSize = logSize;
        return this;
    }

    public int getLogCount() {
        return logCount;
    }

    public NutsBootOptions setLogCount(int logCount) {
        this.logCount = logCount;
        return this;
    }

    public String getBootArgumentsString() {
        if(bootArguments==null){
            return "";
        }
        return NutsArgumentsParser.compressBootArguments(bootArguments);
    }

    public String[] getBootArguments() {
        return bootArguments;
    }

    public NutsBootOptions setBootArguments(String[] bootArguments) {
        this.bootArguments = bootArguments;
        return this;
    }

    public String[] getApplicationArguments() {
        return applicationArguments;
    }

    public NutsBootOptions setApplicationArguments(String[] applicationArguments) {
        this.applicationArguments = applicationArguments;
        return this;
    }

    public boolean isPerf() {
        return perf;
    }

    public NutsBootOptions setPerf(boolean perf) {
        this.perf = perf;
        return this;
    }

    public String getBootJavaCommand() {
        return bootJavaCommand;
    }

    public NutsBootOptions setBootJavaCommand(String bootJavaCommand) {
        this.bootJavaCommand = bootJavaCommand;
        return this;
    }

    public String getBootJavaOptions() {
        return bootJavaOptions;
    }

    public NutsBootOptions setBootJavaOptions(String bootJavaOptions) {
        this.bootJavaOptions = bootJavaOptions;
        return this;
    }

    public NutsBootOptions copy() {
        try {
            NutsBootOptions t = (NutsBootOptions) clone();
            t.setBootArguments(t.getBootArguments() == null ? null : Arrays.copyOf(t.getBootArguments(), t.getBootArguments().length));
            t.setApplicationArguments(t.getApplicationArguments() == null ? null : Arrays.copyOf(t.getApplicationArguments(), t.getApplicationArguments().length));
            return t;
        } catch (CloneNotSupportedException e) {
            throw new NutsUnsupportedOperationException("Should never Happen", e);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsBootOptions that = (NutsBootOptions) o;
        return logSize == that.logSize &&
                logCount == that.logCount &&
                perf == that.perf &&
                Objects.equals(home, that.home) &&
                Objects.equals(bootRuntime, that.bootRuntime) &&
                Objects.equals(bootJavaCommand, that.bootJavaCommand) &&
                Objects.equals(bootJavaOptions, that.bootJavaOptions) &&
                Objects.equals(bootRuntimeSourceURL, that.bootRuntimeSourceURL) &&
                Objects.equals(logFolder, that.logFolder) &&
                Objects.equals(logName, that.logName) &&
                Objects.equals(logLevel, that.logLevel) &&
                Arrays.equals(bootArguments, that.bootArguments) &&
                Arrays.equals(applicationArguments, that.applicationArguments) &&
                Objects.equals(classLoaderProvider, that.classLoaderProvider);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(home, bootRuntime, bootJavaCommand, bootJavaOptions, bootRuntimeSourceURL, logFolder, logName, logLevel, logSize, logCount, perf, classLoaderProvider);
        result = 31 * result + Arrays.hashCode(bootArguments);
        result = 31 * result + Arrays.hashCode(applicationArguments);
        return result;
    }
}
