package net.thevpc.nuts.runtime.standalone.main.config;

import net.thevpc.nuts.NutsConfigItem;
import net.thevpc.nuts.NutsUserConfig;

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
