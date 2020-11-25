package net.thevpc.nuts.toolbox.ndi;

import net.thevpc.nuts.NutsExecutionType;

import java.util.List;
import net.thevpc.nuts.NutsSession;

public class NdiScriptOptions {

    private String id;
    private boolean forceBoot;
    private boolean fetch;
    private NutsExecutionType execType;
    private List<String> executorOptions;
    private NutsSession session;
    private String preferredScriptName;
    private boolean includeEnv;

    public String getId() {
        return id;
    }

    public NdiScriptOptions setId(String id) {
        this.id = id;
        return this;
    }

    public boolean isForceBoot() {
        return forceBoot;
    }

    public NdiScriptOptions setForceBoot(boolean forceBoot) {
        this.forceBoot = forceBoot;
        return this;
    }

    public boolean isFetch() {
        return fetch;
    }

    public NdiScriptOptions setFetch(boolean fetch) {
        this.fetch = fetch;
        return this;
    }

    public NutsExecutionType getExecType() {
        return execType;
    }

    public NdiScriptOptions setExecType(NutsExecutionType execType) {
        this.execType = execType;
        return this;
    }

    public List<String> getExecutorOptions() {
        return executorOptions;
    }

    public NdiScriptOptions setExecutorOptions(List<String> executorOptions) {
        this.executorOptions = executorOptions;
        return this;
    }

    public NutsSession getSession() {
        return session;
    }

    public NdiScriptOptions setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    public String getPreferredScriptName() {
        return preferredScriptName;
    }

    public NdiScriptOptions setPreferredScriptName(String preferredScriptName) {
        this.preferredScriptName = preferredScriptName;
        return this;
    }

    public boolean isIncludeEnv() {
        return includeEnv;
    }

    public NdiScriptOptions setIncludeEnv(boolean includeEnv) {
        this.includeEnv = includeEnv;
        return this;
    }
}
