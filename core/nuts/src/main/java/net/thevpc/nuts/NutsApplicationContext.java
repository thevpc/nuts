/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2020 thevpc
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <br>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <br>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.thevpc.nuts;

import java.nio.file.Path;

/**
 * Application context that store all relevant information about application
 * execution mode, workspace, etc.
 *
 * @author vpc
 * @since 0.5.5
 * @category Application
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
     * application execution mode extra arguments
     *
     * @return application execution mode extra arguments
     */
    String[] getModeArguments();

    /**
     * Auto complete instance associated with the
     * {@link NutsApplicationMode#AUTO_COMPLETE} mode
     *
     * @return Auto complete instance
     */
    NutsCommandAutoComplete getAutoComplete();

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
     * path to the apps folder of this application
     *
     * @return path to the apps folder of this application
     */
    Path getAppsFolder();

    /**
     * path to the configuration folder of this application
     *
     * @return path to the configuration folder of this application
     */
    Path getConfigFolder();

    /**
     * path to the log folder of this application
     *
     * @return path to the log folder of this application
     */
    Path getLogFolder();

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
     * path to the temporary run files (non essential sockets etc...) folder of
     * this application
     *
     * @return path to the temporary run files (non essential sockets etc...)
     * folder of this application
     */
    Path getRunFolder();

    /**
     * path to the cache files folder of this application
     *
     * @return path to the cache files folder of this application
     */
    Path getCacheFolder();

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
     * application version
     *
     * @return application version
     */
    NutsVersion getAppVersion();

    /**
     * application arguments
     *
     * @return application arguments
     */
    String[] getArguments();

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
    
    /**
     * calls configureFirst and ensure this is the last test 
     *
     * @param commandLine arguments to configure with
     * @return boolean when at least one argument was processed
     * @since 0.7.1
     */
    boolean configureLast(NutsCommandLine commandLine);
    
}
