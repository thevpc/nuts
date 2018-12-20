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
    private boolean requiredExit;
    private boolean noColors;
    private long startTimeMillis;
    private int exitCode;
    private String[] args;
    private CommandAutoComplete autoComplete;
    private TableFormatter.CellFormatter tableCellFormatter;


    public NutsApplicationContext(NutsWorkspace workspace, Class appClass, String storeId) {
        String[] args = workspace.getBootOptions().getApplicationArguments();
        int wordIndex = -1;
        if (args.length > 0 && args[0].startsWith("--nuts-autocomplete-index=")) {
            wordIndex = Integer.parseInt(args[0].substring(args[0].indexOf('=') + 1));
            args = Arrays.copyOfRange(args, 1, args.length);
        }
        NutsId appId = workspace.resolveNutsIdForClass(appClass);
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
        setProgramsFolder(workspace.getStoreRoot(getStoreId(), RootFolderType.PROGRAMS));
        setConfigFolder(workspace.getStoreRoot(getStoreId(), RootFolderType.CONFIG));
        setLogsFolder(workspace.getStoreRoot(getStoreId(), RootFolderType.LOGS));
        setTempFolder(workspace.getStoreRoot(getStoreId(), RootFolderType.TEMP));
        setVarFolder(workspace.getStoreRoot(getStoreId(), RootFolderType.VAR));
        if (wordIndex >= 0) {
            setNoColors(true);
        }
        autoComplete = wordIndex >= 0 ? new AppCommandAutoComplete(args, wordIndex
//                ,workspace.createPrintStream(System.out,false)
                , out()
        ) : null;
        tableCellFormatter = new ColoredCellFormatter(this);
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
            setRequiredExit(true);
            setExitCode(0);
            return true;
        } else if ((a = cmd.readBooleanOption("--version")) != null) {
            if (cmd.isExecMode()) {
                out().printf("%s\n", getWorkspace().resolveNutsIdForClass(getClass()).getVersion().toString());
                cmd.skipAll();
            }
            setRequiredExit(true);
            setExitCode(0);
            return true;
        } else if ((a = cmd.readBooleanOption("--no-colors")) != null) {
            setNoColors(a.getBooleanValue());
            return true;
        } else if ((a = cmd.readBooleanOption("--verbose")) != null) {
            this.setVerbose((a.getBooleanValue()));
            return true;
        }
        return false;
    }

    public PrintStream showHelp() {
        String h = getWorkspace().resolveDefaultHelpForClass(getAppClass());
        if (h == null) {
            h = "Help is @@missing@@.";
        }
        return out().printf(h);
    }

    public void setNoColors(boolean b) {
        noColors = b;
        if (noColors) {
            setOut(getWorkspace().createPrintStream(getOut0(), true, true));
            setErr(getWorkspace().createPrintStream(getErr0(), true, true));
        } else {
            setOut(getOut0());
            setErr(getErr0());
        }
    }

    public boolean isRequiredExit() {
        return requiredExit;
    }

    public int getExitCode() {
        return exitCode;
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

    public NutsApplicationContext setRequiredExit(boolean requiredExit) {
        this.requiredExit = requiredExit;
        return this;
    }

    public NutsApplicationContext setExitCode(int exitCode) {
        this.exitCode = exitCode;
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
                throw new IllegalArgumentException("Candidate cannot be null");
            }
            String d = value.getDisplay();
            if (Objects.equals(v, d) || d == null) {
                out0.printf("%s\n", AUTO_COMPLETE_CANDIDATE_PREFIX + escapeArgument(v));
            } else {
                out0.printf("%s\n", AUTO_COMPLETE_CANDIDATE_PREFIX + escapeArgument(v) + " " + escapeArgument(d));
            }
            return c;
        }

        private String escapeArgument(String arg) {
            StringBuilder sb = new StringBuilder();
            boolean s = false;
            if (arg != null) {
                for (char c : arg.toCharArray()) {
                    switch (c) {
                        case '\'':
                        case '\\': {
                            sb.append('\\');
                            sb.append(c);
                            s = true;
                            break;
                        }
                        case ' ': {
                            s = true;
                            sb.append(c);
                            break;
                        }
                        default: {
                            sb.append(c);
                        }
                    }
                }
            }
            if (s || sb.length() == 0) {
                sb.insert(0, '\'');
                sb.append('\'');
            }
            return sb.toString();
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

    public boolean isNoColors() {
        return noColors;
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

}
