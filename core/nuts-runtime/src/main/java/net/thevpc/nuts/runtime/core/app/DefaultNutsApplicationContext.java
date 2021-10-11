package net.thevpc.nuts.runtime.core.app;

import net.thevpc.nuts.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.util.NutsConfigurableHelper;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

public class DefaultNutsApplicationContext implements NutsApplicationContext {

    private final Class appClass;
    private NutsWorkspace workspace;
    private NutsSession session;
    private String[] folders = new String[NutsStoreLocation.values().length];
    private String[] sharedFolders = new String[NutsStoreLocation.values().length];
    private NutsId appId;
    private long startTimeMillis;
    private String[] args;
    private NutsApplicationMode mode = NutsApplicationMode.RUN;
    private NutsAppStoreLocationResolver storeLocationResolver;

    /**
     * previous parse for "update" mode
     */
    private NutsVersion appPreviousVersion;

    /**
     * auto complete info for "auto-complete" mode
     */
    private NutsCommandAutoComplete autoComplete;

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
                NutsWorkspaceUtils.checkSession(workspace, session);
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
            NutsCommandLine execModeCommand = this.session.commandLine().parse(args[0].substring(args[0].indexOf('=') + 1));
            if (execModeCommand.hasNext()) {
                NutsArgument a = execModeCommand.next();
                switch (a.getKey().getString()) {
                    case "auto-complete": {
                        mode = NutsApplicationMode.AUTO_COMPLETE;
                        if (execModeCommand.hasNext()) {
                            wordIndex = execModeCommand.next().getAll().getInt();
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
                            appPreviousVersion = session.version().parser().parse(execModeCommand.next().getString());
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
            _appId = this.session.id().setSession(session).resolveId(appClass);
        }
        if (_appId == null) {
            throw new NutsExecutionException(session, NutsMessage.cstyle("invalid Nuts Application (%s). Id cannot be resolved",appClass.getName()), 203);
        }
        this.args = (args);
        this.appId = (_appId);
        this.appClass = appClass;
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

    public NutsApplicationContext setMode(NutsApplicationMode mode) {
        this.mode = mode;
        return this;
    }

    @Override
    public String[] getModeArguments() {
        return modeArgs;
    }

    public NutsApplicationContext setModeArgs(String[] modeArgs) {
        this.modeArgs = modeArgs;
        return this;
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

    /**
     * configure the current command with the given arguments.
     *
     * @param skipUnsupported when true, all unsupported options are skipped
     * silently
     * @param commandLine arguments to configure with
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
        boolean enabled = a.isEnabled();
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
                        getSession().out().printf("%s%n", getSession().id().resolveId(getClass()).getVersion().toString());
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

    public NutsApplicationContext setWorkspace(NutsWorkspace workspace) {
        this.workspace = workspace;
        return this;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsApplicationContext setSession(NutsSession session) {
        this.session = NutsWorkspaceUtils.bindSession(workspace, session);
        return this;
    }

    @Override
    public String getAppsFolder() {
        return getFolder(NutsStoreLocation.APPS);
    }

    @Override
    public String getConfigFolder() {
        return getFolder(NutsStoreLocation.CONFIG);
    }

    @Override
    public String getLogFolder() {
        return getFolder(NutsStoreLocation.LOG);
    }

    @Override
    public String getTempFolder() {
        return getFolder(NutsStoreLocation.TEMP);
    }

    @Override
    public String getVarFolder() {
        return getFolder(NutsStoreLocation.VAR);
    }

    @Override
    public String getLibFolder() {
        return getFolder(NutsStoreLocation.LIB);
    }

    @Override
    public String getRunFolder() {
        return getFolder(NutsStoreLocation.RUN);
    }

    @Override
    public String getCacheFolder() {
        return getFolder(NutsStoreLocation.CACHE);
    }

    @Override
    public String getFolder(NutsStoreLocation location) {
        return folders[location.ordinal()];
    }

    @Override
    public String getVersionFolderFolder(NutsStoreLocation location, String version) {
        if (version == null
                || version.isEmpty()
                || version.equalsIgnoreCase("current")
                || version.equals(getAppId().getVersion().getValue())) {
            return getFolder(location);
        }
        NutsId newId = this.getAppId().builder().setVersion(version).build();
        if (storeLocationResolver != null) {
            String r = storeLocationResolver.getStoreLocation(newId, location);
            if (r != null) {
                return r;
            }
        }
        return session.locations().getStoreLocation(newId,location);
    }

    public NutsApplicationContext setFolder(NutsStoreLocation location, String folder) {
        this.folders[location.ordinal()] = folder;
        return this;
    }

    public NutsApplicationContext setSharedFolder(NutsStoreLocation location, String folder) {
        this.sharedFolders[location.ordinal()] = folder;
        return this;
    }

    @Override
    public String getSharedAppsFolder() {
        return getSharedFolder(NutsStoreLocation.APPS);
    }

    @Override
    public String getSharedConfigFolder() {
        return getSharedFolder(NutsStoreLocation.CONFIG);
    }

    @Override
    public String getSharedLogFolder() {
        return getSharedFolder(NutsStoreLocation.LOG);
    }

    @Override
    public String getSharedTempFolder() {
        return getSharedFolder(NutsStoreLocation.TEMP);
    }

    @Override
    public String getSharedVarFolder() {
        return getSharedFolder(NutsStoreLocation.VAR);
    }

    @Override
    public String getSharedLibFolder() {
        return getSharedFolder(NutsStoreLocation.LIB);
    }

    @Override
    public String getSharedRunFolder() {
        return getSharedFolder(NutsStoreLocation.RUN);
    }

    @Override
    public String getSharedFolder(NutsStoreLocation location) {
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

    //    @Override
    public NutsApplicationContext setAppId(NutsId appId) {
        this.appId = appId;
        return this;
    }

    @Override
    public String[] getArguments() {
        return args;
    }

    //    @Override
    public NutsApplicationContext setArgs(String[] args) {
        this.args = args;
        return this;
    }

    @Override
    public NutsCommandLine getCommandLine() {
        return this.session.commandLine().setSession(getSession())
                .create(getArguments())
                .setCommandName(getAppId().getArtifactId())
                .setAutoComplete(getAutoComplete());
    }

    @Override
    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public NutsApplicationContext setStartTimeMillis(long startTimeMillis) {
        this.startTimeMillis = startTimeMillis;
        return this;
    }

    @Override
    public NutsVersion getAppPreviousVersion() {
        return appPreviousVersion;
    }

    public NutsApplicationContext setAppPreviousVersion(NutsVersion previousVersion) {
        this.appPreviousVersion = previousVersion;
        return this;
    }

    @Override
    public boolean isExecMode() {
        return getAutoComplete() == null;
    }

    @Override
    public void processCommandLine(NutsCommandLineProcessor commandLineProcessor) {
        getCommandLine().process(this, commandLineProcessor);
    }

    @Override
    public void configureLast(NutsCommandLine commandLine) {
        if (!configureFirst(commandLine)) {
            commandLine.unexpectedArgument();
        }
    }

    private static class AppCommandAutoComplete extends NutsCommandAutoCompleteBase {

        private final ArrayList<String> words;
        private int wordIndex;
        private final NutsPrintStream out0;
        private final NutsSession session;

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
}
