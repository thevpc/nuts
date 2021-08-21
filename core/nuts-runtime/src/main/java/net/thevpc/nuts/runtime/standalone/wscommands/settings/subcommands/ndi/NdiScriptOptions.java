package net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.base.NutsEnvInfo;

import java.util.ArrayList;
import java.util.List;

public class NdiScriptOptions implements Cloneable{

    private String id;
    private boolean forceBoot;
    private boolean fetch;
//    private NutsExecutionType execType;
//    private NutsRunAs runAs=NutsRunAs.currentUser();
//    private List<String> executorOptions;
    private NutsSession session;
//    private String customScriptPath;
//    private String shortcutName;
//    private String customShortcutPath;
    private boolean includeEnv;
    private boolean addNutsScript;

//    private List<String> appArgs= new ArrayList<>();
//    private String workingDirectory;
//    private String icon;
//    private String menuCategory;
//    private Boolean systemWideConfig;
//    private boolean openTerminal;
//    private NutsActionSupportCondition createMenu= NutsActionSupportCondition.NEVER;
//    private NutsActionSupportCondition createDesktop= NutsActionSupportCondition.NEVER;
//    private NutsActionSupportCondition createShortcut= NutsActionSupportCondition.NEVER;
//    private String switchWorkspaceLocation;
    private NutsEnvInfo env;
    private NutsLauncherOptions launcher=new NutsLauncherOptions();

    public NutsLauncherOptions getLauncher() {
        return launcher;
    }

    public NdiScriptOptions setLauncher(NutsLauncherOptions launcher) {
        this.launcher = launcher;
        return this;
    }

    public NdiScriptOptions() {
    }

    public NdiScriptOptions setEnv(NutsEnvInfo env) {
        this.env = env;
        return this;
    }

    public NutsEnvInfo getEnv() {
        return env;
    }

    public boolean isAddNutsScript() {
        return addNutsScript;
    }

    public NdiScriptOptions setAddNutsScript(boolean addNutsScript) {
        this.addNutsScript = addNutsScript;
        return this;
    }

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

//    public List<String> getExecutorOptions() {
//        return executorOptions;
//    }
//
//    public NdiScriptOptions setExecutorOptions(List<String> executorOptions) {
//        this.executorOptions = executorOptions;
//        return this;
//    }

    public NutsSession getSession() {
        return session;
    }

    public NdiScriptOptions setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    public boolean isIncludeEnv() {
        return includeEnv;
    }

    public NdiScriptOptions setIncludeEnv(boolean includeEnv) {
        this.includeEnv = includeEnv;
        return this;
    }

    public NdiScriptOptions copy() {
        NdiScriptOptions c;
        try {
            c=(NdiScriptOptions) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalArgumentException(e);
        }
        c.setLauncher(c.getLauncher()==null?null:c.getLauncher().copy());
        return c;
    }

}
