package net.thevpc.nuts.runtime.standalone.workspace;

import net.thevpc.nuts.app.NApp;

import java.util.Map;

public class NWorkspaceExtNewContext extends NWorkspaceExtAdapter{
    public Map<String, String> env;
    public NApp app;

    public NWorkspaceExtNewContext(NWorkspaceExt baseExt, Map<String, String> env, NApp app) {
        super(baseExt);
        this.env = env;
        this.app = app;
    }

    @Override
    public Map<String, String> getSysEnv() {
        return env;
    }

    @Override
    public NApp getApp() {
        return app;
    }
}
