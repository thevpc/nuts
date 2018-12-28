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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;

/**
 * Created by vpc on 1/23/17.
 */
public final class NutsWorkspaceOptions extends NutsArguments implements Serializable, Cloneable {

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
    private String workspace = null;
    private boolean ignoreIfFound;
    private boolean createIfNotFound;
    private boolean saveIfCreated;
    private String archetype;
    private String[] excludedExtensions;
    private String[] excludedRepositories;
    private String login = null;
    private String password = null;
    private String autoConfig = null;
    private boolean noColors = false;
    private boolean readOnly = false;
    private long creationTime;
    private NutsClassLoaderProvider classLoaderProvider;
    private String[] executorOptions;
    private NutsBootCommand bootCommand = NutsBootCommand.EXEC;

    public String getWorkspace() {
        return workspace;
    }

    public NutsWorkspaceOptions setWorkspace(String workspace) {
        this.workspace = workspace;
        return this;
    }

    public boolean isIgnoreIfFound() {
        return ignoreIfFound;
    }

    public NutsWorkspaceOptions setIgnoreIfFound(boolean ignoreIfFound) {
        this.ignoreIfFound = ignoreIfFound;
        return this;
    }

    public boolean isCreateIfNotFound() {
        return createIfNotFound;
    }

    public NutsWorkspaceOptions setCreateIfNotFound(boolean createIfNotFound) {
        this.createIfNotFound = createIfNotFound;
        return this;
    }

    public boolean isSaveIfCreated() {
        return saveIfCreated;
    }

    public NutsWorkspaceOptions setSaveIfCreated(boolean saveIfCreated) {
        this.saveIfCreated = saveIfCreated;
        return this;
    }

    public String getArchetype() {
        return archetype;
    }

    public NutsWorkspaceOptions setArchetype(String archetype) {
        this.archetype = archetype;
        return this;
    }

    public String[] getExcludedExtensions() {
        return excludedExtensions;
    }

    public NutsWorkspaceOptions setExcludedExtensions(String[] excludedExtensions) {
        this.excludedExtensions = excludedExtensions;
        return this;
    }

    public String[] getExcludedRepositories() {
        return excludedRepositories;
    }

    public NutsWorkspaceOptions setExcludedRepositories(String[] excludedRepositories) {
        this.excludedRepositories = excludedRepositories;
        return this;
    }

    public String getLogin() {
        return login;
    }

    public NutsWorkspaceOptions setLogin(String login) {
        this.login = login;
        return this;
    }

    public String getPassword() {
        return password;
    }

    public NutsWorkspaceOptions setPassword(String password) {
        this.password = password;
        return this;
    }

    public boolean isNoColors() {
        return noColors;
    }

    public NutsWorkspaceOptions setNoColors(boolean noColors) {
        this.noColors = noColors;
        return this;
    }


    public NutsWorkspaceOptions copy() {
        try {
            NutsWorkspaceOptions t = (NutsWorkspaceOptions) clone();
            t.setExcludedExtensions(t.getExcludedExtensions() == null ? null : Arrays.copyOf(t.getExcludedExtensions(), t.getExcludedExtensions().length));
            t.setExcludedRepositories(t.getExcludedRepositories() == null ? null : Arrays.copyOf(t.getExcludedRepositories(), t.getExcludedRepositories().length));
            t.setBootArguments(t.getBootArguments() == null ? null : Arrays.copyOf(t.getBootArguments(), t.getBootArguments().length));
            t.setApplicationArguments(t.getApplicationArguments() == null ? null : Arrays.copyOf(t.getApplicationArguments(), t.getApplicationArguments().length));
            return t;
        } catch (CloneNotSupportedException e) {
            throw new NutsUnsupportedOperationException("Should never Happen", e);
        }
    }

    public long getCreationTime() {
        return creationTime;
    }

    public NutsWorkspaceOptions setCreationTime(long creationTime) {
        this.creationTime = creationTime;
        return this;
    }

    public boolean isReadOnly() {
        return readOnly;
    }

    public NutsWorkspaceOptions setReadOnly(boolean readOnly) {
        this.readOnly = readOnly;
        return this;
    }


    public String getLogName() {
        return logName;
    }

    public void setLogName(String logName) {
        this.logName = logName;
    }

    public String getHome() {
        return home;
    }

    public NutsWorkspaceOptions setHome(String home) {
        this.home = home;
        return this;
    }

    public String getBootRuntime() {
        return bootRuntime;
    }

    public NutsWorkspaceOptions setBootRuntime(String bootRuntime) {
        this.bootRuntime = bootRuntime;
        return this;
    }

    public String getBootRuntimeSourceURL() {
        return bootRuntimeSourceURL;
    }

    public NutsWorkspaceOptions setBootRuntimeSourceURL(String bootRuntimeSourceURL) {
        this.bootRuntimeSourceURL = bootRuntimeSourceURL;
        return this;
    }

    public NutsClassLoaderProvider getClassLoaderProvider() {
        return classLoaderProvider;
    }

    public NutsWorkspaceOptions setClassLoaderProvider(NutsClassLoaderProvider provider) {
        this.classLoaderProvider = provider;
        return this;
    }

    public String getLogFolder() {
        return logFolder;
    }

    public NutsWorkspaceOptions setLogFolder(String logFolder) {
        this.logFolder = logFolder;
        return this;
    }

    public Level getLogLevel() {
        return logLevel;
    }

    public NutsWorkspaceOptions setLogLevel(Level logLevel) {
        this.logLevel = logLevel;
        return this;
    }

    public int getLogSize() {
        return logSize;
    }

    public NutsWorkspaceOptions setLogSize(int logSize) {
        this.logSize = logSize;
        return this;
    }

    public int getLogCount() {
        return logCount;
    }

    public NutsWorkspaceOptions setLogCount(int logCount) {
        this.logCount = logCount;
        return this;
    }

    public String getBootArgumentsString() {
        if (bootArguments == null) {
            return "";
        }
        return NutsArgumentsParser.compressBootArguments(bootArguments);
    }

    public String[] getBootArguments() {
        return bootArguments;
    }

    public NutsWorkspaceOptions setBootArguments(String[] bootArguments) {
        this.bootArguments = bootArguments;
        return this;
    }

    public String[] getApplicationArguments() {
        return applicationArguments;
    }

    public NutsWorkspaceOptions setApplicationArguments(String[] applicationArguments) {
        this.applicationArguments = applicationArguments;
        return this;
    }

    public boolean isPerf() {
        return perf;
    }

    public NutsWorkspaceOptions setPerf(boolean perf) {
        this.perf = perf;
        return this;
    }

    public String getBootJavaCommand() {
        return bootJavaCommand;
    }

    public NutsWorkspaceOptions setBootJavaCommand(String bootJavaCommand) {
        this.bootJavaCommand = bootJavaCommand;
        return this;
    }

    public String getBootJavaOptions() {
        return bootJavaOptions;
    }

    public NutsWorkspaceOptions setBootJavaOptions(String bootJavaOptions) {
        this.bootJavaOptions = bootJavaOptions;
        return this;
    }

    public String getAutoConfig() {
        return autoConfig;
    }

    public NutsWorkspaceOptions setAutoConfig(String autoConfig) {
        this.autoConfig = autoConfig;
        return this;
    }

    public String[] getExecutorOptions() {
        return executorOptions==null?new String[0] : executorOptions;
    }

    public NutsWorkspaceOptions setExecutorOptions(String[] executorOptions) {
        this.executorOptions = executorOptions;
        return this;
    }

    public NutsBootCommand getBootCommand() {
        return bootCommand;
    }

    public NutsWorkspaceOptions setBootCommand(NutsBootCommand bootCommand) {
        this.bootCommand = bootCommand;
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
        if (!empty) {
            sb.append(", ");
        }
        sb.append("ignoreIfFound=").append(ignoreIfFound);
        sb.append(", createIfNotFound=").append(createIfNotFound);
        sb.append(", saveIfCreated=").append(saveIfCreated);
        sb.append(", archetype=").append(archetype);
        sb.append(", excludedExtensions=").append(Arrays.toString(excludedExtensions == null ? new String[0] : excludedExtensions));
        sb.append(", excludedRepositories=").append(Arrays.toString(excludedRepositories == null ? new String[0] : excludedRepositories));
        sb.append(")");
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsWorkspaceOptions that = (NutsWorkspaceOptions) o;
        return logSize == that.logSize &&
                logCount == that.logCount &&
                perf == that.perf &&
                ignoreIfFound == that.ignoreIfFound &&
                createIfNotFound == that.createIfNotFound &&
                saveIfCreated == that.saveIfCreated &&
                noColors == that.noColors &&
                readOnly == that.readOnly &&
                creationTime == that.creationTime &&
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
                Objects.equals(workspace, that.workspace) &&
                Objects.equals(archetype, that.archetype) &&
                Arrays.equals(excludedExtensions, that.excludedExtensions) &&
                Arrays.equals(excludedRepositories, that.excludedRepositories) &&
                Objects.equals(login, that.login) &&
                Objects.equals(password, that.password) &&
                Objects.equals(autoConfig, that.autoConfig) &&
                Objects.equals(classLoaderProvider, that.classLoaderProvider) &&
                Arrays.equals(executorOptions, that.executorOptions) &&
                bootCommand == that.bootCommand;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(home, bootRuntime, bootJavaCommand, bootJavaOptions, bootRuntimeSourceURL, logFolder, logName, logLevel, logSize, logCount, perf, workspace, ignoreIfFound, createIfNotFound, saveIfCreated, archetype, login, password, autoConfig, noColors, readOnly, creationTime, classLoaderProvider, bootCommand);
        result = 31 * result + Arrays.hashCode(bootArguments);
        result = 31 * result + Arrays.hashCode(applicationArguments);
        result = 31 * result + Arrays.hashCode(excludedExtensions);
        result = 31 * result + Arrays.hashCode(excludedRepositories);
        result = 31 * result + Arrays.hashCode(executorOptions);
        return result;
    }
}
