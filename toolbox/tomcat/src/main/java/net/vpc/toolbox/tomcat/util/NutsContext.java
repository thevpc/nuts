package net.vpc.toolbox.tomcat.util;

import net.vpc.app.nuts.NutsPrintStream;
import net.vpc.app.nuts.NutsSession;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.RootFolderType;

public class NutsContext {
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
        session=ws.createSession();
        out=session.getTerminal().getFormattedOut();
        err=session.getTerminal().getFormattedErr();
        programsFolder = ws.getStoreRoot("net.vpc.app.nuts.toolbox:tomcat#LATEST", RootFolderType.PROGRAMS);
        configFolder = ws.getStoreRoot("net.vpc.app.nuts.toolbox:tomcat#LATEST", RootFolderType.CONFIG);
        logsFolder = ws.getStoreRoot("net.vpc.app.nuts.toolbox:tomcat#LATEST", RootFolderType.LOGS);
        tempFolder = ws.getStoreRoot("net.vpc.app.nuts.toolbox:tomcat#LATEST", RootFolderType.TEMP);
        varFolder = ws.getStoreRoot("net.vpc.app.nuts.toolbox:tomcat#LATEST", RootFolderType.VAR);
    }
}
