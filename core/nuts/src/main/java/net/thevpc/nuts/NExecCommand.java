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
 * <p>
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
package net.thevpc.nuts;

import net.thevpc.nuts.cmdline.NCmdLineConfigurable;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NPrintStream;

import java.io.InputStream;
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
public interface NExecCommand extends NWorkspaceCommand {

    static NExecCommand of(NSession session) {
        return NExtensions.of(session).createComponent(NExecCommand.class).get();
    }

    /**
     * create a prefilled command format
     *
     * @return a prefilled command format
     */
    NExecCommandFormat formatter();

    /**
     * shorthand for {@code formatter().format()}
     *
     * @return formatted string of the command
     */
    NString format();


    /**
     * if true, an exception is thrown whenever the command returns non zero
     * value.
     *
     * @return true if failFast is armed
     */
    boolean isFailFast();

    /**
     * when the execution returns a non zero result, an exception is
     * thrown. Particularly, if grabOutputString is used, error exception will
     * state the output message
     *
     * @param failFast failFast if true an exception will be thrown if exit code
     *                 is not zero
     * @return {@code this} instance
     */
    NExecCommand setFailFast(boolean failFast);

    NExecCommand failFast();

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
    NExecCommand setCommand(String... command);

    /**
     * reset command arguments to the given collection
     *
     * @param command command
     * @return {@code this} instance
     */
    NExecCommand setCommand(Collection<String> command);

    /**
     * set command artifact definition. The definition is expected to include
     * content, dependencies, effective descriptor and install information.
     *
     * @param definition definition for the executable
     * @return {@code this} instance
     */
    NExecCommand setCommand(NDefinition definition);

    /**
     * append command arguments
     *
     * @param command command
     * @return {@code this} instance
     */
    NExecCommand addCommand(String... command);

    /**
     * append command arguments
     *
     * @param command command
     * @return {@code this} instance
     */
    NExecCommand addCommand(Collection<String> command);

    /**
     * clear command
     *
     * @return {@code this} instance
     */
    NExecCommand clearCommand();

    /**
     * append executor options
     *
     * @param executorOption executor options
     * @return {@code this} instance
     */
    NExecCommand addExecutorOption(String executorOption);

    /**
     * append executor options
     *
     * @param executorOptions executor options
     * @return {@code this} instance
     */
    NExecCommand addExecutorOptions(String... executorOptions);

    /**
     * append executor options
     *
     * @param executorOptions executor options
     * @return {@code this} instance
     */
    NExecCommand addExecutorOptions(Collection<String> executorOptions);

    /**
     * clear executor options
     *
     * @return {@code this} instance
     */
    NExecCommand clearExecutorOptions();

    List<String> getWorkspaceOptions();

    NExecCommand clearWorkspaceOptions(String workspaceOptions);

    NExecCommand addWorkspaceOptions(NWorkspaceOptions workspaceOptions);

    NExecCommand addWorkspaceOptions(String workspaceOptions);

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
    NExecCommand setEnv(Map<String, String> env);

    /**
     * merge env properties
     *
     * @param env env properties
     * @return {@code this} instance
     */
    NExecCommand addEnv(Map<String, String> env);

    /**
     * set or unset env property. the property is unset if the value is null.
     *
     * @param key   env key
     * @param value env value
     * @return {@code this} instance
     */
    NExecCommand setEnv(String key, String value);

    /**
     * clear env
     *
     * @return {@code this} instance
     */
    NExecCommand clearEnv();

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
    NExecCommand setDirectory(NPath directory);

    /**
     * return new command input stream (standard input source)
     *
     * @return new command input stream (standard input source)
     */
    InputStream getIn();

    /**
     * set new command input stream (standard input source)
     *
     * @param in standard input source
     * @return {@code this} instance
     */
    NExecCommand setIn(InputStream in);

    /**
     * return new command output stream (standard output destination)
     *
     * @return new command output stream (standard output destination)
     */
    NPrintStream getOut();

    /**
     * set new command output stream (standard output destination)
     *
     * @param out standard output destination
     * @return {@code this} instance
     */
    NExecCommand setOut(NPrintStream out);

    /**
     * grab to memory standard output
     *
     * @return {@code this} instance
     */
    NExecCommand grabOutputString();

    /**
     * grab to memory standard error
     *
     * @return {@code this} instance
     */
    NExecCommand grabErrorString();

    /**
     * return grabbed output after command execution
     *
     * @return grabbed output after command execution
     */
    String getOutputString();

    /**
     * return grabbed error after command execution
     *
     * @return grabbed error after command execution
     */
    String getErrorString();

    /**
     * return new command error stream (standard error destination)
     *
     * @return new command error stream (standard error destination)
     */
    NPrintStream getErr();

    /**
     * set new command error stream (standard error destination)
     *
     * @param err standard error destination
     * @return {@code this} instance
     */
    NExecCommand setErr(NPrintStream err);

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
    NExecCommand setExecutionType(NExecutionType executionType);

    /**
     * return true if standard error is redirected to standard output
     *
     * @return true if standard error is redirected to standard output
     */
    boolean isRedirectErrorStream();

    /**
     * if true redirect standard error is redirected to standard output
     *
     * @param redirectErrorStream new value
     * @return {@code this} instance
     */
    NExecCommand setRedirectErrorStream(boolean redirectErrorStream);

    NRunAs getRunAs();

    NExecCommand setRunAs(NRunAs runAs);

    /**
     * copy all field from the given command into {@code this} instance
     *
     * @param other command to copy from
     * @return {@code this} instance
     */
    NExecCommand setAll(NExecCommand other);

    /**
     * create a copy of {@code this} instance
     *
     * @return a copy of {@code this} instance
     */
    NExecCommand copy();

    /**
     * return result value. if not yet executed, will execute first.
     *
     * @return result value
     */
    int getResult();

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
    NExecutionException getResultException();

    /**
     * update session
     *
     * @param session session
     * @return {@code this} instance
     */
    @Override
    NExecCommand setSession(NSession session);

    /**
     * copy session
     *
     * @return {@code this} instance
     */
    @Override
    NExecCommand copySession();

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
    NExecCommand configure(boolean skipUnsupported, String... args);

    /**
     * execute the command and return this instance
     *
     * @return {@code this} instance
     */
    @Override
    NExecCommand run();

    long getSleepMillis();

    NExecCommand setSleepMillis(long sleepMillis);

    boolean isInheritSystemIO();

    NExecCommand setInheritSystemIO(boolean inheritSystemIO);

    NPath getRedirectOutputFile();

    NExecCommand setRedirectOutputFile(NPath redirectOutputFile);

    NPath getRedirectInputFile();

    NExecCommand setRedirectInputFile(NPath redirectInputFile);


    /**
     * return host connexion string.
     * when host is not blank, this connexion string will be used to connect to a remote host for execution
     * @return host
     * @since 0.8.4
     */
    String getTarget() ;

    /**
     * update host connexion string.
     * when host is not blank, this connexion string will be used to connect to a remote host for execution
     * @param host host
     * @return {@code this} instance
     */
    NExecCommand setTarget(String host);
}
