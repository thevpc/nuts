package net.vpc.app.nuts;

import java.io.PrintStream;
import java.nio.file.Path;

public interface NutsApplicationContext extends NutsCommandLineContext {

    public static final String AUTO_COMPLETE_CANDIDATE_PREFIX = "@@Candidate@@: ";

    public String getMode();

    public String[] getModeArgs();

    @Override
    public NutsCommandAutoComplete getAutoComplete();

    public boolean configure(NutsCommandLine cmd);

    public void showHelp();

    public void setTerminalMode(NutsTerminalMode mode);

    public Class getAppClass();

    public NutsSessionTerminal terminal();

    public NutsSessionTerminal getTerminal();

    public NutsWorkspace getWorkspace();

//    public NutsApplicationContext setModeArgs(String[] modeArgs);
//    public NutsApplicationContext setMode(String mode);
//    public NutsApplicationContext setWorkspace(NutsWorkspace workspace);
//    public NutsApplicationContext setProgramsFolder(Path programsFolder);
//    public NutsApplicationContext setConfigFolder(Path configFolder);
//    public NutsApplicationContext setLogsFolder(Path logsFolder);
//    public NutsApplicationContext setTempFolder(Path tempFolder);
//    public NutsApplicationContext setVarFolder(Path varFolder);
//    public NutsApplicationContext setStoreId(String storeId);
//    public NutsApplicationContext setArgs(String[] args);
//    public NutsApplicationContext setStartTimeMillis(long startTimeMillis);
//    public NutsApplicationContext setAppPreviousVersion(NutsVersion previousVersion);
//    public NutsApplicationContext setLibFolder(Path libFolder);
//    public NutsApplicationContext setCacheFolder(Path cacheFolder);
    public NutsSession getSession();

    public NutsApplicationContext setSession(NutsSession session);

    public PrintStream out();

    public NutsApplicationContext setOut(PrintStream out);

    public PrintStream err();

    public NutsApplicationContext setErr(PrintStream err);

    public Path getProgramsFolder();

    public Path getConfigFolder();

    public Path getLogsFolder();

    public Path getTempFolder();

    public Path getVarFolder();

    public Path getLibFolder();

    public Path getCacheFolder();

    public String getStoreId();

    public NutsId getAppId();

    public NutsVersion getAppVersion();

    public NutsApplicationContext setAppId(NutsId appId);

    public boolean isVerbose();

    public NutsApplicationContext setVerbose(boolean verbose);

    @Override
    public String[] getArgs();

    public NutsTerminalMode getTerminalMode();

    public long getStartTimeMillis();

    public NutsVersion getAppPreviousVersion();


    public NutsCommandLine getCommandLine();

    NutsCommandLine commandLine();

}
