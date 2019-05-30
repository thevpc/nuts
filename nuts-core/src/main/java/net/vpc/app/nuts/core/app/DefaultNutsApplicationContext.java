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
import net.vpc.app.nuts.core.util.NutsConfigurableHelper;

public class DefaultNutsApplicationContext implements NutsApplicationContext {

    private final Class appClass;
    private final NutsSessionTerminal terminal;
    private NutsWorkspace workspace;
    private NutsSession session;
    private PrintStream out;
    private PrintStream err;
    private PrintStream out0;
    private PrintStream err0;
    private Path programsFolder;
    private Path configFolder;
    private Path libFolder;
    private Path cacheFolder;
    private Path logsFolder;
    private Path tempFolder;
    private Path varFolder;
    private String storeId;
    private NutsId appId;
    private boolean verbose;
    private long startTimeMillis;
    private String[] args;
    private NutsApplicationMode mode = NutsApplicationMode.RUN;

    /**
     * previous version for "on-update" mode
     */
    private NutsVersion appPreviousVersion;

    /**
     * auto complete info for "auto-complete" mode
     */
    private NutsCommandAutoComplete autoComplete;

    private String[] modeArgs = new String[0];

    public DefaultNutsApplicationContext(NutsWorkspace workspace, Class appClass, String storeId, long startTimeMillis) {
        this(workspace,
                workspace.config().getOptions().getApplicationArguments(),
                appClass,
                storeId,
                startTimeMillis
        );
    }

    public DefaultNutsApplicationContext(NutsWorkspace workspace, String[] args, Class appClass, String storeId, long startTimeMillis) {
        this.startTimeMillis = startTimeMillis <= 0 ? System.currentTimeMillis() : startTimeMillis;
        int wordIndex = -1;
        if (args.length > 0 && args[0].startsWith("--nuts-exec-mode=")) {
            NutsCommand execModeCommand = workspace.parser().parseCommandLine(args[0].substring(args[0].indexOf('=') + 1));
            if (execModeCommand.hasNext()) {
                NutsArgument a=execModeCommand.next();
                switch (a.getKey().getString()) {
                    case "auto-complete": {
                        mode = NutsApplicationMode.AUTO_COMPLETE;
                        if (execModeCommand.hasNext()) {
                            wordIndex = execModeCommand.next().getInt();
                        }
                        modeArgs = execModeCommand.toArray();
                        execModeCommand.skipAll();
                        break;
                    }
                    case "install":
                    case "on-install":
                        {
                        mode = NutsApplicationMode.INSTALL;
                            modeArgs = execModeCommand.toArray();
                            execModeCommand.skipAll();
                            break;
                    }
                    case "uninstall":
                    case "on-uninstall":
                        {
                        mode = NutsApplicationMode.UNINSTALL;
                            modeArgs = execModeCommand.toArray();
                            execModeCommand.skipAll();
                            break;
                    }
                    case "update":
                    case "on-update":
                        {
                        mode = NutsApplicationMode.UPDATE;
                            if (execModeCommand.hasNext()) {
                            appPreviousVersion = workspace.parser().parseVersion(execModeCommand.next().getString());
                        }
                            modeArgs = execModeCommand.toArray();
                            execModeCommand.skipAll();
                            break;
                    }
                    default: {
                        throw new NutsExecutionException(workspace,"Unsupported nuts-exec-mode : " + args[0], 205);
                    }
                }
            }
            args = Arrays.copyOfRange(args, 1, args.length);
        }
        NutsId appId = workspace.resolveIdForClass(appClass);
        if (appId == null) {
            throw new NutsExecutionException(workspace,"Invalid Nuts Application (" + appClass.getName() + "). Id cannot be resolved", 203);
        }
        this.workspace = (workspace);
        this.args = (args);
        this.appId = (appId);
        this.appClass = appClass;
        this.storeId = (storeId == null ? this.appId.toString() : storeId);
        this.session = (workspace.createSession());
        this.terminal = this.session.getTerminal();
        this.out0 = (terminal.fout());
        this.err0 = (terminal.ferr());
        this.out = out0;
        this.err = err0;
        NutsWorkspaceConfigManager cfg = workspace.config();
        this.programsFolder = (cfg.getStoreLocation(this.storeId, NutsStoreLocation.PROGRAMS));
        this.configFolder = (cfg.getStoreLocation(this.storeId, NutsStoreLocation.CONFIG));
        this.logsFolder = (cfg.getStoreLocation(this.storeId, NutsStoreLocation.LOGS));
        this.tempFolder = (cfg.getStoreLocation(this.storeId, NutsStoreLocation.TEMP));
        this.varFolder = (cfg.getStoreLocation(this.storeId, NutsStoreLocation.VAR));
        this.libFolder = (cfg.getStoreLocation(this.storeId, NutsStoreLocation.LIB));
        this.cacheFolder = (cfg.getStoreLocation(this.storeId, NutsStoreLocation.CACHE));
        if (mode==NutsApplicationMode.AUTO_COMPLETE) {
            this.workspace.getSystemTerminal().setMode(NutsTerminalMode.FILTERED);
            if (wordIndex < 0) {
                wordIndex = args.length;
            }
            autoComplete = new AppCommandAutoComplete(workspace,args, wordIndex, out());
        } else {
            autoComplete = null;
        }
    }

    @Override
    public NutsApplicationMode getMode() {
        return mode;
    }

    public DefaultNutsApplicationContext setMode(NutsApplicationMode mode) {
        this.mode = mode;
        return this;
    }

    @Override
    public String[] getModeArguments() {
        return modeArgs;
    }

    public DefaultNutsApplicationContext setModeArgs(String[] modeArgs) {
        this.modeArgs = modeArgs;
        return this;
    }

    @Override
    public NutsCommandAutoComplete getAutoComplete() {
        return autoComplete;
    }

    @Override
    public final NutsApplicationContext configure(String... args) {
        NutsId appId = getAppId();
        String appName=appId==null?"app": appId.getName();
        return NutsConfigurableHelper.configure(this, workspace, args, appName);
    }

    @Override
    public final boolean configure(NutsCommand commandLine, boolean skipIgnored) {
        return NutsConfigurableHelper.configure(this, workspace, commandLine,skipIgnored);
    }

    @Override
    public boolean configureFirst(NutsCommand cmd) {
        NutsArgument a = cmd.peek();
        if (a == null) {
            return false;
        }
        switch (a.getKey().getString()) {
            case "--help": {
                cmd.skip();
                if (cmd.isExecMode()) {
                    showHelp();
                    cmd.skipAll();
                }
                throw new NutsExecutionException(workspace,"Help", 0);
            }
            case "--version": {
                cmd.skip();
                if (cmd.isExecMode()) {
                    out().printf("%s%n", getWorkspace().resolveIdForClass(getClass()).getVersion().toString());
                    cmd.skipAll();
                }
                throw new NutsExecutionException(workspace,"Help", 0);
            }
            case "--term-system": {
                cmd.skip();
                setTerminalMode(null);
                return true;
            }
            case "--term-filtered": {
                cmd.skip();
                setTerminalMode(NutsTerminalMode.FILTERED);
                return true;
            }
            case "--term-formatted": {
                cmd.skip();
                setTerminalMode(NutsTerminalMode.FORMATTED);
                return true;
            }
            case "--term-inherited": {
                cmd.skip();
                setTerminalMode(NutsTerminalMode.INHERITED);
                return true;
            }
            case "--term": {
                String s = cmd.nextString().getValue().getString("").toLowerCase();
                switch (s) {
                    case "":
                    case "system":
                    case "auto": {
                        setTerminalMode(null);
                        break;
                    }
                    case "filtered": {
                        setTerminalMode(NutsTerminalMode.FILTERED);
                        break;
                    }
                    case "formatted": {
                        setTerminalMode(NutsTerminalMode.FORMATTED);
                        break;
                    }
                    case "inherited": {
                        setTerminalMode(NutsTerminalMode.INHERITED);
                        break;
                    }
                }
                return true;
            }
            case "--color": {
                NutsArgument val = cmd.nextString().getValue();
                String s = val.getString("").toLowerCase();
                switch (s) {
                    case "":
                    case "system":
                    case "auto": {
                        setTerminalMode(null);
                        break;
                    }
                    case "filtered": {
                        setTerminalMode(NutsTerminalMode.FILTERED);
                        break;
                    }
                    case "formatted": {
                        setTerminalMode(NutsTerminalMode.FORMATTED);
                        break;
                    }
                    case "inherited": {
                        setTerminalMode(NutsTerminalMode.INHERITED);
                        break;
                    }
                    default: {
                        Boolean bval = cmd.newArgument(s).getBoolean(false);
                        setTerminalMode(bval ? NutsTerminalMode.FORMATTED : NutsTerminalMode.FILTERED);
                    }
                }
                return true;
            }
            case "--verbose": {
                setVerbose(cmd.nextBoolean().getValue().getBoolean());
                return true;
            }
        }
        return false;
    }

    @Override
    public void showHelp() {
        String h = getWorkspace().resolveDefaultHelpForClass(getAppClass());
        if (h == null) {
            h = "Help is @@missing@@.";
        }
        out().print(h);
    }

    @Override
    public void setTerminalMode(NutsTerminalMode mode) {
        getWorkspace().getSystemTerminal().setMode(mode);
    }

    @Override
    public Class getAppClass() {
        return appClass;
    }

    @Override
    public NutsSessionTerminal terminal() {
        return terminal;
    }

    @Override
    public NutsSessionTerminal getTerminal() {
        return terminal;
    }

    @Override
    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    public DefaultNutsApplicationContext setWorkspace(NutsWorkspace workspace) {
        this.workspace = workspace;
        return this;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public DefaultNutsApplicationContext setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    @Override
    public PrintStream out() {
        return out;
    }

    @Override
    public DefaultNutsApplicationContext setOut(PrintStream out) {
        this.out = out;
        return this;
    }

    @Override
    public PrintStream err() {
        return err;
    }

    @Override
    public DefaultNutsApplicationContext setErr(PrintStream err) {
        this.err = err;
        return this;
    }

    protected PrintStream getOut0() {
        return out0;
    }

    protected DefaultNutsApplicationContext setOut0(PrintStream out0) {
        this.out0 = out0;
        return this;
    }

    protected PrintStream getErr0() {
        return err0;
    }

    protected DefaultNutsApplicationContext setErr0(PrintStream err0) {
        this.err0 = err0;
        return this;
    }

    @Override
    public Path getProgramsFolder() {
        return programsFolder;
    }

    public DefaultNutsApplicationContext setProgramsFolder(Path programsFolder) {
        this.programsFolder = programsFolder;
        return this;
    }

    @Override
    public Path getConfigFolder() {
        return configFolder;
    }

//    @Override
    public DefaultNutsApplicationContext setConfigFolder(Path configFolder) {
        this.configFolder = configFolder;
        return this;
    }

    @Override
    public Path getLogsFolder() {
        return logsFolder;
    }

//    @Override
    public DefaultNutsApplicationContext setLogsFolder(Path logsFolder) {
        this.logsFolder = logsFolder;
        return this;
    }

    @Override
    public Path getTempFolder() {
        return tempFolder;
    }

//    @Override
    public DefaultNutsApplicationContext setTempFolder(Path tempFolder) {
        this.tempFolder = tempFolder;
        return this;
    }

    @Override
    public Path getVarFolder() {
        return varFolder;
    }

//    @Override
    public DefaultNutsApplicationContext setVarFolder(Path varFolder) {
        this.varFolder = varFolder;
        return this;
    }

    @Override
    public String getStoreId() {
        return storeId;
    }

//    @Override
    public DefaultNutsApplicationContext setStoreId(String storeId) {
        this.storeId = storeId;
        return this;
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
    public DefaultNutsApplicationContext setAppId(NutsId appId) {
        this.appId = appId;
        return this;
    }

    @Override
    public boolean isVerbose() {
        return verbose;
    }

    @Override
    public DefaultNutsApplicationContext setVerbose(boolean verbose) {
        this.verbose = verbose;
        return this;
    }

    @Override
    public String[] getArguments() {
        return args;
    }

//    @Override
    public DefaultNutsApplicationContext setArgs(String[] args) {
        this.args = args;
        return this;
    }

    @Override
    public NutsCommand commandLine() {
        return getCommandLine();
    }

    @Override
    public NutsCommand getCommandLine() {
        return workspace.parser().parseCommand(getArguments()).setAutoComplete(getAutoComplete());
    }

    private static class AppCommandAutoComplete extends NutsCommandAutoCompleteBase {

        private ArrayList<String> words;
        int wordIndex;
        private PrintStream out0;
        private NutsWorkspace workspace;

        public AppCommandAutoComplete(NutsWorkspace workspace,String[] args, int wordIndex, PrintStream out0) {
            this.workspace=workspace;
            words = new ArrayList<>(Arrays.asList(args));
            this.wordIndex = wordIndex;
            this.out0 = out0;
        }

        @Override
        protected NutsArgumentCandidate addCandidatesImpl(NutsArgumentCandidate value) {
            NutsArgumentCandidate c = super.addCandidatesImpl(value);
            String v = value.getValue();
            if (v == null) {
                throw new NutsExecutionException(workspace,"Candidate cannot be null", 2);
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
            StringBuilder sb = new StringBuilder();
            List<String> w = getWords();
            for (int i = 0; i < w.size(); i++) {
                if (i > 0) {
                    sb.append(" ");
                }
                String word = w.get(i);
                sb.append(word);
            }
            return sb.toString();
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
    public NutsTerminalMode getTerminalMode() {
        return getWorkspace().getSystemTerminal().getOutMode();
    }

    @Override
    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public DefaultNutsApplicationContext setStartTimeMillis(long startTimeMillis) {
        this.startTimeMillis = startTimeMillis;
        return this;
    }

    @Override
    public NutsVersion getAppPreviousVersion() {
        return appPreviousVersion;
    }

    public DefaultNutsApplicationContext setAppPreviousVersion(NutsVersion previousVersion) {
        this.appPreviousVersion = previousVersion;
        return this;
    }

    @Override
    public Path getLibFolder() {
        return libFolder;
    }

    public DefaultNutsApplicationContext setLibFolder(Path libFolder) {
        this.libFolder = libFolder;
        return this;
    }

    @Override
    public Path getCacheFolder() {
        return cacheFolder;
    }

    public DefaultNutsApplicationContext setCacheFolder(Path cacheFolder) {
        this.cacheFolder = cacheFolder;
        return this;
    }

}
