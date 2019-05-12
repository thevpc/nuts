package net.vpc.app.nuts.core.util.app;

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
import net.vpc.app.nuts.NutsTableFormat;

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
    private NutsTerminalMode terminalMode;
    private long startTimeMillis;
    private String[] args;
    private NutsTableCellFormat tableCellFormatter;
    private String mode = "launch";

    /**
     * previous version for "on-update" mode
     */
    private NutsVersion appPreviousVersion;

    /**
     * auto complete info for "auto-complete" mode
     */
    private NutsCommandAutoComplete autoComplete;

    private String[] modeArgs = new String[0];

    public DefaultNutsApplicationContext(NutsWorkspace workspace, Class appClass, String storeId) {
        this(workspace,
                workspace.config().getOptions().getApplicationArguments(),
                appClass,
                storeId
        );
    }

    public DefaultNutsApplicationContext(NutsWorkspace workspace, String[] args, Class appClass, String storeId) {
        int wordIndex = -1;
        if (args.length > 0 && args[0].startsWith("--nuts-exec-mode=")) {
            String[] execModeCommand = NutsCommandLine.parseCommandLine(args[0].substring(args[0].indexOf('=') + 1));
            if (execModeCommand.length > 0) {
                switch (execModeCommand[0]) {
                    case "auto-complete": {
                        mode = execModeCommand[0];
                        if (execModeCommand.length > 1) {
                            wordIndex = Integer.parseInt(execModeCommand[1]);
                        }
                        modeArgs = Arrays.copyOfRange(execModeCommand, 1, execModeCommand.length);
                        break;
                    }
                    case "on-install": {
                        mode = execModeCommand[0];
                        modeArgs = Arrays.copyOfRange(execModeCommand, 1, execModeCommand.length);
                        break;
                    }
                    case "on-uninstall": {
                        mode = execModeCommand[0];
                        modeArgs = Arrays.copyOfRange(execModeCommand, 1, execModeCommand.length);
                        break;
                    }
                    case "on-update": {
                        mode = execModeCommand[0];
                        if (execModeCommand.length > 1) {
                            appPreviousVersion = workspace.parser().parseVersion(execModeCommand[1]);
                        }
                        modeArgs = Arrays.copyOfRange(execModeCommand, 1, execModeCommand.length);
                        break;
                    }
                    default: {
                        throw new NutsExecutionException("Unsupported nuts-exec-mode : " + args[0], 205);
                    }
                }
            }
            args = Arrays.copyOfRange(args, 1, args.length);
        }
        NutsId appId = workspace.resolveIdForClass(appClass);
        if (appId == null) {
            throw new NutsExecutionException("Invalid Nuts Application (" + appClass.getName() + "). Id cannot be resolved", 203);
        }
        this.setWorkspace(workspace);
        this.setArgs(args);
        this.setAppId(appId);
        this.appClass = appClass;
        this.setStoreId(storeId == null ? getAppId().toString() : storeId);
        setSession(workspace.createSession());
        terminal = getSession().getTerminal();
        setOut0(getTerminal().fout());
        setErr0(getTerminal().ferr());
        setOut(getOut0());
        setErr(getErr0());
        setProgramsFolder(workspace.config().getStoreLocation(getStoreId(), NutsStoreLocation.PROGRAMS));
        setConfigFolder(workspace.config().getStoreLocation(getStoreId(), NutsStoreLocation.CONFIG));
        setLogsFolder(workspace.config().getStoreLocation(getStoreId(), NutsStoreLocation.LOGS));
        setTempFolder(workspace.config().getStoreLocation(getStoreId(), NutsStoreLocation.TEMP));
        setVarFolder(workspace.config().getStoreLocation(getStoreId(), NutsStoreLocation.VAR));
        setLibFolder(workspace.config().getStoreLocation(getStoreId(), NutsStoreLocation.LIB));
        setCacheFolder(workspace.config().getStoreLocation(getStoreId(), NutsStoreLocation.CACHE));
        if ("auto-complete".equals(mode)) {
            setTerminalMode(NutsTerminalMode.FILTERED);
            if (wordIndex < 0) {
                wordIndex = args.length;
            }
            autoComplete = new AppCommandAutoComplete(args, wordIndex, out());
        } else {
            autoComplete = null;
        }
        tableCellFormatter = new NutsColoredCellFormatter(this);
        workspace.addWorkspaceListener(new NutsWorkspaceListenerAdapter() {
            @Override
            public void onUpdateProperty(String property, Object oldValue, Object newValue) {
                switch (property) {
                    case "systemTerminal": {
                        break;
                    }
                }
            }
        });
    }

    public String getMode() {
        return mode;
    }

    public DefaultNutsApplicationContext setMode(String mode) {
        this.mode = mode;
        return this;
    }

    public String[] getModeArgs() {
        return modeArgs;
    }

    public DefaultNutsApplicationContext setModeArgs(String[] modeArgs) {
        this.modeArgs = modeArgs;
        return this;
    }

    public NutsCommandAutoComplete getAutoComplete() {
        return autoComplete;
    }

    public boolean configure(NutsCommandLine cmd) {
        NutsArgument a;
        if ((a = cmd.readOption("--help")) != null) {
            if (cmd.isExecMode()) {
                showHelp();
                cmd.skipAll();
            }
            throw new NutsExecutionException("Help", 0);
        } else if ((a = cmd.readBooleanOption("--version")) != null) {
            if (cmd.isExecMode()) {
                out().printf("%s%n", getWorkspace().resolveIdForClass(getClass()).getVersion().toString());
                cmd.skipAll();
            }
            throw new NutsExecutionException("Help", 0);
        } else if ((a = cmd.readOption("--term-system")) != null) {
            setTerminalMode(null);
        } else if ((a = cmd.readOption("--term-filtered")) != null) {
            setTerminalMode(NutsTerminalMode.FILTERED);
        } else if ((a = cmd.readOption("--term-formatted")) != null) {
            setTerminalMode(NutsTerminalMode.FORMATTED);
        } else if ((a = cmd.readOption("--term-inherited")) != null) {
            setTerminalMode(NutsTerminalMode.INHERITED);
        } else if ((a = cmd.readOption("--no-color")) != null) {
            setTerminalMode(NutsTerminalMode.FILTERED);
        } else if ((a = cmd.readStringOption("--term")) != null) {
            String s = a.getValue().getString().toLowerCase();
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
        } else if ((a = cmd.readStringOption("--color")) != null) {
            String s = a.getValue().getString().toLowerCase();
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
                    setTerminalMode(cmd.newArgument(s).getBoolean(false) ? NutsTerminalMode.FORMATTED : NutsTerminalMode.FILTERED);
                }
            }
            return true;
        } else if ((a = cmd.readBooleanOption("--verbose")) != null) {
            this.setVerbose((a.getBooleanValue()));
            return true;
        }
        return false;
    }

    public void showHelp() {
        String h = getWorkspace().resolveDefaultHelpForClass(getAppClass());
        if (h == null) {
            h = "Help is @@missing@@.";
        }
        out().print(h);
    }

    public void setTerminalMode(NutsTerminalMode mode) {
        getWorkspace().getSystemTerminal().setMode(mode);
    }

    public Class getAppClass() {
        return appClass;
    }

    public NutsSessionTerminal terminal() {
        return terminal;
    }

    public NutsSessionTerminal getTerminal() {
        return terminal;
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    public DefaultNutsApplicationContext setWorkspace(NutsWorkspace workspace) {
        this.workspace = workspace;
        return this;
    }

    public NutsSession getSession() {
        return session;
    }

    public DefaultNutsApplicationContext setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    public PrintStream out() {
        return out;
    }

    public DefaultNutsApplicationContext setOut(PrintStream out) {
        this.out = out;
        return this;
    }

    public PrintStream err() {
        return err;
    }

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

    @Override
    public DefaultNutsApplicationContext setConfigFolder(Path configFolder) {
        this.configFolder = configFolder;
        return this;
    }

    @Override
    public Path getLogsFolder() {
        return logsFolder;
    }

    @Override
    public DefaultNutsApplicationContext setLogsFolder(Path logsFolder) {
        this.logsFolder = logsFolder;
        return this;
    }

    @Override
    public Path getTempFolder() {
        return tempFolder;
    }

    @Override
    public DefaultNutsApplicationContext setTempFolder(Path tempFolder) {
        this.tempFolder = tempFolder;
        return this;
    }

    @Override
    public Path getVarFolder() {
        return varFolder;
    }

    @Override
    public DefaultNutsApplicationContext setVarFolder(Path varFolder) {
        this.varFolder = varFolder;
        return this;
    }

    @Override
    public String getStoreId() {
        return storeId;
    }

    @Override
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
    public String[] getArgs() {
        return args;
    }

    @Override
    public DefaultNutsApplicationContext setArgs(String[] args) {
        this.args = args;
        return this;
    }

    @Override
    public NutsCommandLine newCommandLine() {
        return workspace.parser().parseCommandLine(getArgs()).setAutoComplete(getAutoComplete());
    }

    private static class AppCommandAutoComplete extends NutsCommandAutoCompleteBase {

        private ArrayList<String> words;
        int wordIndex;
        private PrintStream out0;

        public AppCommandAutoComplete(String[] args, int wordIndex, PrintStream out0) {
            words = new ArrayList<>(Arrays.asList(args));
            this.wordIndex = wordIndex;
            this.out0 = out0;
        }

        @Override
        protected NutsArgumentCandidate addCandidatesImpl(NutsArgumentCandidate value) {
            NutsArgumentCandidate c = super.addCandidatesImpl(value);
            String v = value.getValue();
            if (v == null) {
                throw new NutsExecutionException("Candidate cannot be null", 2);
            }
            String d = value.getDisplay();
            if (Objects.equals(v, d) || d == null) {
                out0.printf("%s%n", AUTO_COMPLETE_CANDIDATE_PREFIX + NutsCommandLine.escapeArgument(v));
            } else {
                out0.printf("%s%n", AUTO_COMPLETE_CANDIDATE_PREFIX + NutsCommandLine.escapeArgument(v) + " " + NutsCommandLine.escapeArgument(d));
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

    public NutsTerminalMode getTerminalMode() {
        return getWorkspace().getSystemTerminal().getOutMode();
    }

    public NutsTableCellFormat getTableCellFormatter() {
        return tableCellFormatter;
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public DefaultNutsApplicationContext setStartTimeMillis(long startTimeMillis) {
        this.startTimeMillis = startTimeMillis;
        return this;
    }

    public NutsVersion getAppPreviousVersion() {
        return appPreviousVersion;
    }

    public DefaultNutsApplicationContext setAppPreviousVersion(NutsVersion previousVersion) {
        this.appPreviousVersion = previousVersion;
        return this;
    }

    public Path getLibFolder() {
        return libFolder;
    }

    public DefaultNutsApplicationContext setLibFolder(Path libFolder) {
        this.libFolder = libFolder;
        return this;
    }

    public Path getCacheFolder() {
        return cacheFolder;
    }

    public DefaultNutsApplicationContext setCacheFolder(Path cacheFolder) {
        this.cacheFolder = cacheFolder;
        return this;
    }

}
