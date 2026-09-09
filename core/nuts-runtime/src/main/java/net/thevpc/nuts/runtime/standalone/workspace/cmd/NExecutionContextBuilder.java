/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
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
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.workspace.cmd;

import net.thevpc.nuts.artifact.NArtifactCall;
import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.command.NExecutionContext;
import net.thevpc.nuts.command.NExecutionType;
import net.thevpc.nuts.core.NRunAs;
import net.thevpc.nuts.core.NSession;
import net.thevpc.nuts.io.NExecInput;
import net.thevpc.nuts.io.NExecOutput;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NExecutorComponent;
import net.thevpc.nuts.spi.NInstallerComponent;
import net.thevpc.nuts.time.NDuration;

import java.util.List;
import java.util.Map;

/**
 * execution context used in {@link NExecutorComponent} and
 * {@link NInstallerComponent}.
 *
 * @author thevpc
 * @since 0.5.4 %category Base
 */
public interface NExecutionContextBuilder {

    /**
     * command name
     *
     * @return command name
     */
    String commandName();

    NDuration sleepDuration();

    /**
     * executor options
     *
     * @return executor options
     */
    String[] executorOptions();

    /**
     * command definition if any
     *
     * @return command definition if any
     */
    NDefinition definition();

    NDefinition runner();

    /**
     * command arguments
     *
     * @return command arguments
     */
    List<String> arguments();

    /**
     * executor descriptor
     *
     * @return executor descriptor
     */
    NArtifactCall executorDescriptor();


    /**
     * execution environment
     *
     * @return execution environment
     */
    Map<String, String> env();

    /**
     * current working directory
     *
     * @return current working directory
     */
    NPath directory();

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
    NExecutionType executionType();

    NRunAs runAs();

    NExecutionContextBuilder definition(NDefinition definition);
    NExecutionContextBuilder runner(NDefinition definition);

    NExecutionContextBuilder sleepDuration(NDuration sleepMillis);

    NExecutionContextBuilder env(Map<String, String> env);

    NExecutionContextBuilder executorOptions(List<String> executorOptions);

    NExecutionContextBuilder workspaceOptions(List<String> workspaceOptions);

    NExecutionContextBuilder executorOptions(String[] executorOptions);

    NExecutionContextBuilder addExecutorOptions(String[] executorOptions);

    NExecutionContextBuilder addExecutorOptions(List<String> executorOptions);

    NExecutionContextBuilder arguments(String[] arguments);

    NExecutionContextBuilder executorDescriptor(NArtifactCall executorDescriptor);

    NExecutionContextBuilder directory(NPath cwd);

    NExecutionContextBuilder commandName(String commandName);

    NExecutionContextBuilder failFast(boolean failFast);

    NExecutionContextBuilder temporary(boolean temporary);

    NExecutionContextBuilder temporary();

    NExecutionContextBuilder executionType(NExecutionType executionType);
    NExecutionContextBuilder session(NSession session);

    NExecutionContextBuilder runAs(NRunAs runAs);

    NExecutionContext build();

    NExecutionContextBuilder copyFrom(NExecutionContext other);

    NExecutionContextBuilder in(NExecInput in);

    NExecutionContextBuilder out(NExecOutput out);

    NExecutionContextBuilder err(NExecOutput err);

    boolean isDry();

    NExecutionContextBuilder dry(boolean dry);

    boolean isBot();

    NExecutionContextBuilder bot(boolean dry);

}
