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

import net.thevpc.nuts.artifact.NDefinition;
import net.thevpc.nuts.core.NRunAs;
import net.thevpc.nuts.core.NWorkspaceCmd;
import net.thevpc.nuts.core.NWorkspaceOptions;
import net.thevpc.nuts.cmdline.NCmdLineConfigurable;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NExecInput;
import net.thevpc.nuts.io.NExecOutput;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.net.NConnectionString;
import net.thevpc.nuts.platform.NConnectionStringAware;
import net.thevpc.nuts.util.NOptional;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Represents a command execution within the Nuts workspace.
 * <p>
 * {@code NExec} provides a unified API to execute all types of executables:
 * <ul>
 *     <li>Internal commands managed by Nuts</li>
 *     <li>External executables on the local machine</li>
 *     <li>System commands using the underlying OS shell</li>
 *     <li>Remote commands via networked connections (SSH, RSH, etc.)</li>
 * </ul>
 * <p>
 * The command can be configured with:
 * <ul>
 *     <li>Command arguments and executable paths</li>
 *     <li>Environment variables</li>
 *     <li>Executor options</li>
 *     <li>Workspace options (affecting subprocess or embedded execution)</li>
 *     <li>Standard input/output/error redirection and capturing</li>
 *     <li>Run-as context (current user, root, sudo)</li>
 *     <li>Bot mode for clean, machine-readable output</li>
 *     <li>Remote connection targeting via {@link #at(String)} or {@link #at(NConnectionString)}</li>
 * </ul>
 * <p>
 * Commands can be executed in different modes:
 * <ul>
 *     <li>Embedded execution within the current JVM</li>
 *     <li>Spawned as a separate process</li>
 *     <li>System execution using the OS shell</li>
 *     <li>Opening files or URLs with the associated OS handler</li>
 * </ul>
 * <p>
 * {@code NExec} supports output grabbing, error redirection, dry-run mode,
 * and execution target probing (local or remote).
 * <p>
 * The API is fluent, allowing chaining of configuration calls before execution.
 * <p>
 * Example usage:
 * <pre>{@code
 * NExec.of("ls", "-l")
 *         .setBot(true)
 *         .at("ssh://remote-server")
 *         .grabOut()
 *         .run();
 * String output = cmd.getGrabbedOutString();
 * }</pre>
 *
 * @author thevpc
 * @app.category Commands
 * @since 0.5.4
 */
public interface NExec extends NWorkspaceCmd, NConnectionStringAware {

    /**
     * Returns a new instance of {@link NExec} using the default extension.
     * The returned instance can be further configured before execution.
     *
     * @return a new {@code NExec} instance
     */
    static NExec of() {
        return NExtensions.of(NExec.class);
    }

    /**
     * Returns a new instance of {@link NExec} initialized with the given command arguments.
     * The arguments are appended to the command line and the instance can be further configured
     * before execution.
     *
     * @param cmd the command and its arguments
     * @return a new {@code NExec} instance with the specified command
     */
    static NExec of(String... cmd) {
        return of().addCommand(cmd);
    }

    /**
     * Returns a new instance of {@link NExec} initialized with the given command arguments
     * and configured to run as a system command.
     * This is equivalent to {@code of(cmd).system()}.
     *
     * @param cmd the command and its arguments
     * @return a new {@code NExec} instance configured for system execution
     */
    static NExec ofSystem(String... cmd) {
        return of().addCommand(cmd).system();
    }

    static NExec ofOpenFile(NPath path) {
        return of().addCommand(path).open();
    }

    /**
     * if true, an exception is thrown whenever the command returns non zero
     * value.
     *
     * @return true if failFast is armed
     */
    boolean isFailFast();

    /**
     * when the execution returns a non zero result, an exception is thrown.
     * Particularly, if <code>grabOut</code> is used, error exception will be
     * stated in the output message
     *
     * @param failFast failFast if true an exception will be thrown if exit code
     *                 is not zero
     * @return {@code this} instance
     */
    NExec setFailFast(boolean failFast);

    /**
     * equivalent to <code>failFast(true)</code>
     *
     * @return {@code this} instance
     */
    NExec failFast();

    /**
     * Enables or disables "bot mode" for this command.
     * When bot mode is enabled, all user-oriented printing, interactive prompts,
     * traces, and logging are suppressed to make the command output clean
     * and machine-readable, suitable for automated parsing.
     *
     * @param bot {@code true} to enable bot mode, {@code false} to disable
     * @return this instance for fluent API usage
     */
    NExec setBot(Boolean bot);

    /**
     * Returns whether "bot mode" is enabled for this command.
     * In bot mode, user-oriented printing, interactive prompts, traces,
     * and logging are suppressed to produce clean, machine-readable output.
     *
     * @return {@code true} if bot mode is enabled, {@code false} otherwise
     */
    Boolean getBot();

    /**
     * return command to execute
     *
     * @return command to execute
     */
    List<String> getCommand();

    /**
     * reset command arguments to the given array
     *
     * @param command command
     * @return {@code this} instance
     * @since 0.7.1
     */
    NExec setCommand(String... command);

    /**
     * reset command arguments to the given collection
     *
     * @param command command
     * @return {@code this} instance
     */
    NExec setCommand(Collection<String> command);

    /**
     * set command artifact definition. The definition is expected to include
     * content, dependencies, effective descriptor and install information.
     *
     * @param definition definition for the executable
     * @return {@code this} instance
     */
    NExec setCommandDefinition(NDefinition definition);

    /**
     * Returns the artifact definition associated with this command.
     * The definition may include the executable content, dependencies,
     * effective descriptor, and installation information.
     *
     * @return the command's artifact definition
     */
    NDefinition getCommandDefinition();

    /**
     * Appends one or more arguments to the command to be executed.
     * These arguments are added to the existing command line and will be passed
     * to the underlying process or embedded executor as-is.
     *
     * @param command one or more command arguments to append
     * @return this instance for fluent API usage
     */
    NExec addCommand(String... command);

    /**
     * Appends a path to the command to be executed.
     * The path is converted to a string representation and added to the command line.
     * This is useful for passing executable files or file arguments.
     *
     * @param path the path to append to the command
     * @return this instance for fluent API usage
     */
    NExec addCommand(NPath path);

    /**
     * append command arguments
     *
     * @param command command
     * @return {@code this} instance
     */
    NExec addCommand(Collection<String> command);

    /**
     * clear command
     *
     * @return {@code this} instance
     */
    NExec clearCommand();

    /**
     * append executor options
     *
     * @param executorOption executor options
     * @return {@code this} instance
     */
    NExec addExecutorOption(String executorOption);

    /**
     * append executor options
     *
     * @param executorOptions executor options
     * @return {@code this} instance
     */
    NExec addExecutorOptions(String... executorOptions);

    /**
     * Sets the executor options for this command, replacing any existing options.
     * Executor options are passed to the underlying process executor (or embedded runtime)
     * and can affect how the command is launched or executed.
     *
     * @param executorOptions a collection of executor-specific options
     * @return this instance for fluent API usage
     */
    NExec setExecutorOptions(Collection<String> executorOptions);

    /**
     * Appends executor options to the current set of options.
     * These options are passed to the underlying process executor (or embedded runtime)
     * in addition to any existing options.
     *
     * @param executorOptions a collection of executor-specific options to add
     * @return this instance for fluent API usage
     */
    NExec addExecutorOptions(Collection<String> executorOptions);

    /**
     * clear executor options
     *
     * @return {@code this} instance
     */
    NExec clearExecutorOptions();

    /**
     * Returns the list of workspace-specific options applied to this command.
     * These options are used to configure the subprocess or embedded process according
     * to the current Nuts workspace environment. For example, they can enable bot mode,
     * select workspace-specific directories, or influence other workspace-level execution settings.
     *
     * @return a list of workspace options as strings
     */
    List<String> getWorkspaceOptions();

    /**
     * Removes a specific workspace option from this command.
     * The option is removed from the subprocess or embedded process configuration.
     * If the specified option is not present, the command is unchanged.
     *
     * @param workspaceOptions the workspace option to remove
     * @return this instance for fluent API usage
     */
    NExec clearWorkspaceOptions(String workspaceOptions);

    /**
     * Adds a workspace option to this command.
     * The option is applied to configure the subprocess or embedded process
     * according to the Nuts workspace environment.
     *
     * @param workspaceOptions the workspace option to add
     * @return this instance for fluent API usage
     */
    NExec addWorkspaceOptions(NWorkspaceOptions workspaceOptions);

    /**
     * Adds a workspace option to this command using a string representation.
     * The option is applied to configure the subprocess or embedded process
     * according to the Nuts workspace environment.
     *
     * @param workspaceOptions the workspace option to add as a string
     * @return this instance for fluent API usage
     */
    NExec addWorkspaceOptions(String workspaceOptions);

    /**
     * return env properties
     *
     * @return env properties
     */
    Map<String, String> getEnv();

    /**
     * clear existing env and set new env
     *
     * @param env new env
     * @return {@code this} instance
     */
    NExec setEnv(Map<String, String> env);

    /**
     * merge env properties
     *
     * @param env env properties
     * @return {@code this} instance
     */
    NExec addEnv(Map<String, String> env);

    /**
     * set or unset env property. the property is unset if the value is null.
     *
     * @param key   env key
     * @param value env value
     * @return {@code this} instance
     */
    NExec setEnv(String key, String value);

    /**
     * clear env
     *
     * @return {@code this} instance
     */
    NExec clearEnv();

    /**
     * return execution directory
     *
     * @return execution directory
     */
    NPath getDirectory();

    /**
     * set execution directory
     *
     * @param directory execution directory
     * @return {@code this} instance
     */
    NExec setDirectory(NPath directory);

    /**
     * return new command input stream (standard input source)
     *
     * @return new command input stream (standard input source)
     */
    NExecInput getIn();

    /**
     * set new command input stream (standard input source)
     *
     * @param in standard input source
     * @return {@code this} instance
     */
    NExec setIn(NExecInput in);

    /**
     * return new command output stream (standard output destination)
     *
     * @return new command output stream (standard output destination)
     */
    NExecOutput getOut();

    /**
     * set new command output stream (standard output destination)
     *
     * @param out standard output destination
     * @return {@code this} instance
     */
    NExec setOut(NExecOutput out);

    /**
     * grub output stream while redirecting error stream to the grabbed output
     * stream. equivalent to <code>grabOut().redirectErr()</code>
     *
     * @return this instance
     */
    NExec grabAll();

    /**
     * grub output stream to be retrieved later using
     * <code>getGrabbedOutString</code>.
     *
     * @return this instance
     */
    NExec grabOut();

    /**
     * handy method to grab output stream and silence error stream
     *
     * @return output stream, ignoring error stream
     */
    NExec grabOutOnly();

    /**
     * grub error stream to be retrieved later using
     * <code>getGrabbedErrString</code>.
     *
     * @return this instance
     */
    NExec grabErr();

    /**
     * redirects error to out, runs the command and returns out string
     * equivalent to <code>grabAll().getGrabbedOutString()</code> if the command
     * is already run, has no effect, and may fail if the out stream is not
     * configured to bed grabbed.
     *
     * @return output stream, ignoring error stream
     */
    String getGrabbedAllString();

    /**
     * silences error, runs the command and return out string equivalent to
     * <code>grabOutOnly().getGrabbedOutString()</code> if the command is
     * already run, has no effect, and may fail if the out stream is not
     * configured to bed grabbed.
     *
     * @return output stream, ignoring error stream
     */
    String getGrabbedOutOnlyString();

    /**
     * return grabbed output after command execution Also runs the command if
     * not yet run.
     *
     * @return grabbed output after command execution
     */
    String getGrabbedOutString();

    /**
     *
     * @since 0.8.9
     */
    byte[] getGrabbedOutBytes();

    /**
     * return grabbed error after command execution
     *
     * @return grabbed error after command execution
     */
    String getGrabbedErrString();

    /**
     *
     * @since 0.8.9
     */
    byte[] getGrabbedErrBytes();

    /**
     * return new command error stream (standard error destination)
     *
     * @return new command error stream (standard error destination)
     */
    NExecOutput getErr();

    /**
     * set new command error stream (standard error destination)
     *
     * @param err standard error destination
     * @return {@code this} instance
     */
    NExec setErr(NExecOutput err);

    /**
     * return execution type
     *
     * @return execution type
     */
    NExecutionType getExecutionType();

    /**
     * set execution type
     *
     * @param executionType execution type
     * @return {@code this} instance
     */
    NExec setExecutionType(NExecutionType executionType);

    /**
     * Configures the command to be executed as a system command.
     * Typically, this means the command is executed using the underlying OS shell
     * without additional wrapping or embedding.
     *
     * @return this instance for fluent API usage
     */
    NExec system();

    /**
     * Configures the command to be executed in embedded mode.
     * Embedded execution may mean running the command within the current process
     * context or using a controlled runtime, depending on the executor implementation.
     *
     * @return this instance for fluent API usage
     */
    NExec embedded();

    /**
     * Configures the command to be executed as a new spawned process.
     * This is typically used to isolate execution from the current process,
     * ensuring separate input/output streams and lifecycle.
     *
     * @return this instance for fluent API usage
     */
    NExec spawn();

    /**
     * Configures the command to be executed in "open" mode.
     * Open mode may allow the command to interact with the environment directly,
     * possibly opening resources such as files or URLs as part of its execution.
     *
     * @return this instance for fluent API usage
     */
    NExec open();

    /**
     * Returns the user context under which the command will be executed.
     * This allows controlling the effective permissions, user name, and home directory
     * used during execution.
     *
     * @return the current run-as configuration
     */
    NRunAs getRunAs();

    /**
     * Sets the user context under which the command will be executed.
     * This determines the effective permissions, user name, and home directory
     * used for the command.
     *
     * @param runAs user context to apply to command execution
     * @return this instance for fluent API usage
     */
    NExec setRunAs(NRunAs runAs);

    /**
     * Returns whether the command is in "dry-run" mode.
     * When true, the command will not actually execute but may simulate or log the intended actions.
     *
     * @return {@code true} if dry-run is enabled, {@code false} otherwise
     */
    Boolean getDry();

    /**
     * Sets the "dry-run" mode for this command.
     * When enabled, the command does not execute but may perform validation or logging
     * to indicate what would have happened.
     *
     * @param dry {@code true} to enable dry-run mode, {@code false} otherwise
     * @return this instance for fluent API usage
     */
    NExec setDry(Boolean dry);

    /**
     * Configures the command to execute with elevated privileges using sudo.
     * Effective only if the underlying platform supports sudo or similar privilege escalation.
     *
     * @return this instance for fluent API usage
     */
    NExec sudo();

    /**
     * Configures the command to execute as the root user.
     * Typically equivalent to using sudo or switching to the root account before execution.
     *
     * @return this instance for fluent API usage
     */
    NExec root();

    /**
     * Configures the command to execute as the current user.
     * This ensures the command runs with the same permissions and environment as the invoking process.
     *
     * @return this instance for fluent API usage
     */
    NExec currentUser();

    /**
     * copy all field from the given command into {@code this} instance
     *
     * @param other command to copy from
     * @return {@code this} instance
     */
    NExec copyFrom(NExec other);

    /**
     * create a copy of {@code this} instance
     *
     * @return a copy of {@code this} instance
     */
    NExec copy();

    /**
     * return result value. if not yet executed, will execute first.
     *
     * @return result value
     */
    int getResultCode();

    /**
     * return executable information
     *
     * @return executable information
     */
    NExecutableInformation which();

    /**
     * return executor options
     *
     * @return executor options
     */
    List<String> getExecutorOptions();

    /**
     * return result exception or null
     *
     * @return result exception or null
     */
    NOptional<NExecutionException> getResultException();


    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NCmdLineConfigurable#configure(boolean, java.lang.String...)
     * }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args            argument to configure with
     * @return {@code this} instance
     */
    @Override
    NExec configure(boolean skipUnsupported, String... args);

    /**
     * execute the command and return this instance
     *
     * @return {@code this} instance
     */
    @Override
    NExec run();

    /**
     * Returns the duration in milliseconds to wait after the command completes.
     * This delay ensures that any output from the command, especially from
     * fast-running processes, is fully captured before continuing.
     *
     * <p>Even if the command finishes very quickly, the thread will pause for
     * the specified duration to allow reading the complete stdout/stderr streams.
     *
     * @return post-execution wait duration in milliseconds
     */
    long getSleepMillis();

    /**
     * Sets the duration in milliseconds to wait after the command completes.
     * This can help ensure that output from fast-running processes is fully
     * captured by the Java caller before proceeding.
     *
     * <p>The command executes normally, and after it finishes, the calling
     * thread waits for the specified duration.
     *
     * @param sleepMillis post-execution wait duration in milliseconds
     * @return this instance for fluent API usage
     */
    NExec setSleepMillis(long sleepMillis);

    /**
     * Returns the connection string representing the target host for execution.
     * When non-blank, this connection string will be used to connect to a remote host.
     *
     * @return the target host connection string
     * @since 0.8.4
     */
    NConnectionString getConnectionString();

    /**
     * Updates the target host connection string.
     * When non-blank, the connection string will be used to connect to a remote host.
     *
     * @param connectionString target host connection string
     * @return this instance for fluent API usage
     */
    NExec setConnectionString(String connectionString);

    /**
     * Shortcut to set the connection string for execution.
     *
     * @param connectionString target host connection string
     * @return this instance for fluent API usage
     */
    NExec at(String connectionString);

    /**
     * Shortcut to set the connection string for execution using a typed object.
     *
     * @param connectionString target host connection object
     * @return this instance for fluent API usage
     */
    NExec at(NConnectionString connectionString);


    /**
     * Sets the connection string for execution using a typed object.
     *
     * @param connectionString target host connection object
     * @return this instance for fluent API usage
     */
    NExec setConnectionString(NConnectionString connectionString);

    /**
     * Redirects the standard error stream to the standard output stream.
     * This is useful when capturing all output of the command into a single stream.
     *
     * @return this instance for fluent API usage
     */
    NExec redirectErr();

    /**
     * Returns true if this command is considered "raw".
     * A raw command will be passed as-is to the underlying executor without further parsing or modification.
     *
     * @return {@code true} if the command is raw, {@code false} otherwise
     * @since 0.8.9
     */
    boolean isRawCommand();

    /**
     * Sets the raw command flag.
     * When {@code true} and the command consists of a single string, it will be passed as-is
     * to the underlying executor without splitting or additional processing.
     *
     * @param rawCommand {@code true} to treat the command as raw, {@code false} otherwise
     * @return this instance for fluent API usage
     * @since 0.8.9
     */
    NExec setRawCommand(boolean rawCommand);

}
