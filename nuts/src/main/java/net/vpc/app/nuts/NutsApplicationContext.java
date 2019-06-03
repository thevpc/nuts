/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.io.PrintStream;
import java.nio.file.Path;

/**
 * Application context that store all relevant information about application
 * execution mode, workspace, etc.
 *
 * @author vpc
 * @since 0.5.5
 */
public interface NutsApplicationContext extends NutsCommandLineContext, NutsConfigurable {

    /**
     * string that prefix each auto complete candidate
     */
    String AUTO_COMPLETE_CANDIDATE_PREFIX = "@@Candidate@@: ";

    /**
     * application execution mode
     *
     * @return application execution mode
     */
    NutsApplicationMode getMode();

    /**
     * application execution mode extra arguments
     *
     * @return
     */
    String[] getModeArguments();

    /**
     * Auto complete instance associated with the
     * {@link NutsApplicationMode#AUTO_COMPLETE} mode
     *
     * @return Auto complete instance associated with the
     * {@link NutsApplicationMode#AUTO_COMPLETE} mode
     */
    @Override
    NutsCommandAutoComplete getAutoComplete();

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsConfigurable#configure(java.lang.String...)}
     * to help return a more specific return type;
     *
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    NutsApplicationContext configure(String... args);

    /**
     * print application help to the default out ({@link #out()}) print stream.
     */
    void printHelp();

    /**
     * change terminal mode
     *
     * @param mode mode
     */
    void setTerminalMode(NutsTerminalMode mode);

    /**
     * application class instance
     *
     * @return application class instance
     */
    Class getAppClass();

    /**
     * current terminal
     *
     * @return current terminal
     */
    NutsSessionTerminal terminal();

    /**
     * current terminal
     *
     * @return current terminal
     */
    NutsSessionTerminal getTerminal();

    /**
     * current workspace
     *
     * @return current workspace
     */
    NutsWorkspace getWorkspace();

    /**
     * current session
     *
     * @return current session
     */
    NutsSession getSession();

    /**
     * update session
     *
     * @param session new session
     * @return {@code this} instance
     */
    NutsApplicationContext setSession(NutsSession session);

    /**
     * current output stream
     *
     * @return current output stream
     */
    PrintStream out();

    /**
     * update output stream
     *
     * @param out new value
     * @return {@code this} instance
     */
    NutsApplicationContext setOut(PrintStream out);

    /**
     * current error stream
     *
     * @return current error stream
     */
    PrintStream err();

    /**
     * update error stream
     *
     * @param err new value
     * @return {@code this} instance
     */
    NutsApplicationContext setErr(PrintStream err);

    /**
     * path to the programs folder of this application
     *
     * @return path to the programs folder of this application
     */
    Path getProgramsFolder();

    /**
     * path to the configuration folder of this application
     *
     * @return path to the configuration folder of this application
     */
    Path getConfigFolder();

    /**
     * path to the logs folder of this application
     *
     * @return path to the logs folder of this application
     */
    Path getLogsFolder();

    /**
     * path to the temporary files folder of this application
     *
     * @return path to the temporary files folder of this application
     */
    Path getTempFolder();

    /**
     * path to the variable files (aka /var in POSIX systems) folder of this
     * application
     *
     * @return path to the variable files (aka /var in POSIX systems) folder of
     * this application
     */
    Path getVarFolder();

    /**
     * path to the libraries files (non applications) folder of this application
     *
     * @return path to the libraries files (non applications) folder of this
     * application
     */
    Path getLibFolder();

    /**
     * path to the cache files folder of this application
     *
     * @return path to the cache files folder of this application
     */
    Path getCacheFolder();

    /**
     * application store id (typically the long nuts id)
     *
     * @return application store id (typically the long nuts id)
     */
    String getStoreId();

    /**
     * application nuts id
     *
     * @return application nuts id
     */
    NutsId getAppId();

    /**
     * application version
     *
     * @return application version
     */
    NutsVersion getAppVersion();

    /**
     * true if verbose flag activated
     *
     * @return verbose flag activated
     */
    boolean isVerbose();

    /**
     * update verbose flag
     *
     * @param verbose new value
     * @return {@code this} instance
     */
    NutsApplicationContext setVerbose(boolean verbose);

    /**
     * application arguments
     *
     * @return application arguments
     */
    @Override
    String[] getArguments();

    /**
     * terminal mode
     *
     * @return terminal mode
     */
    NutsTerminalMode getTerminalMode();

    /**
     * application start time in milli-seconds
     *
     * @return application start time in milli-seconds
     */
    long getStartTimeMillis();

    /**
     * previous version (applicable in update mode)
     *
     * @return previous version
     */
    NutsVersion getAppPreviousVersion();

    /**
     * a new instance of command line arguments to process
     *
     * @return a new instance of command line arguments to process
     */
    NutsCommand getCommandLine();

    /**
     * a new instance of command line arguments to process
     *
     * @return a new instance of command line arguments to process
     */
    NutsCommand commandLine();

    NutsApplicationContext printOutObject(Object anyObject);

    NutsApplicationContext printErrObject(Object anyObject);

}
