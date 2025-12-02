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
package net.thevpc.nuts.command;

import net.thevpc.nuts.artifact.NArtifactCall;
import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.core.NRunAs;
import net.thevpc.nuts.core.NSessionProvider;
import net.thevpc.nuts.io.NExecInput;
import net.thevpc.nuts.io.NExecOutput;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.spi.NExecutorComponent;
import net.thevpc.nuts.spi.NInstallerComponent;

import java.util.List;
import java.util.Map;

/**
 * Represents the execution context for commands executed via {@link NExecutorComponent}
 * or {@link NInstallerComponent}. This interface provides all information and
 * configuration needed to run a command, including arguments, environment,
 * I/O redirection, execution type, and user context.
 *
 * <p>The execution context allows:
 * <ul>
 *     <li>Capturing standard output, error, and input streams</li>
 *     <li>Setting workspace and executor options</li>
 *     <li>Specifying execution environment and working directory</li>
 *     <li>Configuring execution flags such as fail-fast, dry-run, bot mode</li>
 *     <li>Probing remote or local execution targets</li>
 * </ul>
 *
 * <p>Instances of this interface are generally provided by the executor framework
 * and can be extended or decorated as needed for specific execution requirements.
 *
 * @author thevpc
 * @app.category Base
 * @since 0.5.4
 */
public interface NExecutionContext extends NSessionProvider {

    /** Returns the name of the command being executed. */
    String getCommandName();

    /**
     * Returns the duration in milliseconds to wait after the command completes.
     * <p>
     * This ensures that any output from the command is fully captured,
     * especially for fast-executing processes.
     *
     * @return post-execution wait duration in milliseconds
     */
    long getSleepMillis();

    /** Returns the executor options to be passed to the underlying executor. */
    List<String> getExecutorOptions();


    /**
     * Returns additional workspace options that affect the execution.
     * These options may configure subprocess behavior, workspace paths,
     * or other environment-specific settings.
     */
    List<String> getWorkspaceOptions();

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

    /** Returns the executor descriptor providing metadata about the executor artifact. */
    NArtifactCall getExecutorDescriptor();

    /** Returns the environment variables for this execution. */
    Map<String, String> getEnv();

    /** Returns the working directory where the command will execute. */
    NPath getDirectory();

    /** Returns {@code true} if execution should fail immediately on non-zero exit code. */
    boolean isFailFast();

    /**
     * Returns {@code true} if the execution is temporary and should not be
     * registered with the workspace.
     */
    boolean isTemporary();

    /** Returns the type of execution (system, embedded, spawn, etc). */
    NExecutionType getExecutionType();

    /** Returns the user context under which the command will run. */
    NRunAs getRunAs();

    /**
     * Returns whether the command is in "dry-run" mode.
     * When true, the command will not actually execute but may simulate or log the intended actions.
     *
     * @return {@code true} if dry-run is enabled, {@code false} otherwise
     */
    boolean isDry();

    /**
     * Returns whether "bot mode" is enabled for this command.
     * In bot mode, user-oriented printing, interactive prompts, traces,
     * and logging are suppressed to produce clean, machine-readable output.
     *
     * @return {@code true} if bot mode is enabled, {@code false} otherwise
     */
    boolean isBot();

    /**
     * Sets the "dry-run" mode for this command.
     * When enabled, the command does not execute but may perform validation or logging
     * to indicate what would have happened.
     *
     * @param dry {@code true} to enable dry-run mode, {@code false} otherwise
     * @return this instance for fluent API usage
     */
    NExecutionContext setDry(boolean dry);

    /** Returns the input configuration for the command execution. */
    NExecInput getIn();

    /** Sets the input configuration for the command execution. */
    NExecutionContext setIn(NExecInput in);

    /** Returns the standard output configuration. */
    NExecOutput getOut();

    /** Sets the standard output configuration. */
    NExecutionContext setOut(NExecOutput out);

    /** Returns the standard error configuration. */
    NExecOutput getErr();

    /** Sets the standard error configuration. */
    NExecutionContext setErr(NExecOutput err);
}
