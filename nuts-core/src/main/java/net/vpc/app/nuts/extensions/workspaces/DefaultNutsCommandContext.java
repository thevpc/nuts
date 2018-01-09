package net.vpc.app.nuts.extensions.workspaces;

import net.vpc.app.nuts.*;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

public class DefaultNutsCommandContext implements NutsCommandContext{
    private String serviceName;
    private NutsWorkspace workspace;
    private NutsCommandLineConsoleComponent commandLine;
    private NutsSession session;
    private Map<String,Object> userProperties=new HashMap<>();
    private Properties env=new Properties();

    public DefaultNutsCommandContext() {
    }

    public NutsCommandContext copy() {
        DefaultNutsCommandContext c = new DefaultNutsCommandContext();
        c.serviceName = serviceName;
        c.workspace = workspace;
        c.commandLine = commandLine;
        c.session = session.copy();
        c.env = new Properties();
        c.env.putAll(env);
        c.userProperties = new HashMap<>();
        c.userProperties.putAll(userProperties);
        return c;
    }

    @Override
    public Properties getEnv() {
        return env;
    }

    @Override
    public NutsCommandContext setEnv(Properties env) {
        this.env =new Properties();
        if(env!=null){
            this.env.putAll(env);
        }
        return this;
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
