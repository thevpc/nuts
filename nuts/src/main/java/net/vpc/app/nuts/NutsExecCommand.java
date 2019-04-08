/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public interface NutsExecCommand {
    NutsExecCommand setFailFast();

    /**
     * when the execution returns a non zero result, an exception is thrown.
     * Particularly, if grabOutputString is used, error exception will state the output message
     *
     * @return this instance
     */
    NutsExecCommand setFailFast(boolean failFast);

    /**
     * failFast value
     * @return true if failFast is armed
     */
    boolean isFailFast();

    NutsSession getSession();

    NutsExecCommand setSession(NutsSession session);

    List<String> getCommand();

    NutsExecCommand addCommand(String... command);

    NutsExecCommand addCommand(List<String> command);

    NutsExecCommand addExecutorOptions(String... executorOptions);

    NutsExecCommand addExecutorOptions(List<String> executorOptions);

    NutsExecCommand setCommand(String... command);

    NutsExecCommand setCommand(List<String> command);

    NutsExecCommand setExecutorOptions(String... options);

    NutsExecCommand setExecutorOptions(List<String> options);

    Properties getEnv();

    NutsExecCommand addEnv(Map<String, String> env);

    NutsExecCommand setEnv(String k, String val);

    NutsExecCommand setEnv(Map<String, String> env);

    NutsExecCommand setEnv(Properties env);

    String getDirectory();

    NutsExecCommand setDirectory(String directory);

    InputStream getIn();

    NutsExecCommand setIn(InputStream in);

    PrintStream getOut();

    NutsExecCommand grabOutputString();

    NutsExecCommand grabErrorString();

    String getOutputString();

    String getErrorString();

    NutsExecCommand setOut(PrintStream out);

    NutsExecCommand setErr(PrintStream err);

    PrintStream getErr();

    NutsExecCommand exec();

    NutsExecutionType getExecutionType();

    boolean isRedirectErrorStream();

    NutsExecCommand setRedirectErrorStream();

    NutsExecCommand setRedirectErrorStream(boolean redirectErrorStream);


    NutsExecCommand setExecutionType(NutsExecutionType executionType);

    int getResult();

    String getCommandString();

    String getCommandString(NutsCommandStringFormatter f);
}
