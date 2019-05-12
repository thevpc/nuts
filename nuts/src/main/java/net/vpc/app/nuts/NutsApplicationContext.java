package net.vpc.app.nuts;


import java.io.PrintStream;
import java.nio.file.Path;

public interface NutsApplicationContext extends NutsCommandLineContext {
    public static final String AUTO_COMPLETE_CANDIDATE_PREFIX = "@@Candidate@@: ";

    public String getMode();

    public NutsApplicationContext setMode(String mode);

    public String[] getModeArgs();

    public NutsApplicationContext setModeArgs(String[] modeArgs);

    @Override
    public NutsCommandAutoComplete getAutoComplete();

    public boolean configure(NutsCommandLine cmd);

    public void showHelp();

    public void setTerminalMode(NutsTerminalMode mode);

    public Class getAppClass();

    public NutsSessionTerminal terminal();

    public NutsSessionTerminal getTerminal();

    public NutsWorkspace getWorkspace();

    public NutsApplicationContext setWorkspace(NutsWorkspace workspace);

    public NutsSession getSession();

    public NutsApplicationContext setSession(NutsSession session);

    public PrintStream out();

    public NutsApplicationContext setOut(PrintStream out);

    public PrintStream err();

    public NutsApplicationContext setErr(PrintStream err);

    public Path getProgramsFolder();

    public NutsApplicationContext setProgramsFolder(Path programsFolder);

    public Path getConfigFolder();

    public NutsApplicationContext setConfigFolder(Path configFolder);

    public Path getLogsFolder();

    public NutsApplicationContext setLogsFolder(Path logsFolder);

    public Path getTempFolder();

    public NutsApplicationContext setTempFolder(Path tempFolder);

    public Path getVarFolder();

    public NutsApplicationContext setVarFolder(Path varFolder);

    public String getStoreId();

    public NutsApplicationContext setStoreId(String storeId);

    public NutsId getAppId();

    public NutsVersion getAppVersion();

    public NutsApplicationContext setAppId(NutsId appId);

    public boolean isVerbose();

    public NutsApplicationContext setVerbose(boolean verbose);

    public String[] getArgs();

    public NutsApplicationContext setArgs(String[] args);

    public NutsTerminalMode getTerminalMode();

    public NutsTableCellFormat getTableCellFormatter();

    public long getStartTimeMillis();

    public NutsApplicationContext setStartTimeMillis(long startTimeMillis);

    public NutsVersion getAppPreviousVersion();

    public NutsApplicationContext setAppPreviousVersion(NutsVersion previousVersion);

    public Path getLibFolder();

    public NutsApplicationContext setLibFolder(Path libFolder);

    public Path getCacheFolder();

    public NutsApplicationContext setCacheFolder(Path cacheFolder);
    
    public NutsCommandLine newCommandLine();

}
