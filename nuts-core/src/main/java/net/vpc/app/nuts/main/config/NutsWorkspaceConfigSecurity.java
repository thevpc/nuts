package net.vpc.app.nuts.main.config;

import net.vpc.app.nuts.NutsConfigItem;
import net.vpc.app.nuts.NutsUserConfig;

import java.io.Serializable;

public class NutsWorkspaceConfigSecurity extends NutsConfigItem {
    private static final long serialVersionUID = 2;

    private boolean secure = false;
    private String authenticationAgent;
    private NutsUserConfig[] users;

    public boolean isSecure() {
        return secure;
    }

    public NutsWorkspaceConfigSecurity setSecure(boolean secure) {
        this.secure = secure;
        return this;
    }

    public String getAuthenticationAgent() {
        return authenticationAgent;
    }

    public NutsWorkspaceConfigSecurity setAuthenticationAgent(String authenticationAgent) {
        this.authenticationAgent = authenticationAgent;
        return this;
    }

    public NutsUserConfig[] getUsers() {
        return users;
    }

    public NutsWorkspaceConfigSecurity setUsers(NutsUserConfig[] users) {
        this.users = users;
        return this;
    }
}
