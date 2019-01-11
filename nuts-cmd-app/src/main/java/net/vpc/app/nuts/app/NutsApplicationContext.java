package net.vpc.app.nuts.app;

import net.vpc.app.nuts.*;
import net.vpc.common.commandline.*;
import net.vpc.common.commandline.format.TableFormatter;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class NutsApplicationContext implements CommandLineContext {
    public static final String AUTO_COMPLETE_CANDIDATE_PREFIX = "@@Candidate@@: ";
    private final Class appClass;
    private final NutsTerminal terminal;
    private NutsWorkspace workspace;
    private NutsSession session;
    private PrintStream out;
    private PrintStream err;
    private PrintStream out0;
    private PrintStream err0;
    private String programsFolder;
    private String configFolder;
    private String logsFolder;
    private String tempFolder;
    private String varFolder;
    private String storeId;
    private NutsId appId;
    private boolean verbose;
    private NutsTerminalMode terminalMode;
    private long startTimeMillis;
    private String[] args;
    private TableFormatter.CellFormatter tableCellFormatter;
    private String mode = "launch";

    /**
     * previous version for "on-update" mode
     */
    private NutsVersion appPreviousVersion;

    /**
     * auto complete info for "auto-complete" mode
     */
    private CommandAutoComplete autoComplete;

    private String[] modeArgs = new String[0];


    public NutsApplicationContext(NutsWorkspace workspace, Class appClass, String storeId) {
        String[] args = workspace.getConfigManager().getOptions().getApplicationArguments();
        int wordIndex = -1;
        if (args.length > 0 && args[0].startsWith("--nuts-execution-mode=")) {
            String[] execModeCommand = NutsMinimalCommandLine.parseCommandLine(args[0].substring(args[0].indexOf('=') + 1));
            if (execModeCommand.length > 0) {
                switch (execModeCommand[0]) {
                    case "auto-complete": {
                        mode = "auto-complete";
                        if (execModeCommand.length > 1) {
                            wordIndex = Integer.parseInt(execModeCommand[1]);
                        }
                        modeArgs = Arrays.copyOfRange(execModeCommand, 1, execModeCommand.length);
                        break;
                    }
                    case "on-install": {
                        mode = "on-install";
                        modeArgs = Arrays.copyOfRange(execModeCommand, 1, execModeCommand.length);
                        break;
                    }
                    case "on-uninstall": {
                        mode = "on-uninstall";
                        modeArgs = Arrays.copyOfRange(execModeCommand, 1, execModeCommand.length);
                        break;
                    }
                    case "on-update": {
                        mode = "on-uninstall";
                        if (execModeCommand.length > 1) {
                            appPreviousVersion = workspace.getParseManager().parseVersion(execModeCommand[1]);
                        }
                        modeArgs = Arrays.copyOfRange(execModeCommand, 1, execModeCommand.length);
                        break;
                    }
                    default: {
                        throw new NutsExecutionException("Unsupported nuts-execution-mode : " + args[0], 205);
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
        this.setStoreId(storeId == null ? getAppId().setVersion("LATEST").toString() : storeId);
        setSession(workspace.createSession());
        terminal = getSession().getTerminal();
        setOut0(getTerminal().getFormattedOut());
        setErr0(getTerminal().getFormattedErr());
        setOut(getOut0());
        setErr(getErr0());
        setProgramsFolder(workspace.getConfigManager().getStoreLocation(getStoreId(), StoreFolder.PROGRAMS));
        setConfigFolder(workspace.getConfigManager().getStoreLocation(getStoreId(), StoreFolder.CONFIG));
        setLogsFolder(workspace.getConfigManager().getStoreLocation(getStoreId(), StoreFolder.LOGS));
        setTempFolder(workspace.getConfigManager().getStoreLocation(getStoreId(), StoreFolder.TEMP));
        setVarFolder(workspace.getConfigManager().getStoreLocation(getStoreId(), StoreFolder.VAR));
        if (wordIndex >= 0) {
            setTerminalMode(NutsTerminalMode.FILTERED);
        }
        autoComplete = wordIndex >= 0 ? new AppCommandAutoComplete(args, wordIndex, out()) : null;
        tableCellFormatter = new ColoredCellFormatter(this);
    }

    public String getMode() {
        return mode;
    }

    public NutsApplicationContext setMode(String mode) {
        this.mode = mode;
        return this;
    }

    public String[] getModeArgs() {
        return modeArgs;
    }

    public NutsApplicationContext setModeArgs(String[] modeArgs) {
        this.modeArgs = modeArgs;
        return this;
    }

    public CommandAutoComplete getAutoComplete() {
        return autoComplete;
    }

    public boolean configure(CommandLine cmd) {
        Argument a;
        if ((a = cmd.readOption("--help")) != null) {
            if (cmd.isExecMode()) {
                showHelp();
                cmd.skipAll();
            }
            throw new NutsExecutionException("Help", 0);
        } else if ((a = cmd.readBooleanOption("--version")) != null) {
            if (cmd.isExecMode()) {
                out().printf("%s\n", getWorkspace().resolveIdForClass(getClass()).getVersion().toString());
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
        } else if ((a = cmd.readOption("--no-colors")) != null) {
            setTerminalMode(NutsTerminalMode.FILTERED);
        } else if ((a = cmd.readStringOption("--term")) != null) {
            String s=a.getStringValue().toLowerCase();
            switch (s){
                case "":
                case "system":
                    {
                    setTerminalMode(null);
                    break;
                }
                case "filtered":{
                    setTerminalMode(NutsTerminalMode.FILTERED);
                    break;
                }
                case "formatted":{
                    setTerminalMode(NutsTerminalMode.FORMATTED);
                    break;
                }
                case "inherited":{
                    setTerminalMode(NutsTerminalMode.INHERITED);
                    break;
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

    public NutsTerminal terminal() {
        return terminal;
    }

    public NutsTerminal getTerminal() {
        return terminal;
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    public NutsApplicationContext setWorkspace(NutsWorkspace workspace) {
        this.workspace = workspace;
        return this;
    }

    public NutsSession getSession() {
        return session;
    }

    public NutsApplicationContext setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    public PrintStream out() {
        return out;
    }

    public NutsApplicationContext setOut(PrintStream out) {
        this.out = out;
        return this;
    }

    public PrintStream err() {
        return err;
    }

    public NutsApplicationContext setErr(PrintStream err) {
        this.err = err;
        return this;
    }

    protected PrintStream getOut0() {
        return out0;
    }

    protected NutsApplicationContext setOut0(PrintStream out0) {
        this.out0 = out0;
        return this;
    }

    protected PrintStream getErr0() {
        return err0;
    }

    protected NutsApplicationContext setErr0(PrintStream err0) {
        this.err0 = err0;
        return this;
    }

    public String getProgramsFolder() {
        return programsFolder;
    }

    public NutsApplicationContext setProgramsFolder(String programsFolder) {
        this.programsFolder = programsFolder;
        return this;
    }

    public String getConfigFolder() {
        return configFolder;
    }

    public NutsApplicationContext setConfigFolder(String configFolder) {
        this.configFolder = configFolder;
        return this;
    }

    public String getLogsFolder() {
        return logsFolder;
    }

    public NutsApplicationContext setLogsFolder(String logsFolder) {
        this.logsFolder = logsFolder;
        return this;
    }

    public String getTempFolder() {
        return tempFolder;
    }

    public NutsApplicationContext setTempFolder(String tempFolder) {
        this.tempFolder = tempFolder;
        return this;
    }

    public String getVarFolder() {
        return varFolder;
    }

    public NutsApplicationContext setVarFolder(String varFolder) {
        this.varFolder = varFolder;
        return this;
    }

    public String getStoreId() {
        return storeId;
    }

    public NutsApplicationContext setStoreId(String storeId) {
        this.storeId = storeId;
        return this;
    }

    public NutsId getAppId() {
        return appId;
    }

    public NutsVersion getAppVersion() {
        return appId == null ? null : appId.getVersion();
    }

    public NutsApplicationContext setAppId(NutsId appId) {
        this.appId = appId;
        return this;
    }

    public boolean isVerbose() {
        return verbose;
    }

    public NutsApplicationContext setVerbose(boolean verbose) {
        this.verbose = verbose;
        return this;
    }

    public String[] getArgs() {
        return args;
    }

    public NutsApplicationContext setArgs(String[] args) {
        this.args = args;
        return this;
    }

    private static class AppCommandAutoComplete extends AbstractCommandAutoComplete {
        private ArrayList<String> words;
        int wordIndex;
        private PrintStream out0;

        public AppCommandAutoComplete(String[] args, int wordIndex, PrintStream out0) {
            words = new ArrayList<>(Arrays.asList(args));
            this.wordIndex = wordIndex;
            this.out0 = out0;
        }

        @Override
        protected ArgumentCandidate addCandidatesImpl(ArgumentCandidate value) {
            ArgumentCandidate c = super.addCandidatesImpl(value);
            String v = value.getValue();
            if (v == null) {
                throw new NutsExecutionException("Candidate cannot be null", 2);
            }
            String d = value.getDisplay();
            if (Objects.equals(v, d) || d == null) {
                out0.printf("%s\n", AUTO_COMPLETE_CANDIDATE_PREFIX + NutsMinimalCommandLine.escapeArgument(v));
            } else {
                out0.printf("%s\n", AUTO_COMPLETE_CANDIDATE_PREFIX + NutsMinimalCommandLine.escapeArgument(v) + " " + NutsMinimalCommandLine.escapeArgument(d));
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

    public TableFormatter.CellFormatter getTableCellFormatter() {
        return tableCellFormatter;
    }

    public long getStartTimeMillis() {
        return startTimeMillis;
    }

    public NutsApplicationContext setStartTimeMillis(long startTimeMillis) {
        this.startTimeMillis = startTimeMillis;
        return this;
    }

    public NutsVersion getAppPreviousVersion() {
        return appPreviousVersion;
    }

    public NutsApplicationContext setAppPreviousVersion(NutsVersion previousVersion) {
        this.appPreviousVersion = previousVersion;
        return this;
    }
}
