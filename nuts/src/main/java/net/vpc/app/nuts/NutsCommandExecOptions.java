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
import java.util.Arrays;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;

/**
 * Command execution options
 * @author vpc
 * @since 0.5.4
 */
public class NutsCommandExecOptions implements Serializable {
    private static final long serialVersionUID = 1;

    /**
     * execution options
     */
    private String[] executorOptions;
    
    /**
     * execution environment variables
     */
    private Map<String,String> env;

    /**
     * execution directory
     */
    private String directory;

    /**
     * when fail fast,non zero exit value will raise NutsExecutionException
     */
    private boolean failFast;
    
    /**
     * execution type
     */
    private NutsExecutionType executionType;

    /**
     * execution options
     * @return execution options
     */
    public String[] getExecutorOptions() {
        return executorOptions;
    }

    /**
     * execution options
     * @param executorOptions new value
     * @return {@code this} instance
     */
    public NutsCommandExecOptions setExecutorOptions(String[] executorOptions) {
        this.executorOptions = executorOptions;
        return this;
    }

    /**
     * execution environment variables
     * @return execution environment variables
     */
    public Map<String,String> getEnv() {
        return env;
    }

    /**
     * execution environment variables
     * @param env new value
     * @return {@code this} instance
     */
    public NutsCommandExecOptions setEnv(Map<String,String> env) {
        this.env = env;
        return this;
    }

    /**
     * execution directory
     * @return execution directory
     */
    public String getDirectory() {
        return directory;
    }

    /**
     * execution directory
     * @param directory new value
     * @return {@code this} instance
     */
    public NutsCommandExecOptions setDirectory(String directory) {
        this.directory = directory;
        return this;
    }

    /**
     * when fail fast,non zero exit value will raise NutsExecutionException
     * @return when fail fast,non zero exit value will raise NutsExecutionException
     */
    public boolean isFailFast() {
        return failFast;
    }

    /**
     * when fail fast,non zero exit value will raise NutsExecutionException
     * @param failFast new value
     * @return {@code this} instance
     */
    public NutsCommandExecOptions setFailFast(boolean failFast) {
        this.failFast = failFast;
        return this;
    }

    /**
     * execution type
     * @return execution type
     */
    public NutsExecutionType getExecutionType() {
        return executionType;
    }

    /**
     * execution type
     * @param executionType new value
     * @return {@code this} instance
     */
    public NutsCommandExecOptions setExecutionType(NutsExecutionType executionType) {
        this.executionType = executionType;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsCommandExecOptions that = (NutsCommandExecOptions) o;
        return failFast == that.failFast &&
                Arrays.equals(executorOptions, that.executorOptions) &&
                Objects.equals(env, that.env) &&
                Objects.equals(directory, that.directory) &&
                executionType == that.executionType;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(env, directory, failFast, executionType);
        result = 31 * result + Arrays.hashCode(executorOptions);
        return result;
    }

    @Override
    public String toString() {
        return "NutsCommandExecOptions{" +
                "executorOptions=" + Arrays.toString(executorOptions) +
                ", env=" + env +
                ", directory='" + directory + '\'' +
                ", failFast=" + failFast +
                ", executionType=" + executionType +
                '}';
    }
}
