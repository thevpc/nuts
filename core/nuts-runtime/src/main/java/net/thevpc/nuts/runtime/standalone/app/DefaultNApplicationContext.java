package net.thevpc.nuts.runtime.standalone.app;

import net.thevpc.nuts.*;
import net.thevpc.nuts.cmdline.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NOutputStream;
import net.thevpc.nuts.runtime.standalone.app.cmdline.NCommandLineUtils;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.util.NConfigurableHelper;
import net.thevpc.nuts.runtime.standalone.util.jclass.JavaClassUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTextTransformConfig;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NClock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class DefaultNApplicationContext implements NApplicationContext {

    private final Class appClass;
    private final NPath[] folders = new NPath[NStoreLocation.values().length];
    private final NPath[] sharedFolders = new NPath[NStoreLocation.values().length];
    /**
     * auto complete info for "auto-complete" mode
     */
    private final NCommandAutoComplete autoComplete;
    private NWorkspace workspace;
    private NSession session;
    private NId appId;
    private NClock startTime;
    private List<String> args;
    private NApplicationMode mode = NApplicationMode.RUN;
    private NAppStoreLocationResolver storeLocationResolver;
    /**
     * previous parse for "update" mode
     */
    private NVersion appPreviousVersion;
    private List<String> modeArgs = new ArrayList<>();

    public DefaultNApplicationContext(NWorkspace workspace, NSession session, List<String> args, Class appClass, String storeId, NClock startTime) {
        this.startTime = startTime == null ? NClock.now() : startTime;
        if (workspace == null && session == null) {
            NAssert.requireSession(session);
        } else if (workspace != null) {
            if (session == null) {
                this.session = workspace.createSession();
            } else {
                NSessionUtils.checkSession(workspace, session);
                this.session = session.copy();
            }
            this.workspace = this.session.getWorkspace();
        } else {
            this.session = session;
            this.workspace = session.getWorkspace(); //get a worspace session aware!
        }
        session = this.session;//will be used later
        int wordIndex = -1;
        if (args.size() > 0 && args.get(0).startsWith("--nuts-exec-mode=")) {
            NCommandLine execModeCommand = NCommandLine.parseDefault(
                    args.get(0).substring(args.get(0).indexOf('=') + 1)).get(session);
            if (execModeCommand.hasNext()) {
                NArg a = execModeCommand.next().get(session);
                switch (a.key()) {
                    case "auto-complete": {
                        mode = NApplicationMode.AUTO_COMPLETE;
                        if (execModeCommand.hasNext()) {
                            wordIndex = execModeCommand.next().get(session).asInt().get(session);
                        }
                        modeArgs = execModeCommand.toStringList();
                        execModeCommand.skipAll();
                        break;
                    }
                    case "install": {
                        mode = NApplicationMode.INSTALL;
                        modeArgs = execModeCommand.toStringList();
                        execModeCommand.skipAll();
                        break;
                    }
                    case "uninstall": {
                        mode = NApplicationMode.UNINSTALL;
                        modeArgs = execModeCommand.toStringList();
                        execModeCommand.skipAll();
                        break;
                    }
                    case "update": {
                        mode = NApplicationMode.UPDATE;
                        if (execModeCommand.hasNext()) {
                            appPreviousVersion = NVersion.of(execModeCommand.next().flatMap(NLiteral::asString).get(session)).get(session);
                        }
                        modeArgs = execModeCommand.toStringList();
                        execModeCommand.skipAll();
                        break;
                    }
                    default: {
                        throw new NExecutionException(session, NMsg.ofC("Unsupported nuts-exec-mode : %s", args.get(0)), 205);
                    }
                }
            }
            args = args.subList(1, args.size());
        }
        NId _appId = (NId) NApplications.getSharedMap().get("nuts.embedded.application.id");
        if (_appId != null) {
            //("=== Inherited "+_appId);
        } else {
            _appId = NIdResolver.of(session).resolveId(appClass);
        }
        if (_appId == null) {
            throw new NExecutionException(session, NMsg.ofC("invalid Nuts Application (%s). Id cannot be resolved", appClass.getName()), 203);
        }
        this.args = (args);
        this.appId = (_appId);
        this.appClass = appClass == null ? null : JavaClassUtils.unwrapCGLib(appClass);
        //always copy the session to bind to appId
        this.session.setAppId(appId);
//        NutsWorkspaceConfigManager cfg = workspace.config();
        NLocations locations = NLocations.of(session);
        for (NStoreLocation folder : NStoreLocation.values()) {
            setFolder(folder, locations.getStoreLocation(this.appId, folder));
            setSharedFolder(folder, locations.getStoreLocation(this.appId.builder().setVersion("SHARED").build(), folder));
        }
        if (mode == NApplicationMode.AUTO_COMPLETE) {
            //TODO fix me
//            this.workspace.term().setSession(session).getSystemTerminal()
//                    .setMode(NutsTerminalMode.FILTERED);
            if (wordIndex < 0) {
                wordIndex = args.size();
            }
            autoComplete = new AppCommandAutoComplete(this.session, args, wordIndex, getSession().out());
        } else {
            autoComplete = null;
        }
    }

    @Override
    public NApplicationMode getMode() {
        return mode;
    }

    @Override
    public List<String> getModeArguments() {
        return modeArgs;
    }

    @Override
    public NCommandAutoComplete getAutoComplete() {
        return autoComplete;
    }

    /**
     * configure the current command with the given arguments. This is an
     * override of the {@link NCommandLineConfigurable#configure(boolean, java.lang.String...)
     * }
     * to help return a more specific return type;
     *
     * @param args argument to configure with
     * @return {@code this} instance
     */
    @Override
    public final NApplicationContext configure(boolean skipUnsupported, String... args) {
        NId appId = getAppId();
        String appName = appId == null ? "app" : appId.getArtifactId();
        return NConfigurableHelper.configure(this, getSession(), skipUnsupported, args, appName);
    }

    @Override
    public void configureLast(NCommandLine commandLine) {
        if (!configureFirst(commandLine)) {
            commandLine.throwUnexpectedArgument();
        }
    }

    @Override
    public void printHelp() {
        NText h = NWorkspaceExt.of(getWorkspace()).resolveDefaultHelp(getAppClass(), session);
        h = NTexts.of(session).transform(h, new NTextTransformConfig()
                .setProcessTitleNumbers(true)
                .setNormalize(true)
                .setFlatten(true)
        );
        if (h == null) {
            getSession().out().println(NMsg.ofC("Help is %s.", NMsg.ofStyled("missing", NTextStyle.error())));
        } else {
            getSession().out().println(h);
        }
        //need flush if the help is syntactically incorrect
        getSession().out().flush();
    }

    @Override
    public Class getAppClass() {
        return appClass;
    }

    @Override
    public NWorkspace getWorkspace() {
        return workspace;
    }

    @Override
    public NSession getSession() {
        return session;
    }

    @Override
    public NSession createSession() {
        return getSession().getWorkspace().createSession();
    }

    @Override
    public NApplicationContext setSession(NSession session) {
        this.session = NWorkspaceUtils.bindSession(workspace, session);
        return this;
    }

    @Override
    public NPath getAppsFolder() {
        return getFolder(NStoreLocation.APPS);
    }

    @Override
    public NPath getConfigFolder() {
        return getFolder(NStoreLocation.CONFIG);
    }

    @Override
    public NPath getLogFolder() {
        return getFolder(NStoreLocation.LOG);
    }

    @Override
    public NPath getTempFolder() {
        return getFolder(NStoreLocation.TEMP);
    }

    @Override
    public NPath getVarFolder() {
        return getFolder(NStoreLocation.VAR);
    }

    @Override
    public NPath getLibFolder() {
        return getFolder(NStoreLocation.LIB);
    }

    @Override
    public NPath getRunFolder() {
        return getFolder(NStoreLocation.RUN);
    }

    @Override
    public NPath getCacheFolder() {
        return getFolder(NStoreLocation.CACHE);
    }

    @Override
    public NPath getVersionFolder(NStoreLocation location, String version) {
        if (version == null
                || version.isEmpty()
                || version.equalsIgnoreCase("current")
                || version.equals(getAppId().getVersion().getValue())) {
            return getFolder(location);
        }
        NId newId = this.getAppId().builder().setVersion(version).build();
        if (storeLocationResolver != null) {
            NPath r = storeLocationResolver.getStoreLocation(newId, location);
            if (r != null) {
                return r;
            }
        }
        return NLocations.of(session).getStoreLocation(newId, location);
    }

    @Override
    public NPath getSharedAppsFolder() {
        return getSharedFolder(NStoreLocation.APPS);
    }

    @Override
    public NPath getSharedConfigFolder() {
        return getSharedFolder(NStoreLocation.CONFIG);
    }

    @Override
    public NPath getSharedLogFolder() {
        return getSharedFolder(NStoreLocation.LOG);
    }

    @Override
    public NPath getSharedTempFolder() {
        return getSharedFolder(NStoreLocation.TEMP);
    }

    @Override
    public NPath getSharedVarFolder() {
        return getSharedFolder(NStoreLocation.VAR);
    }

    @Override
    public NPath getSharedLibFolder() {
        return getSharedFolder(NStoreLocation.LIB);
    }

    @Override
    public NPath getSharedRunFolder() {
        return getSharedFolder(NStoreLocation.RUN);
    }

    @Override
    public NPath getSharedFolder(NStoreLocation location) {
        return sharedFolders[location.ordinal()];
    }

    @Override
    public NId getAppId() {
        return appId;
    }

    @Override
    public NVersion getAppVersion() {
        return appId == null ? null : appId.getVersion();
    }

    @Override
    public List<String> getArguments() {
        return args;
    }

    @Override
    public NClock getStartTime() {
        return startTime;
    }

    @Override
    public NVersion getAppPreviousVersion() {
        return appPreviousVersion;
    }

    @Override
    public NCommandLine getCommandLine() {
        return NCommandLine.of(getArguments())
                .setCommandName(getAppId().getArtifactId())
                .setAutoComplete(getAutoComplete())
                .setSession(getSession());
    }

    @Override
    public void processCommandLine(NCommandLineProcessor commandLineProcessor) {
        getCommandLine().process(commandLineProcessor, new AppContextNCommandLineContext(this));
    }

    @Override
    public NPath getFolder(NStoreLocation location) {
        return folders[location.ordinal()];
    }

    @Override
    public boolean isExecMode() {
        return getAutoComplete() == null;
    }

    @Override
    public NAppStoreLocationResolver getStoreLocationResolver() {
        return storeLocationResolver;
    }

    @Override
    public NApplicationContext setAppVersionStoreLocationSupplier(NAppStoreLocationResolver appVersionStoreLocationSupplier) {
        this.storeLocationResolver = appVersionStoreLocationSupplier;
        return this;
    }

    public NApplicationContext setMode(NApplicationMode mode) {
        this.mode = mode;
        return this;
    }

    public NApplicationContext setModeArgs(List<String> modeArgs) {
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
    public final boolean configure(boolean skipUnsupported, NCommandLine commandLine) {
        return NConfigurableHelper.configure(this, getSession(), skipUnsupported, commandLine);
    }

    @Override
    public boolean configureFirst(NCommandLine cmd) {
        NArg a = cmd.peek().orNull();
        if (a == null) {
            return false;
        }
        boolean enabled = a.isActive();
        switch (a.key()) {
            case "-?":
            case "-h":
            case "--help": {
                cmd.skip();
                if (enabled) {
                    if (cmd.isExecMode()) {
                        printHelp();
                    }
                    cmd.skipAll();
                    throw new NExecutionException(session, NMsg.ofPlain("help"), 0);
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
                            throw new NExecutionException(session, NMsg.ofPlain("skip-event"), 0);
                        }
                    }
                }
                return true;
            }
            case "--version": {
                cmd.skip();
                if (enabled) {
                    if (cmd.isExecMode()) {
                        getSession().out().println(NIdResolver.of(session).resolveId(getClass()).getVersion());
                        cmd.skipAll();
                    }
                    throw new NExecutionException(session, NMsg.ofPlain("version"), 0);
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

    public NApplicationContext setWorkspace(NWorkspace workspace) {
        this.workspace = workspace;
        return this;
    }

    public NApplicationContext setFolder(NStoreLocation location, NPath folder) {
        this.folders[location.ordinal()] = folder;
        return this;
    }

    public NApplicationContext setSharedFolder(NStoreLocation location, NPath folder) {
        this.sharedFolders[location.ordinal()] = folder;
        return this;
    }

    //    @Override
    public NApplicationContext setAppId(NId appId) {
        this.appId = appId;
        return this;
    }

    //    @Override
    public NApplicationContext setArguments(List<String> args) {
        this.args = args;
        return this;
    }

    public NApplicationContext setArguments(String[] args) {
        this.args = new ArrayList<>(Arrays.asList(args));
        return this;
    }

    public NApplicationContext setStartTime(NClock startTime) {
        this.startTime = startTime;
        return this;
    }

    public NApplicationContext setAppPreviousVersion(NVersion previousVersion) {
        this.appPreviousVersion = previousVersion;
        return this;
    }

    private static class AppCommandAutoComplete extends NCommandAutoCompleteBase {

        private final ArrayList<String> words;
        private final NOutputStream out0;
        private final NSession session;
        private final int wordIndex;

        public AppCommandAutoComplete(NSession session, List<String> args, int wordIndex, NOutputStream out0) {
            this.session = session;
            words = new ArrayList<>(args);
            this.wordIndex = wordIndex;
            this.out0 = out0;
        }

        @Override
        public NSession getSession() {
            return session;
        }

        @Override
        public String getLine() {
            return NCommandLine.of(getWords()).toString();
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
        protected NArgCandidate addCandidatesImpl(NArgCandidate value) {
            NArgCandidate c = super.addCandidatesImpl(value);
            String v = value.getValue();
            if (v == null) {
                throw new NExecutionException(session, NMsg.ofPlain("candidate cannot be null"), 2);
            }
            String d = value.getDisplay();
            if (Objects.equals(v, d) || d == null) {
                out0.println(NMsg.ofC("%s", AUTO_COMPLETE_CANDIDATE_PREFIX + NCommandLineUtils.escapeArgument(v)));
            } else {
                out0.println(NMsg.ofC("%s", AUTO_COMPLETE_CANDIDATE_PREFIX + NCommandLineUtils.escapeArgument(v) + " " + NCommandLineUtils.escapeArgument(d)));
            }
            return c;
        }
    }

    private static class AppContextNCommandLineContext implements NCommandLineContext {
        private NApplicationContext context;

        public AppContextNCommandLineContext(NApplicationContext context) {
            this.context = context;
        }

        @Override
        public Object configure(boolean skipUnsupported, String... args) {
            return this.context.configure(skipUnsupported, args);
        }

        @Override
        public boolean configure(boolean skipUnsupported, NCommandLine commandLine) {
            return this.context.configure(skipUnsupported, commandLine);
        }

        @Override
        public boolean configureFirst(NCommandLine commandLine) {
            return this.context.configureFirst(commandLine);
        }

        @Override
        public void configureLast(NCommandLine commandLine) {
            this.context.configureLast(commandLine);
        }
    }
}
