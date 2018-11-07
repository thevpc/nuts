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
    private String runtimeId;
    private String runtimeSourceURL;
    private String logFolder = null;
    private Level logLevel = null;
    private int logSize = 0;
    private int logCount = 0;
    private String[] bootArguments;
    private String[] applicationArguments;
    private boolean perf = false;

    private NutsClassLoaderProvider classLoaderProvider;

    public String getHome() {
        return home;
    }

    public NutsBootOptions setHome(String home) {
        this.home = home;
        return this;
    }

    public String getRuntimeId() {
        return runtimeId;
    }

    public NutsBootOptions setRuntimeId(String runtimeId) {
        this.runtimeId = runtimeId;
        return this;
    }

    public String getRuntimeSourceURL() {
        return runtimeSourceURL;
    }

    public NutsBootOptions setRuntimeSourceURL(String runtimeSourceURL) {
        this.runtimeSourceURL = runtimeSourceURL;
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
        if (runtimeId != null) {
            if (!empty) {
                sb.append(", ");
            }
            sb.append("runtimeId=").append(runtimeId);
            empty = false;
        }
        if (runtimeSourceURL != null) {
            if (!empty) {
                sb.append(", ");
            }
            sb.append("runtimeSourceURL=").append(runtimeSourceURL);
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

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 41 * hash + Objects.hashCode(this.home);
        hash = 41 * hash + Objects.hashCode(this.runtimeId);
        hash = 41 * hash + Objects.hashCode(this.runtimeSourceURL);
        hash = 41 * hash + Objects.hashCode(this.classLoaderProvider);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NutsBootOptions other = (NutsBootOptions) obj;
        if (!Objects.equals(this.home, other.home)) {
            return false;
        }
        if (!Objects.equals(this.runtimeId, other.runtimeId)) {
            return false;
        }
        if (!Objects.equals(this.runtimeSourceURL, other.runtimeSourceURL)) {
            return false;
        }
        if (!Objects.equals(this.classLoaderProvider, other.classLoaderProvider)) {
            return false;
        }
        return true;
    }

    public NutsBootOptions copy() {
        try {
            NutsBootOptions t = (NutsBootOptions) clone();
            t.setBootArguments(t.getBootArguments() == null ? null : Arrays.copyOf(t.getBootArguments(), t.getBootArguments().length));
            t.setBootArguments(t.getApplicationArguments() == null ? null : Arrays.copyOf(t.getApplicationArguments(), t.getApplicationArguments().length));
            return t;
        } catch (CloneNotSupportedException e) {
            throw new NutsUnsupportedOperationException("Should never Happen", e);
        }
    }
}
