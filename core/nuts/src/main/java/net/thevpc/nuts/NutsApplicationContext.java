/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts;


import net.thevpc.nuts.spi.NutsApplicationContexts;

import java.util.List;

/**
 * Application context that store all relevant information about application
 * execution mode, workspace, etc.
 *
 * @author thevpc
 * @app.category Application
 * @since 0.5.5
 */
public interface NutsApplicationContext extends NutsCommandLineConfigurable {
    /**
     * string that prefix each auto complete candidate
     */
    String AUTO_COMPLETE_CANDIDATE_PREFIX = "```error Candidate```: ";

    /**
     * create a new instance of {@link NutsApplicationContext}
     *
     * @param session         session context session. If null will consider {@code getSession()} that should not be null as well.
     * @param args            application arguments
     * @param startTimeMillis application start time
     * @param appClass        application class
     * @param storeId         application store id or null
     * @return new instance of {@link NutsApplicationContext}
     */
    static NutsApplicationContext of(String[] args, long startTimeMillis, Class appClass, String storeId, NutsSession session) {
        return NutsApplicationContexts.of(session).create(args, startTimeMillis, appClass, storeId);
    }

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
    List<String> getModeArguments();

    /**
     * Auto complete instance associated with the
     * {@link NutsApplicationMode#AUTO_COMPLETE} mode
     *
     * @return Auto complete instance
     */
    NutsCommandAutoComplete getAutoComplete();

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsCommandLineConfigurable#configure(boolean, java.lang.String...) }
     * to help return a more specific return type;
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * @param args            argument to configure with
     * @return {@code this} instance
     */
    @Override
    NutsApplicationContext configure(boolean skipUnsupported, String... args);

    /**
     * calls configureFirst and ensure this is the last test.
     * If the argument is not supported, throws unsupported argument
     * by calling {@link NutsCommandLine#unexpectedArgument()}
     *
     * @param commandLine arguments to configure with
     * @since 0.7.1
     */
    void configureLast(NutsCommandLine commandLine);

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
     * create a new session
     *
     * @return create a new session
     */
    NutsSession createSession();

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
    NutsPath getAppsFolder();

    /**
     * path to the configuration folder of this application
     *
     * @return path to the configuration folder of this application
     */
    NutsPath getConfigFolder();

    /**
     * path to the log folder of this application
     *
     * @return path to the log folder of this application
     */
    NutsPath getLogFolder();

    /**
     * path to the temporary files folder of this application
     *
     * @return path to the temporary files folder of this application
     */
    NutsPath getTempFolder();

    /**
     * path to the variable files (aka /var in POSIX systems) folder of this
     * application
     *
     * @return path to the variable files (aka /var in POSIX systems) folder of
     * this application
     */
    NutsPath getVarFolder();

    /**
     * path to the libraries files (non applications) folder of this application
     *
     * @return path to the libraries files (non applications) folder of this
     * application
     */
    NutsPath getLibFolder();

    /**
     * path to the temporary run files (non essential sockets etc...) folder of
     * this application
     *
     * @return path to the temporary run files (non essential sockets etc...)
     * folder of this application
     */
    NutsPath getRunFolder();

    /**
     * path to the cache files folder of this application
     *
     * @return path to the cache files folder of this application
     */
    NutsPath getCacheFolder();

    NutsPath getVersionFolder(NutsStoreLocation location, String version);

    NutsPath getSharedAppsFolder();

    NutsPath getSharedConfigFolder();

    NutsPath getSharedLogFolder();

    NutsPath getSharedTempFolder();

    NutsPath getSharedVarFolder();

    NutsPath getSharedLibFolder();

    NutsPath getSharedRunFolder();

    NutsPath getSharedFolder(NutsStoreLocation location);

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
    List<String> getArguments();

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
     *         NutsCommandLine cmd = getCommandLine();
     *         NutsArgument a;
     *         commandLineProcessor.onCmdInitParsing(cmd, this);
     *         while (cmd.hasNext()) {
     *             a = cmd.peek();
     *             boolean consumed;
     *             if (a.isOption()) {
     *                 consumed = commandLineProcessor.onCmdNextOption(a, cmd, this);
     *             } else {
     *                 consumed = commandLineProcessor.onCmdNextNonOption(a, cmd, this);
     *             }
     *             if (consumed) {
     *                 NutsArgument next = cmd.peek();
     *                 //reference equality!
     *                 if (next == a) {
     *                     //was not consumed!
     *                     throw new NutsIllegalArgumentException(session,
     *                             NutsMessage.cstyle("%s must consume the option: %s",
     *                                     (a.isOption() ? "nextOption" : "nextNonOption"),
     *                                     a));
     *                 }
     *             } else if (!configureFirst(cmd)) {
     *                 cmd.unexpectedArgument();
     *             }
     *         }
     *         commandLineProcessor.onCmdFinishParsing(cmd, this);
     *
     *         // test if application is running in exec mode
     *         // (and not in autoComplete mode)
     *         if (this.isExecMode()) {
     *             //do the good staff here
     *             commandLineProcessor.onCmdExec(cmd, this);
     *         } else if (this.getAutoComplete() != null) {
     *             commandLineProcessor.onCmdAutoComplete(this.getAutoComplete(), this);
     *         }
     * </pre>
     * <p>
     * This is an example of its usage
     * <pre>
     *     applicationContext.processCommandLine(new NutsAppCmdProcessor() {
     *             HLCWithOptions hl = new HL().withOptions();
     *             boolean noMoreOptions=false;
     *             &#64;Override
     *             public boolean onCmdNextOption(NutsArgument argument, NutsCommandLine cmdLine, NutsApplicationContext context) {
     *                 if(!noMoreOptions){
     *                     return false;
     *                 }
     *                 switch (argument.getKey().getString()) {
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
     *             public boolean onCmdNextNonOption(NutsArgument argument, NutsCommandLine cmdLine, NutsApplicationContext context) {
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
     *             public void onCmdExec(NutsCommandLine cmdLine, NutsApplicationContext context) {
     *                 hl.compile();
     *             }
     *         });
     * </pre>
     *
     * @param commandLineProcessor commandLineProcessor
     * @throws NullPointerException if the commandLineProcessor is null
     * @since 0.7.0
     */
    void processCommandLine(NutsAppCmdProcessor commandLineProcessor);

    /**
     * application store folder path for the given {@code location}
     *
     * @param location location type
     * @return application store folder path for the given {@code location}
     */
    NutsPath getFolder(NutsStoreLocation location);

    /**
     * return true if {@code getAutoComplete()==null }
     *
     * @return true if {@code getAutoComplete()==null }
     */
    boolean isExecMode();

    NutsAppStoreLocationResolver getStoreLocationResolver();

    NutsApplicationContext setAppVersionStoreLocationSupplier(NutsAppStoreLocationResolver appVersionStoreLocationSupplier);
}
