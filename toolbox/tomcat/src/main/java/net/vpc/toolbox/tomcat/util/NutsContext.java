package net.vpc.toolbox.tomcat.util;

import net.vpc.app.nuts.*;

public class NutsContext {
    public final NutsTerminal terminal;
    public NutsWorkspace ws;
    public NutsSession session;
    public NutsPrintStream out;
    public NutsPrintStream err;
    public String programsFolder;
    public String configFolder;
    public String logsFolder;
    public String tempFolder;
    public String varFolder;

    public NutsContext(NutsWorkspace ws) {
        this.ws = ws;
        session = ws.createSession();
        terminal = session.getTerminal();
        out = terminal.getFormattedOut();
        err = terminal.getFormattedErr();
        programsFolder = ws.getStoreRoot("net.vpc.app.nuts.toolbox:tomcat#LATEST", RootFolderType.PROGRAMS);
        configFolder = ws.getStoreRoot("net.vpc.app.nuts.toolbox:tomcat#LATEST", RootFolderType.CONFIG);
        logsFolder = ws.getStoreRoot("net.vpc.app.nuts.toolbox:tomcat#LATEST", RootFolderType.LOGS);
        tempFolder = ws.getStoreRoot("net.vpc.app.nuts.toolbox:tomcat#LATEST", RootFolderType.TEMP);
        varFolder = ws.getStoreRoot("net.vpc.app.nuts.toolbox:tomcat#LATEST", RootFolderType.VAR);
    }

    public String readOrCancel(String message, String defaultValue, Object... params) {
        if (message.endsWith("\n")) {
            message = message.substring(0, message.length() - 1);
        }
        out.printf(message, params);
        if (defaultValue != null) {
            out.printf(" (default %s)\n", defaultValue);
        }
        out.printf("\n");
        String v = terminal.readLine("\tPlease enter value or @@%s@@ to cancel : ", "cancel!");
        if ("cancel!".equals(v)) {
            throw new UserCancelException();
        }
        if (v == null) {
            return defaultValue;
        }
        return v;
    }
}
