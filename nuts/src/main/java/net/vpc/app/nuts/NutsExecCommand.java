/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

/**
 *
 * @author vpc
 * @since 0.5.4
 */
public interface NutsExecCommand extends NutsWorkspaceCommand {

    NutsExecCommand failFast();

    /**
     * when the execution returns a non zero result, an exception is
     * thrown.Particularly, if grabOutputString is used, error exception will
     * state the output message
     *
     * @param failFast
     * @return this instance
     */
    NutsExecCommand setFailFast(boolean failFast);

    NutsExecCommand failFast(boolean failFast);

    /**
     * failFast value
     *
     * @return true if failFast is armed
     */
    boolean isFailFast();

    String[] getCommand();

    NutsExecCommand command(String... command);

    NutsExecCommand addCommand(String... command);

    NutsExecCommand command(Collection<String> command);

    NutsExecCommand clearCommand();

    NutsExecCommand addCommand(Collection<String> command);

    NutsExecCommand addExecutorOption(String executorOption);

    NutsExecCommand executorOption(String executorOption);

    NutsExecCommand executorOptions(String... executorOptions);

    NutsExecCommand addExecutorOptions(String... executorOptions);

    NutsExecCommand executorOptions(Collection<String> executorOptions);

    NutsExecCommand addExecutorOptions(Collection<String> executorOptions);

    NutsExecCommand clearExecutorOptions();

    Properties getEnv();

    NutsExecCommand env(Map<String, String> env);

    NutsExecCommand addEnv(Properties env);

    NutsExecCommand addEnv(Map<String, String> env);

    NutsExecCommand env(String k, String val);

    NutsExecCommand setEnv(String k, String val);

    NutsExecCommand setEnv(Map<String, String> env);

    NutsExecCommand setEnv(Properties env);

    NutsExecCommand env(Properties env);

    NutsExecCommand clearEnv();

    String getDirectory();

    NutsExecCommand setDirectory(String directory);

    NutsExecCommand directory(String directory);

    InputStream getIn();

    InputStream in();

    NutsExecCommand in(InputStream in);

    NutsExecCommand setIn(InputStream in);

    PrintStream getOut();

    PrintStream out();

    NutsExecCommand grabOutputString();

    NutsExecCommand grabErrorString();

    String getOutputString();

    String getErrorString();

    NutsExecCommand out(PrintStream out);

    NutsExecCommand setOut(PrintStream out);

    NutsExecCommand err(PrintStream err);

    NutsExecCommand setErr(PrintStream err);

    PrintStream getErr();

    PrintStream err();

    NutsExecutionType getExecutionType();

    boolean isRedirectErrorStream();

    NutsExecCommand redirectErrorStream();

    NutsExecCommand setRedirectErrorStream(boolean redirectErrorStream);

    NutsExecCommand redirectErrorStream(boolean redirectErrorStream);

    NutsExecCommand setExecutionType(NutsExecutionType executionType);

    NutsExecCommand executionType(NutsExecutionType executionType);

    NutsExecCommand embedded();

    public NutsExecCommand copyFrom(NutsExecCommand other);

    public NutsExecCommand copy();

    int getResult();

    String getCommandString();

    NutsExecutableInfo which();

    String[] getExecutorOptions();

    NutsExecutionException getResultException();

    NutsExecCommand syscall();

    NutsExecCommand spawn();

    NutsCommandStringFormat getCommandStringFormat();

    NutsExecCommand setCommandStringFormat(NutsCommandStringFormat commandStringFormat);

    
    @Override
    NutsExecCommand session(NutsSession session);

    @Override
    NutsExecCommand setSession(NutsSession session);

    @Override
    NutsExecCommand configure(String... args);

    @Override
    NutsExecCommand run();

}
