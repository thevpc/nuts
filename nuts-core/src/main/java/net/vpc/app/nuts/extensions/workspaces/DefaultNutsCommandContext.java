package net.vpc.app.nuts.extensions.workspaces;

import net.vpc.app.nuts.*;

import java.util.HashMap;
import java.util.Map;

public class DefaultNutsCommandContext implements NutsCommandContext{
    private String serviceName;
    private NutsWorkspace workspace;
    private NutsCommandLineConsoleComponent commandLine;
    private NutsSession session;
    private Map<String,Object> userProperties=new HashMap<>();

    public DefaultNutsCommandContext() {
    }

    public NutsCommandContext copy() {
        DefaultNutsCommandContext c = new DefaultNutsCommandContext();
        c.serviceName = serviceName;
        c.workspace = workspace;
        c.commandLine = commandLine;
        c.session = session.copy();
        return c;
    }

    public Map<String, Object> getUserProperties() {
        return userProperties;
    }

    public String getServiceName() {
        return serviceName;
    }

    public NutsCommandContext setServiceName(String serviceName) {
        this.serviceName = serviceName;
        return this;
    }

    public NutsSession getSession() {
        return session;
    }

    public NutsCommandContext setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    public NutsTerminal getTerminal() {
        return session.getTerminal();
    }

    public NutsCommandLineConsoleComponent getCommandLine() {
        return commandLine;
    }

    public void setCommandLine(NutsCommandLineConsoleComponent commandLine) {
        this.commandLine = commandLine;
    }

    public NutsWorkspace getValidWorkspace() {
        if (workspace == null) {
            throw new IllegalArgumentException("No valid Workspace openWorkspace");
        }
        return workspace;
    }

    public NutsWorkspace getWorkspace() {
        return workspace;
    }

    public void setWorkspace(NutsWorkspace workspace) {
        this.workspace = workspace;
    }
}
