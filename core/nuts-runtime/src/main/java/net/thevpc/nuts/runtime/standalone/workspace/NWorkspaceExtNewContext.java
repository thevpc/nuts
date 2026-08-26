package net.thevpc.nuts.runtime.standalone.workspace;

import net.thevpc.nuts.app.NApplication;

import java.util.Map;

public class NWorkspaceExtNewContext extends NWorkspaceExtAdapter{
    public Map<String, String> env;
    public NApplication app;

    public NWorkspaceExtNewContext(NWorkspaceExt baseExt, Map<String, String> env, NApplication app) {
        super(baseExt);
        this.env = env;
        this.app = app;
    }

    @Override
    public Map<String, String> getSysEnv() {
        return env;
    }

    @Override
    public NApplication getApp() {
        return app;
    }
}
