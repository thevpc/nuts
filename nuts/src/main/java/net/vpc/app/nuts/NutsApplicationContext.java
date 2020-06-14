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

import java.nio.file.Path;

/**
 * Application context that store all relevant information about application
 * execution mode, workspace, etc.
 *
 * @author vpc
 * @since 0.5.5
 */
public interface NutsApplicationContext extends NutsConfigurable {

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
     * equivalent to {@code getMode()}
     *
     * @return application execution mode
     */
    NutsApplicationMode mode();

    /**
     * application execution mode extra arguments
     *
     * @return application execution mode extra arguments
     */
    String[] getModeArguments();

    /**
     * equivalent to {@code getModeArguments()}
     *
     * @return application execution mode extra arguments
     */
    String[] modeArguments();

    /**
     * Auto complete instance associated with the
     * {@link NutsApplicationMode#AUTO_COMPLETE} mode
     *
     * @return Auto complete instance
     */
    NutsCommandAutoComplete getAutoComplete();

    /**
     * equivalent to {@code getAutoComplete()}
     *
     * @return Auto complete instance
     */
    NutsCommandAutoComplete autoComplete();

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsConfigurable#configure(boolean, java.lang.String...) }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    NutsApplicationContext configure(boolean skipUnsupported, String... args);

    /**
     * print application help to the default out ({@code getSession().out()}) 
     * print stream.
     */
    void printHelp();

    /**
     * application class reference
     *
     * @return application class reference
     */
    Class getAppClass();

    /**
     * equivalent to {@code getAppClass()}
     *
     * @return application class reference
     */
    Class appClass();

    /**
     * current workspace
     *
     * @return current workspace
     */
    NutsWorkspace getWorkspace();

    /**
     * equivalent to {@code getWorkspace()}
     *
     * @return current workspace
     */
    NutsWorkspace workspace();

    /**
     * current session
     *
     * @return current session
     */
    NutsSession getSession();

    /**
     * equivalent to {@code getSession()}
     *
     * @return current session
     */
    NutsSession session();

    /**
     * update session
     *
     * @param session new session
     * @return {@code this} instance
     */
    NutsApplicationContext setSession(NutsSession session);

    /**
     * path to the apps folder of this application
     *
     * @return path to the apps folder of this application
     */
    Path getAppsFolder();

    /**
     * equivalent to {@code getAppsFolder()}
     *
     * @return path to the apps folder of this application
     */
    Path appsFolder();

    /**
     * path to the configuration folder of this application
     *
     * @return path to the configuration folder of this application
     */
    Path getConfigFolder();

    /**
     * equivalent to {@code getConfigFolder()}
     *
     * @return path to the configuration folder of this application
     */
    Path configFolder();

    /**
     * path to the log folder of this application
     *
     * @return path to the log folder of this application
     */
    Path getLogFolder();

    /**
     * equivalent to {@code getLogFolder()}
     *
     * @return path to the log folder of this application
     */
    Path logFolder();

    /**
     * path to the temporary files folder of this application
     *
     * @return path to the temporary files folder of this application
     */
    Path getTempFolder();

    /**
     * equivalent to {@code getTempFolder()}
     *
     * @return path to the temporary files folder of this application
     */
    Path tempFolder();

    /**
     * path to the variable files (aka /var in POSIX systems) folder of this
     * application
     *
     * @return path to the variable files (aka /var in POSIX systems) folder of
     * this application
     */
    Path getVarFolder();

    /**
     * equivalent to {@code getVarFolder()}
     *
     * @return path to the variable files (aka /var in POSIX systems) folder of
     * this application
     */
    Path varFolder();

    /**
     * path to the libraries files (non applications) folder of this application
     *
     * @return path to the libraries files (non applications) folder of this
     * application
     */
    Path getLibFolder();

    /**
     * path to the temporary run files (non essential sockets etc...) folder of
     * this application
     *
     * @return path to the temporary run files (non essential sockets etc...)
     * folder of this application
     */
    Path getRunFolder();

    /**
     * equivalent to {@code getLibFolder()}
     *
     * @return path to the libraries files (non applications) folder of this
     * application
     */
    Path libFolder();

    /**
     * path to the cache files folder of this application
     *
     * @return path to the cache files folder of this application
     */
    Path getCacheFolder();

    /**
     * equivalent to {@code getCacheFolder()}
     *
     * @return path to the cache files folder of this application
     */
    Path cacheFolder();

    Path getSharedAppsFolder();

    Path getSharedConfigFolder();

    Path getSharedLogFolder();

    Path getSharedTempFolder();

    Path getSharedVarFolder();

    Path getSharedLibFolder();

    Path getSharedRunFolder();

    Path getSharedFolder(NutsStoreLocation location);

    /**
     * application nuts id
     *
     * @return application nuts id
     */
    NutsId getAppId();

    /**
     * equivalent to {@code getAppId()}
     *
     * @return application nuts id
     */
    NutsId appId();

    /**
     * application version
     *
     * @return application version
     */
    NutsVersion getAppVersion();

    /**
     * equivalent to {@code getAppVersion()}
     *
     * @return application version
     */
    NutsVersion appVersion();

    /**
     * application arguments
     *
     * @return application arguments
     */
    String[] getArguments();

    /**
     * equivalent to {@code getArguments()}
     *
     * @return application arguments
     */
    String[] arguments();

    /**
     * application start time in milli-seconds
     *
     * @return application start time in milli-seconds
     */
    long getStartTimeMillis();

    /**
     * equivalent to {@code getStartTimeMillis()}
     *
     * @return application start time in milli-seconds
     */
    long startTimeMillis();

    /**
     * previous version (applicable in update mode)
     *
     * @return previous version
     */
    NutsVersion getAppPreviousVersion();

    /**
     * equivalent to {@code getAppPreviousVersion()}
     *
     * @return previous version
     */
    NutsVersion appPreviousVersion();

    /**
     * a new instance of command line arguments to process filled 
     * with application's arguments.
     *
     * @return a new instance of command line arguments to process
     */
    NutsCommandLine getCommandLine();

    /**
     * create new NutsCommandLine and consume it with the given processor.
     * This method is equivalent to the following code
     * <pre>
     *         NutsCommandLine cmdLine=getCommandLine();
     *         NutsArgument a;
     *         while (cmdLine.hasNext()) {
     *             if (!this.configureFirst(cmdLine)) {
     *                 a = cmdLine.peek();
     *                 if(a.isOption()){
     *                     if(!commandLineProcessor.processOption(a,cmdLine)){
     *                         cmdLine.unexpectedArgument();
     *                     }
     *                 }else{
     *                     if(!commandLineProcessor.processNonOption(a,cmdLine)){
     *                         cmdLine.unexpectedArgument();
     *                     }
     *                 }
     *             }
     *         }
     *         // test if application is running in exec mode
     *         // (and not in autoComplete mode)
     *         if (cmdLine.isExecMode()) {
     *             //do the good staff here
     *             commandLineProcessor.exec();
     *         }
     * </pre>
     *
     * This as an example of its usage
     * <pre>
     *     applicationContext.processCommandLine(new NutsCommandLineProcessor() {
     *             HLCWithOptions hl = new HL().withOptions();
     *             boolean noMoreOptions=false;
     *             &#64;Override
     *             public boolean processOption(NutsArgument argument, NutsCommandLine cmdLine) {
     *                 if(!noMoreOptions){
     *                     return false;
     *                 }
     *                 switch (argument.getStringKey()) {
     *                     case "--clean": {
     *                         hl.clean(cmdLine.nextBoolean().getBooleanValue());
     *                         return true;
     *                     }
     *                     case "-i":
     *                     case "--incremental":{
     *                         hl.setIncremental(cmdLine.nextBoolean().getBooleanValue());
     *                         return true;
     *                     }
     *                     case "-r":
     *                     case "--root":{
     *                         hl.setProjectRoot(cmdLine.nextString().getStringValue());
     *                         return true;
     *                     }
     *                 }
     *                 return false;
     *             }
     *
     *             &#64;Override
     *             public boolean processNonOption(NutsArgument argument, NutsCommandLine cmdLine) {
     *                 String s = argument.getString();
     *                 if(isURL(s)){
     *                     hl.includeFileURL(s);
     *                 }else{
     *                     hl.includeFile(s);
     *                 }
     *                 noMoreOptions=true;
     *                 return true;
     *             }
     *
     *             private boolean isURL(String s) {
     *                 return
     *                         s.startsWith("file:")
     *                         ||s.startsWith("http:")
     *                         ||s.startsWith("https:")
     *                         ;
     *             }
     *
     *             &#64;Override
     *             public void exec() {
     *                 hl.compile();
     *             }
     *         });
     * </pre>
     *
     * @param commandLineProcessor commandLineProcessor
     * @throws NullPointerException if the commandLineProcessor is null
     * @since 0.7.0
     */
    void processCommandLine(NutsCommandLineProcessor commandLineProcessor);

    /**
     * equivalent to {@code getCommandLine()}
     *
     * @return a new instance of command line arguments to process
     */
    NutsCommandLine commandLine();

    /**
     * application store folder path for the given {@code location}
     * @param location location type
     * @return application store folder path for the given {@code location}
     */
    Path getFolder(NutsStoreLocation location);

    /**
     * return true if {@code getAutoComplete()==null }
     * @return true if {@code getAutoComplete()==null } 
     */
    boolean isExecMode();
}
