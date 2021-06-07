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
package net.thevpc.nuts;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;

/**
 * Execute command. This class helps executing all types of executables :
 * internal, external, alias and system
 *
 * @author thevpc
 * @since 0.5.4
 * @category Commands
 */
public interface NutsExecCommand extends NutsWorkspaceCommand {

    NutsExecCommandFormat format();

    /**
     * when the execution returns a non zero result, an exception is
     * thrown.Particularly, if grabOutputString is used, error exception will
     * state the output message
     *
     * @param failFast failFast if true an exception will be thrown if exit code
     * is not zero
     * @return {@code this} instance
     */
    NutsExecCommand setFailFast(boolean failFast);

    /**
     * if true, an exception is thrown whenever the command returns non zero
     * value.
     *
     * @return true if failFast is armed
     */
    boolean isFailFast();

    /**
     * return command to execute
     *
     * @return command to execute
     */
    String[] getCommand();

    /**
     * reset command arguments to the given array
     *
     * @param command command
     * @return {@code this} instance
     * @since 0.7.1
     */
    NutsExecCommand setCommand(String... command);

    /**
     * append command arguments
     *
     * @param command command
     * @return {@code this} instance
     */
    NutsExecCommand addCommand(String... command);

    /**
     * reset command arguments to the given collection
     *
     * @param command command
     * @return {@code this} instance
     */
    NutsExecCommand setCommand(Collection<String> command);

    /**
     * append command arguments
     *
     * @param command command
     * @return {@code this} instance
     */
    NutsExecCommand addCommand(Collection<String> command);

    /**
     * clear command
     *
     * @return {@code this} instance
     */
    NutsExecCommand clearCommand();

    /**
     * set command artifact definition. The definition is expected to include
     * content, dependencies, effective descriptor and install information.
     *
     * @param definition definition for the executable
     * @return {@code this} instance
     */
    NutsExecCommand setCommand(NutsDefinition definition);

    /**
     * append executor options
     *
     * @param executorOption executor options
     * @return {@code this} instance
     */
    NutsExecCommand addExecutorOption(String executorOption);

    /**
     * append executor options
     *
     * @param executorOptions executor options
     * @return {@code this} instance
     */
    NutsExecCommand addExecutorOptions(String... executorOptions);

    /**
     * append executor options
     *
     * @param executorOptions executor options
     * @return {@code this} instance
     */
    NutsExecCommand addExecutorOptions(Collection<String> executorOptions);

    /**
     * clear executor options
     *
     * @return {@code this} instance
     */
    NutsExecCommand clearExecutorOptions();

    /**
     * return env properties
     *
     * @return env properties
     */
    Map<String, String> getEnv();

    /**
     * merge env properties
     *
     * @param env env properties
     * @return {@code this} instance
     */
    NutsExecCommand addEnv(Map<String, String> env);

    /**
     * set or unset env property. the property is unset if the value is null.
     *
     * @param key env key
     * @param value env value
     * @return {@code this} instance
     */
    NutsExecCommand setEnv(String key, String value);

    /**
     * clear existing env and set new env
     *
     * @param env new env
     * @return {@code this} instance
     */
    NutsExecCommand setEnv(Map<String, String> env);

    /**
     * clear env
     *
     * @return {@code this} instance
     */
    NutsExecCommand clearEnv();

    /**
     * return execution directory
     *
     * @return execution directory
     */
    String getDirectory();

    /**
     * set execution directory
     *
     * @param directory execution directory
     * @return {@code this} instance
     */
    NutsExecCommand setDirectory(String directory);

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
    NutsExecCommand setIn(InputStream in);

    /**
     * return new command output stream (standard output destination)
     *
     * @return new command output stream (standard output destination)
     */
    NutsPrintStream getOut();

    /**
     * grab to memory standard output
     *
     * @return {@code this} instance
     */
    NutsExecCommand grabOutputString();

    /**
     * grab to memory standard error
     *
     * @return {@code this} instance
     */
    NutsExecCommand grabErrorString();

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
     * set new command output stream (standard output destination)
     *
     * @param out standard output destination
     * @return {@code this} instance
     */
    NutsExecCommand setOut(NutsPrintStream out);

    /**
     * set new command error stream (standard error destination)
     *
     * @param err standard error destination
     * @return {@code this} instance
     */
    NutsExecCommand setErr(NutsPrintStream err);

    /**
     * return new command error stream (standard error destination)
     *
     * @return new command error stream (standard error destination)
     */
    NutsPrintStream getErr();

    /**
     * return execution type
     *
     * @return execution type
     */
    NutsExecutionType getExecutionType();

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
    NutsExecCommand setRedirectErrorStream(boolean redirectErrorStream);

    /**
     * set execution type
     *
     * @param executionType execution type
     * @return {@code this} instance
     */
    NutsExecCommand setExecutionType(NutsExecutionType executionType);

    /**
     * set embedded execution type
     *
     * @return {@code this} instance
     */
    NutsExecCommand embedded();

    /**
     * set user command execution type
     *
     * @return {@code this} instance
     */
    NutsExecCommand userCmd();

    /**
     * set root command execution type
     *
     * @return {@code this} instance
     */
    NutsExecCommand rootCmd();

    /**
     * set spawn execution type
     *
     * @return {@code this} instance
     */
    NutsExecCommand spawn();

    /**
     * return true if dry execution.
     *
     * @return true if dry execution.
     */
    boolean isDry();

    /**
     * if true set dry execution
     *
     * @param value new value
     * @return {@code this} instance
     */
    NutsExecCommand setDry(boolean value);

    /**
     * copy all field from the given command into {@code this} instance
     *
     * @param other command to copy from
     * @return {@code this} instance
     */
    NutsExecCommand copyFrom(NutsExecCommand other);

    /**
     * create a copy of {@code this} instance
     *
     * @return a copy of {@code this} instance
     */
    NutsExecCommand copy();

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
    NutsExecutableInformation which();

    /**
     * return executor options
     *
     * @return executor options
     */
    String[] getExecutorOptions();

    /**
     * return result exception or null
     *
     * @return result exception or null
     */
    NutsExecutionException getResultException();

    /**
     * copy session
     *
     * @return {@code this} instance
     */
    @Override
    NutsExecCommand copySession();

    /**
     * update session
     *
     * @param session session
     * @return {@code this} instance
     */
    @Override
    NutsExecCommand setSession(NutsSession session);

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsCommandLineConfigurable#configure(boolean, java.lang.String...)
     * }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    NutsExecCommand configure(boolean skipUnsupported, String... args);

    /**
     * execute the command and return this instance
     *
     * @return {@code this} instance
     */
    @Override
    NutsExecCommand run();

    int getSleepMillis();

    NutsExecCommand setSleepMillis(int sleepMillis);

    boolean isInheritSystemIO();

    NutsExecCommand setInheritSystemIO(boolean inheritSystemIO);

    String getRedirectOuputFile();

    NutsExecCommand setRedirectOuputFile(String redirectOuputFile);

    String getRedirectInpuFile();

    NutsExecCommand setRedirectInpuFile(String redirectInpuFile);

}
