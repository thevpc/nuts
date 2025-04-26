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
package net.thevpc.nuts;

import net.thevpc.nuts.cmdline.NCmdLineConfigurable;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.io.NExecInput;
import net.thevpc.nuts.io.NExecOutput;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.util.NConnexionString;
import net.thevpc.nuts.util.NOptional;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Execute command. This class helps executing all types of executables :
 * internal, external, alias and system
 *
 * @author thevpc
 * @app.category Commands
 * @since 0.5.4
 */
public interface NExecCmd extends NWorkspaceCmd {

    static NExecCmd of() {
        return NExtensions.of(NExecCmd.class);
    }

    static NExecCmd of(String... cmd) {
        return of().addCommand(cmd);
    }

    static NExecCmd ofSystem(String... cmd) {
        return of().addCommand(cmd).system();
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
    NExecCmd setFailFast(boolean failFast);

    /**
     * equivalent to <code>failFast(true)</code>
     *
     * @return {@code this} instance
     */
    NExecCmd failFast();

    /**
     * runin bot mode
     *
     * @param bot bot
     * @return {@code this} instance
     */
    NExecCmd setBot(Boolean bot);

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
    NExecCmd setCommand(String... command);

    /**
     * reset command arguments to the given collection
     *
     * @param command command
     * @return {@code this} instance
     */
    NExecCmd setCommand(Collection<String> command);

    /**
     * set command artifact definition. The definition is expected to include
     * content, dependencies, effective descriptor and install information.
     *
     * @param definition definition for the executable
     * @return {@code this} instance
     */
    NExecCmd setCommandDefinition(NDefinition definition);

    NDefinition getCommandDefinition();

    /**
     * append command arguments
     *
     * @param command command
     * @return {@code this} instance
     */
    NExecCmd addCommand(String... command);

    NExecCmd addCommand(NPath path);

    /**
     * append command arguments
     *
     * @param command command
     * @return {@code this} instance
     */
    NExecCmd addCommand(Collection<String> command);

    /**
     * clear command
     *
     * @return {@code this} instance
     */
    NExecCmd clearCommand();

    /**
     * append executor options
     *
     * @param executorOption executor options
     * @return {@code this} instance
     */
    NExecCmd addExecutorOption(String executorOption);

    /**
     * append executor options
     *
     * @param executorOptions executor options
     * @return {@code this} instance
     */
    NExecCmd addExecutorOptions(String... executorOptions);

    /**
     * append executor options
     *
     * @param executorOptions executor options
     * @return {@code this} instance
     */
    NExecCmd setExecutorOptions(Collection<String> executorOptions);

    NExecCmd addExecutorOptions(Collection<String> executorOptions);

    /**
     * clear executor options
     *
     * @return {@code this} instance
     */
    NExecCmd clearExecutorOptions();

    List<String> getWorkspaceOptions();

    NExecCmd clearWorkspaceOptions(String workspaceOptions);

    NExecCmd addWorkspaceOptions(NWorkspaceOptions workspaceOptions);

    NExecCmd addWorkspaceOptions(String workspaceOptions);

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
    NExecCmd setEnv(Map<String, String> env);

    /**
     * merge env properties
     *
     * @param env env properties
     * @return {@code this} instance
     */
    NExecCmd addEnv(Map<String, String> env);

    /**
     * set or unset env property. the property is unset if the value is null.
     *
     * @param key   env key
     * @param value env value
     * @return {@code this} instance
     */
    NExecCmd setEnv(String key, String value);

    /**
     * clear env
     *
     * @return {@code this} instance
     */
    NExecCmd clearEnv();

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
    NExecCmd setDirectory(NPath directory);

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
    NExecCmd setIn(NExecInput in);

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
    NExecCmd setOut(NExecOutput out);

    /**
     * grub output stream while redirecting error stream to the grabbed output
     * stream. equivalent to <code>grabOut().redirectErr()</code>
     *
     * @return
     */
    NExecCmd grabAll();

    /**
     * grub output stream to be retrieved later using
     * <code>getGrabbedOutString</code>.
     *
     * @return
     */
    NExecCmd grabOut();

    /**
     * handy method to grab output stream and silence error stream
     *
     * @return output stream, ignoring error stream
     */
    NExecCmd grabOutOnly();

    /**
     * grub error stream to be retrieved later using
     * <code>getGrabbedErrString</code>.
     *
     * @return
     */
    NExecCmd grabErr();

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
     * return grabbed error after command execution
     *
     * @return grabbed error after command execution
     */
    String getGrabbedErrString();

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
    NExecCmd setErr(NExecOutput err);

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
    NExecCmd setExecutionType(NExecutionType executionType);

    NExecCmd system();

    NExecCmd embedded();

    NExecCmd spawn();

    NExecCmd open();

    NRunAs getRunAs();

    NExecCmd setRunAs(NRunAs runAs);

    Boolean getDry();

    public NExecCmd setDry(Boolean dry);

    NExecCmd sudo();

    NExecCmd root();

    NExecCmd currentUser();

    /**
     * copy all field from the given command into {@code this} instance
     *
     * @param other command to copy from
     * @return {@code this} instance
     */
    NExecCmd copyFrom(NExecCmd other);

    /**
     * create a copy of {@code this} instance
     *
     * @return a copy of {@code this} instance
     */
    NExecCmd copy();

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


//    /**
//     * copy session
//     *
//     * @return {@code this} instance
//     */
//    @Override
//    NExecCmd copySession();

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
    NExecCmd configure(boolean skipUnsupported, String... args);

    /**
     * execute the command and return this instance
     *
     * @return {@code this} instance
     */
    @Override
    NExecCmd run();

    long getSleepMillis();

    NExecCmd setSleepMillis(long sleepMillis);

    /**
     * return host connexion string. when host is not blank, this connexion
     * string will be used to connect to a remote host for execution
     *
     * @return host
     * @since 0.8.4
     */
    String getTarget();

    /**
     * update host connexion string. when host is not blank, this connexion
     * string will be used to connect to a remote host for execution
     *
     * @param host host
     * @return {@code this} instance
     */
    NExecCmd setTarget(String host);

    NExecCmd at(String host);

    NExecCmd at(NConnexionString host);

    NExecCmd setTarget(NConnexionString host);

    NExecCmd redirectErr();
}
