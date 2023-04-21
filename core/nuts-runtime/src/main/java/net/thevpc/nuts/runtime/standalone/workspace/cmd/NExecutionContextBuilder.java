/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NExecutorComponent;
import net.thevpc.nuts.spi.NInstallerComponent;

import java.util.List;
import java.util.Map;

/**
 * execution context used in {@link NExecutorComponent} and
 * {@link NInstallerComponent}.
 *
 * @author thevpc
 * @since 0.5.4 %category Base
 */
public interface NExecutionContextBuilder extends NSessionProvider{

    /**
     * command name
     *
     * @return command name
     */
    String getCommandName();

    long getSleepMillis();

    /**
     * executor options
     *
     * @return executor options
     */
    String[] getExecutorOptions();

    /**
     * command definition if any
     *
     * @return command definition if any
     */
    NDefinition getDefinition();

    /**
     * command arguments
     *
     * @return command arguments
     */
    List<String> getArguments();

    /**
     * workspace
     *
     * @return workspace
     */
    NWorkspace getWorkspace();

    /**
     * executor descriptor
     *
     * @return executor descriptor
     */
    NArtifactCall getExecutorDescriptor();


    /**
     * execution environment
     *
     * @return execution environment
     */
    Map<String, String> getEnv();

    /**
     * current working directory
     *
     * @return current working directory
     */
    NPath getDirectory();

    /**
     * when true, any non 0 exited command will throw an Exception
     *
     * @return fail fast status
     */
    boolean isFailFast();

    /**
     * when true, the package is temporary and is not registered withing the
     * workspace
     *
     * @return true if the package is temporary and is not registered withing
     * the workspace
     */
    boolean isTemporary();

    /**
     * execution type
     *
     * @return execution type
     */
    NExecutionType getExecutionType();

    NRunAs getRunAs();

    NExecutionContextBuilder setDefinition(NDefinition definition);

    NExecutionContextBuilder setSleepMillis(long sleepMillis);
    
    NExecutionContextBuilder setEnv(Map<String, String> env);

    NExecutionContextBuilder setExecutorOptions(List<String> executorOptions);
    NExecutionContextBuilder setWorkspaceOptions(List<String> workspaceOptions);

    NExecutionContextBuilder setExecutorOptions(String[] executorOptions);

    NExecutionContextBuilder addExecutorOptions(String[] executorOptions);
    NExecutionContextBuilder addExecutorOptions(List<String> executorOptions);

    NExecutionContextBuilder setArguments(String[] arguments);

    NExecutionContextBuilder setSession(NSession session);

    NExecutionContextBuilder setWorkspace(NWorkspace workspace);

    NExecutionContextBuilder setExecutorDescriptor(NArtifactCall executorDescriptor);

    NExecutionContextBuilder setDirectory(NPath cwd);

    NExecutionContextBuilder setCommandName(String commandName);

    NExecutionContextBuilder setFailFast(boolean failFast);

    NExecutionContextBuilder setTemporary(boolean temporary);

    NExecutionContextBuilder setExecutionType(NExecutionType executionType);

    NExecutionContextBuilder setRunAs(NRunAs runAs);

    NExecutionContext build();

    NExecutionContextBuilder setAll(NExecutionContext other);

    NExecutionContextBuilder setIn(NExecInput in);

    NExecutionContextBuilder setOut(NExecOutput out);

    NExecutionContextBuilder setErr(NExecOutput err);
}
