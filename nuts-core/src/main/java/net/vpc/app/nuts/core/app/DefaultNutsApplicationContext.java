package net.vpc.app.nuts.core.app;

import net.vpc.app.nuts.*;

import java.io.PrintStream;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import net.vpc.app.nuts.NutsCommandAutoCompleteBase;
import net.vpc.app.nuts.NutsArgumentCandidate;
import net.vpc.app.nuts.NutsCommandAutoComplete;
import net.vpc.app.nuts.core.spi.NutsWorkspaceExt;
import net.vpc.app.nuts.core.util.NutsConfigurableHelper;

public class DefaultNutsApplicationContext implements NutsApplicationContext {

    private final Class appClass;
    private NutsWorkspace workspace;
    private NutsSession session;
    private Path[] folders = new Path[NutsStoreLocation.values().length];
    private Path[] sharedFolders = new Path[NutsStoreLocation.values().length];
    private NutsId appId;
    private long startTimeMillis;
    private String[] args;
    private NutsApplicationMode mode = NutsApplicationMode.RUN;

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

    public DefaultNutsApplicationContext(NutsWorkspace workspace, String[] args, Class appClass, String storeId, long startTimeMillis) {
        this.startTimeMillis = startTimeMillis <= 0 ? System.currentTimeMillis() : startTimeMillis;
        int wordIndex = -1;
        if (args.length > 0 && args[0].startsWith("--nuts-exec-mode=")) {
            NutsCommandLine execModeCommand = workspace.commandLine().parse(args[0].substring(args[0].indexOf('=') + 1));
            if (execModeCommand.hasNext()) {
                NutsArgument a = execModeCommand.next();
                switch (a.getStringKey()) {
                    case "auto-complete": {
                        mode = NutsApplicationMode.AUTO_COMPLETE;
                        if (execModeCommand.hasNext()) {
                            wordIndex = execModeCommand.next().getInt();
                        }
                        modeArgs = execModeCommand.toArray();
                        execModeCommand.skipAll();
                        break;
                    }
                    case "install": {
                        mode = NutsApplicationMode.INSTALL;
                        modeArgs = execModeCommand.toArray();
                        execModeCommand.skipAll();
                        break;
                    }
                    case "uninstall": {
                        mode = NutsApplicationMode.UNINSTALL;
                        modeArgs = execModeCommand.toArray();
                        execModeCommand.skipAll();
                        break;
                    }
                    case "update": {
                        mode = NutsApplicationMode.UPDATE;
                        if (execModeCommand.hasNext()) {
                            appPreviousVersion = workspace.version().parse(execModeCommand.next().getString());
                        }
                        modeArgs = execModeCommand.toArray();
                        execModeCommand.skipAll();
                        break;
                    }
                    default: {
                        throw new NutsExecutionException(workspace, "Unsupported nuts-exec-mode : " + args[0], 205);
                    }
                }
            }
            args = Arrays.copyOfRange(args, 1, args.length);
        }
        NutsId _appId = (NutsId)NutsApplications.getSharedMap().get("nuts.embedded.application.id");
        if(_appId!=null) {
            //System.out.println("=== Inherited "+_appId);
        }else {
            _appId=workspace.id().resolveId(appClass);
        }
        if (_appId == null) {
            throw new NutsExecutionException(workspace, "Invalid Nuts Application (" + appClass.getName() + "). Id cannot be resolved", 203);
        }
        this.workspace = (workspace);
        this.args = (args);
        this.appId = (_appId);
        this.appClass = appClass;
        this.session = (workspace.createSession());
        NutsWorkspaceConfigManager cfg = workspace.config();
        for (NutsStoreLocation folder : NutsStoreLocation.values()) {
            setFolder(folder, cfg.getStoreLocation(this.appId, folder));
            setSharedFolder(folder, cfg.getStoreLocation(this.appId.builder().setVersion(NutsConstants.Versions.RELEASE).build(), folder));
        }
        if (mode == NutsApplicationMode.AUTO_COMPLETE) {
            this.workspace.io().getSystemTerminal().setMode(NutsTerminalMode.FILTERED);
            if (wordIndex < 0) {
                wordIndex = args.length;
            }
            autoComplete = new AppCommandAutoComplete(workspace, args, wordIndex, getSession().out());
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
     * override of the {@link NutsConfigurable#configure(boolean, java.lang.String...) }
     * to help return a more specific return type;
     *
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    public final NutsApplicationContext configure(boolean skipUnsupported, String... args) {
        NutsId appId = getAppId();
        String appName = appId == null ? "app" : appId.getArtifactId();
        return NutsConfigurableHelper.configure(this, workspace, skipUnsupported, args, appName);
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
        return NutsConfigurableHelper.configure(this, workspace, skipUnsupported, commandLine);
    }

    @Override
    public boolean configureFirst(NutsCommandLine cmd) {
        NutsArgument a = cmd.peek();
        if (a == null) {
            return false;
        }
        switch (a.getStringKey()) {
            case "--help": {
                cmd.skip();
                if (cmd.isExecMode()) {
                    printHelp();
                    cmd.skipAll();
                }
                throw new NutsExecutionException(workspace, "Help", 0);
            }
            case "--skip-event": {
                switch (getMode()){
                    case INSTALL:
                    case UNINSTALL:
                    case UPDATE:{
                        cmd.skip();
                        throw new NutsExecutionException(workspace, "skip-event", 0);
                    }
                }
            }
            case "--version": {
                cmd.skip();
                if (cmd.isExecMode()) {
                    getSession().out().printf("%s%n", getWorkspace().id().resolveId(getClass()).getVersion().toString());
                    cmd.skipAll();
                }
                throw new NutsExecutionException(workspace, "Version", 0);
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
        String h = NutsWorkspaceExt.of(getWorkspace()).resolveDefaultHelp(getAppClass());
        if (h == null) {
            h = "Help is @@missing@@.";
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
    public NutsSession session() {
        return getSession();
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsApplicationContext setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    @Override
    public Path getAppsFolder() {
        return getFolder(NutsStoreLocation.APPS);
    }

    @Override
    public Path getConfigFolder() {
        return getFolder(NutsStoreLocation.CONFIG);
    }

    @Override
    public Path getLogFolder() {
        return getFolder(NutsStoreLocation.LOG);
    }

    @Override
    public Path getTempFolder() {
        return getFolder(NutsStoreLocation.TEMP);
    }

    @Override
    public Path getVarFolder() {
        return getFolder(NutsStoreLocation.VAR);
    }

    @Override
    public Path getLibFolder() {
        return getFolder(NutsStoreLocation.LIB);
    }

    @Override
    public Path getRunFolder() {
        return getFolder(NutsStoreLocation.RUN);
    }

    @Override
    public Path getCacheFolder() {
        return getFolder(NutsStoreLocation.CACHE);
    }

    @Override
    public Path getFolder(NutsStoreLocation location) {
        return folders[location.ordinal()];
    }

    public NutsApplicationContext setFolder(NutsStoreLocation location, Path folder) {
        this.folders[location.ordinal()] = folder;
        return this;
    }

    public NutsApplicationContext setSharedFolder(NutsStoreLocation location, Path folder) {
        this.sharedFolders[location.ordinal()] = folder;
        return this;
    }

    @Override
    public Path getSharedAppsFolder() {
        return getSharedFolder(NutsStoreLocation.APPS);
    }

    @Override
    public Path getSharedConfigFolder() {
        return getSharedFolder(NutsStoreLocation.CONFIG);
    }

    @Override
    public Path getSharedLogFolder() {
        return getSharedFolder(NutsStoreLocation.LOG);
    }

    @Override
    public Path getSharedTempFolder() {
        return getSharedFolder(NutsStoreLocation.TEMP);
    }

    @Override
    public Path getSharedVarFolder() {
        return getSharedFolder(NutsStoreLocation.VAR);
    }

    @Override
    public Path getSharedLibFolder() {
        return getSharedFolder(NutsStoreLocation.LIB);
    }

    @Override
    public Path getSharedRunFolder() {
        return getSharedFolder(NutsStoreLocation.RUN);
    }

    @Override
    public Path getSharedFolder(NutsStoreLocation location) {
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
    public NutsCommandLine commandLine() {
        return getCommandLine();
    }

    @Override
    public NutsCommandLine getCommandLine() {
        return workspace.commandLine().create(getArguments()).setAutoComplete(getAutoComplete());
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
    public NutsApplicationMode mode() {
        return getMode();
    }

    @Override
    public String[] modeArguments() {
        return getModeArguments();
    }

    @Override
    public NutsCommandAutoComplete autoComplete() {
        return getAutoComplete();
    }

    @Override
    public Class appClass() {
        return getAppClass();
    }

    @Override
    public NutsWorkspace workspace() {
        return getWorkspace();
    }

    @Override
    public Path appsFolder() {
        return getAppsFolder();
    }

    @Override
    public Path configFolder() {
        return getConfigFolder();
    }

    @Override
    public Path logFolder() {
        return getLogFolder();
    }

    @Override
    public Path tempFolder() {
        return getTempFolder();
    }

    @Override
    public Path varFolder() {
        return getVarFolder();
    }

    @Override
    public Path libFolder() {
        return getLibFolder();
    }

    @Override
    public Path cacheFolder() {
        return getCacheFolder();
    }

    @Override
    public NutsId appId() {
        return getAppId();
    }

    @Override
    public NutsVersion appVersion() {
        return getAppVersion();
    }

    @Override
    public String[] arguments() {
        return getArguments();
    }

    @Override
    public long startTimeMillis() {
        return getStartTimeMillis();
    }

    @Override
    public NutsVersion appPreviousVersion() {
        return getAppPreviousVersion();
    }

    @Override
    public boolean isExecMode() {
        return getAutoComplete() == null;
    }

    private static class AppCommandAutoComplete extends NutsCommandAutoCompleteBase {

        private final ArrayList<String> words;
        private int wordIndex;
        private final PrintStream out0;
        private final NutsWorkspace workspace;

        public AppCommandAutoComplete(NutsWorkspace workspace, String[] args, int wordIndex, PrintStream out0) {
            this.workspace = workspace;
            words = new ArrayList<>(Arrays.asList(args));
            this.wordIndex = wordIndex;
            this.out0 = out0;
        }

        @Override
        protected NutsArgumentCandidate addCandidatesImpl(NutsArgumentCandidate value) {
            NutsArgumentCandidate c = super.addCandidatesImpl(value);
            String v = value.getValue();
            if (v == null) {
                throw new NutsExecutionException(workspace, "Candidate cannot be null", 2);
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
            return new DefaultNutsCommandLine(workspace).setArguments(getWords()).toString();
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

}
