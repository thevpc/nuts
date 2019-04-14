package net.vpc.app.nuts.toolbox.ndi;

import net.vpc.app.nuts.NutsExecutionType;

import java.util.List;

public class NdiScriptOptions {
    private String id;
    private boolean force;
    private boolean forceBoot;
    private boolean trace=true;
    private boolean fetch;
    private NutsExecutionType execType;
    private List<String> executorOptions;

    public String getId() {
        return id;
    }

    public NdiScriptOptions setId(String id) {
        this.id = id;
        return this;
    }

    public boolean isForce() {
        return force;
    }

    public NdiScriptOptions setForce(boolean force) {
        this.force = force;
        return this;
    }

    public boolean isForceBoot() {
        return forceBoot;
    }

    public NdiScriptOptions setForceBoot(boolean forceBoot) {
        this.forceBoot = forceBoot;
        return this;
    }

    public boolean isTrace() {
        return trace;
    }

    public NdiScriptOptions setTrace(boolean trace) {
        this.trace = trace;
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
}
