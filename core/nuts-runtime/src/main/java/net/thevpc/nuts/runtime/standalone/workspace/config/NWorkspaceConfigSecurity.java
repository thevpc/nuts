package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.NConfigItem;
import net.thevpc.nuts.NUserConfig;

public class NWorkspaceConfigSecurity extends NConfigItem {
    private static final long serialVersionUID = 2;

    private boolean secure = false;
    private String authenticationAgent;
    private NUserConfig[] users;

    public boolean isSecure() {
        return secure;
    }

    public NWorkspaceConfigSecurity setSecure(boolean secure) {
        this.secure = secure;
        return this;
    }

    public String getAuthenticationAgent() {
        return authenticationAgent;
    }

    public NWorkspaceConfigSecurity setAuthenticationAgent(String authenticationAgent) {
        this.authenticationAgent = authenticationAgent;
        return this;
    }

    public NUserConfig[] getUsers() {
        return users;
    }

    public NWorkspaceConfigSecurity setUsers(NUserConfig[] users) {
        this.users = users;
        return this;
    }
}
