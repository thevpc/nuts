package net.thevpc.nuts.runtime.standalone.app;

import net.thevpc.nuts.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import net.thevpc.nuts.runtime.standalone.app.cmdline.DefaultNutsCommandLine;
import net.thevpc.nuts.runtime.standalone.app.cmdline.NutsCommandLineUtils;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.jclass.JavaClassUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.util.NutsConfigurableHelper;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;

public class DefaultNutsApplicationContext implements NutsApplicationContext {

    private final Class appClass;
    private final NutsPath[] folders = new NutsPath[NutsStoreLocation.values().length];
    private final NutsPath[] sharedFolders = new NutsPath[NutsStoreLocation.values().length];
    /**
     * auto complete info for "auto-complete" mode
     */
    private final NutsCommandAutoComplete autoComplete;
    private NutsWorkspace workspace;
    private NutsSession session;
    private NutsId appId;
    private long startTimeMillis;
    private String[] args;
    private NutsApplicationMode mode = NutsApplicationMode.RUN;
    private NutsAppStoreLocationResolver storeLocationResolver;
    /**
     * previous parse for "update" mode
     */
    private NutsVersion appPreviousVersion;
    private String[] modeArgs = new String[0];

    //    public DefaultNutsApplicationContext(String[] args, NutsWorkspace workspace, Class appClass, String storeId, long startTimeMillis) {
//        this(workspace,
//                args,
//                appClass,
//                storeId,
//                startTimeMillis
//        );
//    }
    public DefaultNutsApplicationContext(NutsWorkspace workspace, NutsSession session, String[] args, Class appClass, String storeId, long startTimeMillis) {
        this.startTimeMillis = startTimeMillis <= 0 ? System.currentTimeMillis() : startTimeMillis;
        if (workspace == null && session == null) {
            throw new IllegalArgumentException("missing workpace and/or session");
        } else if (workspace != null) {
            if (session == null) {
                this.session = workspace.createSession();
            } else {
                NutsSessionUtils.checkSession(workspace, session);
                this.session = session.copy();
            }
            this.workspace = this.session.getWorkspace();
        } else {
            this.session = session;
            this.workspace = session.getWorkspace(); //get a worspace session aware!
        }
        session = this.session;//will be used later
        int wordIndex = -1;
        if (args.length > 0 && args[0].startsWith("--nuts-exec-mode=")) {
            NutsCommandLine execModeCommand = NutsCommandLine.of(args[0].substring(args[0].indexOf('=') + 1), session);
            if (execModeCommand.hasNext()) {
                NutsArgument a = execModeCommand.next();
                switch (a.getKey().getString()) {
                    case "auto-complete": {
                        mode = NutsApplicationMode.AUTO_COMPLETE;
                        if (execModeCommand.hasNext()) {
                            wordIndex = execModeCommand.next().toElement().getInt();
                        }
                        modeArgs = execModeCommand.toStringArray();
                        execModeCommand.skipAll();
                        break;
                    }
                    case "install": {
                        mode = NutsApplicationMode.INSTALL;
                        modeArgs = execModeCommand.toStringArray();
                        execModeCommand.skipAll();
                        break;
                    }
                    case "uninstall": {
                        mode = NutsApplicationMode.UNINSTALL;
                        modeArgs = execModeCommand.toStringArray();
                        execModeCommand.skipAll();
                        break;
                    }
                    case "update": {
                        mode = NutsApplicationMode.UPDATE;
                        if (execModeCommand.hasNext()) {
                            appPreviousVersion = NutsVersion.of(execModeCommand.next().getString(), session);
                        }
                        modeArgs = execModeCommand.toStringArray();
                        execModeCommand.skipAll();
                        break;
                    }
                    default: {
                        throw new NutsExecutionException(session, NutsMessage.cstyle("Unsupported nuts-exec-mode : %s", args[0]), 205);
                    }
                }
            }
            args = Arrays.copyOfRange(args, 1, args.length);
        }
        NutsId _appId = (NutsId) NutsApplications.getSharedMap().get("nuts.embedded.application.id");
        if (_appId != null) {
            //("=== Inherited "+_appId);
        } else {
            _appId = NutsIdResolver.of(session).resolveId(appClass);
        }
        if (_appId == null) {
            throw new NutsExecutionException(session, NutsMessage.cstyle("invalid Nuts Application (%s). Id cannot be resolved", appClass.getName()), 203);
        }
        this.args = (args);
        this.appId = (_appId);
        this.appClass = appClass==null?null: JavaClassUtils.unwrapCGLib(appClass);
        //always copy the session to bind to appId
        this.session.setAppId(appId);
//        NutsWorkspaceConfigManager cfg = workspace.config();
        NutsWorkspaceLocationManager locations = session.locations();
        for (NutsStoreLocation folder : NutsStoreLocation.values()) {
            setFolder(folder, locations.getStoreLocation(this.appId, folder));
            setSharedFolder(folder, locations.getStoreLocation(this.appId.builder().setVersion("SHARED").build(), folder));
        }
        if (mode == NutsApplicationMode.AUTO_COMPLETE) {
            //TODO fix me
//            this.workspace.term().setSession(session).getSystemTerminal()
//                    .setMode(NutsTerminalMode.FILTERED);
            if (wordIndex < 0) {
                wordIndex = args.length;
            }
            autoComplete = new AppCommandAutoComplete(this.session, args, wordIndex, getSession().out());
        } else {
            autoComplete = null;
        }
    }

    @Override
    public NutsApplicationMode getMode() {
        return mode;
    }

    @Override
    public String[] getModeArguments() {
        return modeArgs;
    }

    @Override
    public NutsCommandAutoComplete getAutoComplete() {
        return autoComplete;
    }

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NutsCommandLineConfigurable#configure(boolean, java.lang.String...)
     * }
     * to help return a more specific return type;
     *
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    public final NutsApplicationContext configure(boolean skipUnsupported, String... args) {
        NutsId appId = getAppId();
        String appName = appId == null ? "app" : appId.getArtifactId();
        return NutsConfigurableHelper.configure(this, getSession(), skipUnsupported, args, appName);
    }

    @Override
    public void configureLast(NutsCommandLine commandLine) {
        if (!configureFirst(commandLine)) {
            commandLine.unexpectedArgument();
        }
    }

    @Override
    public void printHelp() {
        String h = NutsWorkspaceExt.of(getWorkspace()).resolveDefaultHelp(getAppClass(), session);
        if (h == null) {
            h = "Help is ```error missing```.";
        }
        getSession().out().println(h);
        //need flush if the help is syntactically incorrect
        getSession().out().flush();
    }

    @Override
    public Class getAppClass() {
        return appClass;
    }

    @Override
    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsSession createSession() {
        return getSession().getWorkspace().createSession();
    }

    @Override
    public NutsApplicationContext setSession(NutsSession session) {
        this.session = NutsWorkspaceUtils.bindSession(workspace, session);
        return this;
    }

    @Override
    public NutsPath getAppsFolder() {
        return getFolder(NutsStoreLocation.APPS);
    }

    @Override
    public NutsPath getConfigFolder() {
        return getFolder(NutsStoreLocation.CONFIG);
    }

    @Override
    public NutsPath getLogFolder() {
        return getFolder(NutsStoreLocation.LOG);
    }

    @Override
    public NutsPath getTempFolder() {
        return getFolder(NutsStoreLocation.TEMP);
    }

    @Override
    public NutsPath getVarFolder() {
        return getFolder(NutsStoreLocation.VAR);
    }

    @Override
    public NutsPath getLibFolder() {
        return getFolder(NutsStoreLocation.LIB);
    }

    @Override
    public NutsPath getRunFolder() {
        return getFolder(NutsStoreLocation.RUN);
    }

    @Override
    public NutsPath getCacheFolder() {
        return getFolder(NutsStoreLocation.CACHE);
    }

    @Override
    public NutsPath getVersionFolder(NutsStoreLocation location, String version) {
        if (version == null
                || version.isEmpty()
                || version.equalsIgnoreCase("current")
                || version.equals(getAppId().getVersion().getValue())) {
            return getFolder(location);
        }
        NutsId newId = this.getAppId().builder().setVersion(version).build();
        if (storeLocationResolver != null) {
            NutsPath r = storeLocationResolver.getStoreLocation(newId, location);
            if (r != null) {
                return r;
            }
        }
        return session.locations().getStoreLocation(newId, location);
    }

    @Override
    public NutsPath getSharedAppsFolder() {
        return getSharedFolder(NutsStoreLocation.APPS);
    }

    @Override
    public NutsPath getSharedConfigFolder() {
        return getSharedFolder(NutsStoreLocation.CONFIG);
    }

    @Override
    public NutsPath getSharedLogFolder() {
        return getSharedFolder(NutsStoreLocation.LOG);
    }

    @Override
    public NutsPath getSharedTempFolder() {
        return getSharedFolder(NutsStoreLocation.TEMP);
    }

    @Override
    public NutsPath getSharedVarFolder() {
        return getSharedFolder(NutsStoreLocation.VAR);
    }

    @Override
    public NutsPath getSharedLibFolder() {
        return getSharedFolder(NutsStoreLocation.LIB);
    }

    @Override
    public NutsPath getSharedRunFolder() {
        return getSharedFolder(NutsStoreLocation.RUN);
    }

    @Override
    public NutsPath getSharedFolder(NutsStoreLocation location) {
        return sharedFolders[location.ordinal()];
    }

    @Override
    public NutsId getAppId() {
        return appId;
    }

    @Override
    public NutsVersion getAppVersion() {
        return appId == null ? null : appId.getVersion();
    }

    @Override
    public String[] getArguments() {
        return args;
    }

    @Override
    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    @Override
    public NutsVersion getAppPreviousVersion() {
        return appPreviousVersion;
    }

    @Override
    public NutsCommandLine getCommandLine() {
        return NutsCommandLine.of(getArguments(), getSession())
                .setCommandName(getAppId().getArtifactId())
                .setAutoComplete(getAutoComplete());
    }

    @Override
    public void processCommandLine(NutsAppCmdProcessor commandLineProcessor) {
        NutsCommandLine cmd = getCommandLine();
        NutsArgument a;
        commandLineProcessor.onCmdInitParsing(cmd, this);
        while (cmd.hasNext()) {
            a = cmd.peek();
            boolean consumed;
            if (a.isOption()) {
                consumed = commandLineProcessor.onCmdNextOption(a, cmd, this);
            } else {
                consumed = commandLineProcessor.onCmdNextNonOption(a, cmd, this);
            }
            if (consumed) {
                NutsArgument next = cmd.peek();
                //reference equality!
                if (next == a) {
                    //was not consumed!
                    throw new NutsIllegalArgumentException(session,
                            NutsMessage.cstyle("%s must consume the option: %s",
                                    (a.isOption() ? "nextOption" : "nextNonOption"),
                                    a));
                }
            } else if (!configureFirst(cmd)) {
                cmd.unexpectedArgument();
            }
        }
        commandLineProcessor.onCmdFinishParsing(cmd, this);

        // test if application is running in exec mode
        // (and not in autoComplete mode)
        if (this.isExecMode()) {
            //do the good staff here
            commandLineProcessor.onCmdExec(cmd, this);
        } else if (this.getAutoComplete() != null) {
            commandLineProcessor.onCmdAutoComplete(this.getAutoComplete(), this);
        }
    }

    @Override
    public NutsPath getFolder(NutsStoreLocation location) {
        return folders[location.ordinal()];
    }

    @Override
    public boolean isExecMode() {
        return getAutoComplete() == null;
    }

    @Override
    public NutsAppStoreLocationResolver getStoreLocationResolver() {
        return storeLocationResolver;
    }

    @Override
    public NutsApplicationContext setAppVersionStoreLocationSupplier(NutsAppStoreLocationResolver appVersionStoreLocationSupplier) {
        this.storeLocationResolver = appVersionStoreLocationSupplier;
        return this;
    }

    public NutsApplicationContext setMode(NutsApplicationMode mode) {
        this.mode = mode;
        return this;
    }

    public NutsApplicationContext setModeArgs(String[] modeArgs) {
        this.modeArgs = modeArgs;
        return this;
    }

    /**
     * configure the current command with the given arguments.
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     *                        silently
     * @param commandLine     arguments to configure with
     * @return {@code this} instance
     */
    @Override
    public final boolean configure(boolean skipUnsupported, NutsCommandLine commandLine) {
        return NutsConfigurableHelper.configure(this, getSession(), skipUnsupported, commandLine);
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmd) {
        NutsArgument a = cmd.peek();
        if (a == null) {
            return false;
        }
        boolean enabled = a.isActive();
        switch (a.getKey().getString()) {
            case "-?":
            case "-h":
            case "--help": {
                cmd.skip();
                if (enabled) {
                    if (cmd.isExecMode()) {
                        printHelp();
                    }
                    cmd.skipAll();
                    throw new NutsExecutionException(session, NutsMessage.cstyle("help"), 0);
                }
                break;
            }
            case "--skip-event": {
                switch (getMode()) {
                    case INSTALL:
                    case UNINSTALL:
                    case UPDATE: {
                        if (enabled) {
                            cmd.skip();
                            throw new NutsExecutionException(session, NutsMessage.cstyle("skip-event"), 0);
                        }
                    }
                }
                return true;
            }
            case "--version": {
                cmd.skip();
                if (enabled) {
                    if (cmd.isExecMode()) {
                        getSession().out().printf("%s%n", NutsIdResolver.of(session).resolveId(getClass()).getVersion().toString());
                        cmd.skipAll();
                    }
                    throw new NutsExecutionException(session, NutsMessage.cstyle("version"), 0);
                }
                return true;
            }
            default: {
                if (getSession() != null && getSession().configureFirst(cmd)) {
                    return true;
                }
            }
        }
        return false;
    }

    public NutsApplicationContext setWorkspace(NutsWorkspace workspace) {
        this.workspace = workspace;
        return this;
    }

    public NutsApplicationContext setFolder(NutsStoreLocation location, NutsPath folder) {
        this.folders[location.ordinal()] = folder;
        return this;
    }

    public NutsApplicationContext setSharedFolder(NutsStoreLocation location, NutsPath folder) {
        this.sharedFolders[location.ordinal()] = folder;
        return this;
    }

    //    @Override
    public NutsApplicationContext setAppId(NutsId appId) {
        this.appId = appId;
        return this;
    }

    //    @Override
    public NutsApplicationContext setArguments(String[] args) {
        this.args = args;
        return this;
    }

    public NutsApplicationContext setStartTimeMillis(long startTimeMillis) {
        this.startTimeMillis = startTimeMillis;
        return this;
    }

    public NutsApplicationContext setAppPreviousVersion(NutsVersion previousVersion) {
        this.appPreviousVersion = previousVersion;
        return this;
    }

    private static class AppCommandAutoComplete extends NutsCommandAutoCompleteBase {

        private final ArrayList<String> words;
        private final NutsPrintStream out0;
        private final NutsSession session;
        private final int wordIndex;

        public AppCommandAutoComplete(NutsSession session, String[] args, int wordIndex, NutsPrintStream out0) {
            this.session = session;
            words = new ArrayList<>(Arrays.asList(args));
            this.wordIndex = wordIndex;
            this.out0 = out0;
        }

        @Override
        public NutsSession getSession() {
            return session;
        }

        @Override
        public String getLine() {
            return new DefaultNutsCommandLine(getSession()).setArguments(getWords()).toString();
        }

        @Override
        public List<String> getWords() {
            return words;
        }

        @Override
        public int getCurrentWordIndex() {
            return wordIndex;
        }

        @Override
        protected NutsArgumentCandidate addCandidatesImpl(NutsArgumentCandidate value) {
            NutsArgumentCandidate c = super.addCandidatesImpl(value);
            String v = value.getValue();
            if (v == null) {
                throw new NutsExecutionException(session, NutsMessage.cstyle("candidate cannot be null"), 2);
            }
            String d = value.getDisplay();
            if (Objects.equals(v, d) || d == null) {
                out0.printf("%s%n", AUTO_COMPLETE_CANDIDATE_PREFIX + NutsCommandLineUtils.escapeArgument(v));
            } else {
                out0.printf("%s%n", AUTO_COMPLETE_CANDIDATE_PREFIX + NutsCommandLineUtils.escapeArgument(v) + " " + NutsCommandLineUtils.escapeArgument(d));
            }
            return c;
        }
    }
}
