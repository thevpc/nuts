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

import net.thevpc.nuts.io.NPath;

import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Command execution options
 *
 * @author thevpc
 * @app.category Command Line
 * @since 0.5.4
 */
public class NCommandExecOptions implements Serializable {
    private static final long serialVersionUID = 1;

    /**
     * execution options
     */
    private List<String> executorOptions;

    /**
     * execution environment variables
     */
    private Map<String, String> env;

    /**
     * execution directory
     */
    private NPath directory;

    /**
     * when fail fast,non zero exit value will raise NutsExecutionException
     */
    private boolean failFast;

    /**
     * execution type
     */
    private NExecutionType executionType;

    /**
     * execution options
     *
     * @return execution options
     */
    public List<String> getExecutorOptions() {
        return executorOptions;
    }

    /**
     * execution options
     *
     * @param executorOptions new value
     * @return {@code this} instance
     */
    public NCommandExecOptions setExecutorOptions(List<String> executorOptions) {
        this.executorOptions = executorOptions;
        return this;
    }

    /**
     * execution environment variables
     *
     * @return execution environment variables
     */
    public Map<String, String> getEnv() {
        return env;
    }

    /**
     * execution environment variables
     *
     * @param env new value
     * @return {@code this} instance
     */
    public NCommandExecOptions setEnv(Map<String, String> env) {
        this.env = env;
        return this;
    }

    /**
     * execution directory
     *
     * @return execution directory
     */
    public NPath getDirectory() {
        return directory;
    }

    /**
     * execution directory
     *
     * @param directory new value
     * @return {@code this} instance
     */
    public NCommandExecOptions setDirectory(NPath directory) {
        this.directory = directory;
        return this;
    }

    /**
     * when fail fast,non zero exit value will raise NutsExecutionException
     *
     * @return when fail fast,non zero exit value will raise NutsExecutionException
     */
    public boolean isFailFast() {
        return failFast;
    }

    /**
     * when fail fast,non zero exit value will raise NutsExecutionException
     *
     * @param failFast new value
     * @return {@code this} instance
     */
    public NCommandExecOptions setFailFast(boolean failFast) {
        this.failFast = failFast;
        return this;
    }

    /**
     * execution type
     *
     * @return execution type
     */
    public NExecutionType getExecutionType() {
        return executionType;
    }

    /**
     * execution type
     *
     * @param executionType new value
     * @return {@code this} instance
     */
    public NCommandExecOptions setExecutionType(NExecutionType executionType) {
        this.executionType = executionType;
        return this;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(env, directory, failFast, executionType,executorOptions);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NCommandExecOptions that = (NCommandExecOptions) o;
        return failFast == that.failFast &&
                Objects.equals(executorOptions, that.executorOptions) &&
                Objects.equals(env, that.env) &&
                Objects.equals(directory, that.directory) &&
                executionType == that.executionType;
    }

    @Override
    public String toString() {
        return "NutsCommandExecOptions{" +
                "executorOptions=" + executorOptions +
                ", env=" + env +
                ", directory='" + directory + '\'' +
                ", failFast=" + failFast +
                ", executionType=" + executionType +
                '}';
    }
}
