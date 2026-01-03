package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.app.NApp;

import java.util.Map;

public class NWorkspaceEnvScope {
    public Map<String, String> env;
    /**
     * using currentApp so that we can change NApp when calling embedded apps
     */
    public NApp currentApp;
}
