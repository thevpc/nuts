package net.thevpc.nuts.toolbox.nadmin.subcommands.ndi;

import net.thevpc.nuts.NutsExecutionType;

import java.util.ArrayList;
import java.util.List;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.base.NutsEnvInfo;

public class NdiScriptOptions {

    private String id;
    private boolean forceBoot;
    private boolean fetch;
    private NutsExecutionType execType;
    private List<String> executorOptions;
    private NutsSession session;
    private String preferredScriptName;
    private boolean includeEnv;
    private boolean addNutsScript;

    private List<String> appArgs= new ArrayList<>();
    private String cwd;
    private String icon;
    private String menuPath;
    private boolean persistentConfig;
    private boolean terminalMode;
    private boolean createMenu;
    private boolean createDesktop;
    private boolean createShortcut;
    private NutsEnvInfo env;

    public boolean isTerminalMode() {
        return terminalMode;
    }

    public NdiScriptOptions setTerminalMode(boolean terminalMode) {
        this.terminalMode = terminalMode;
        return this;
    }

    public NdiScriptOptions(NutsEnvInfo env) {
        this.env = env;
    }

    public boolean isPersistentConfig() {
        return persistentConfig;
    }

    public NdiScriptOptions setPersistentConfig(boolean persistentConfig) {
        this.persistentConfig = persistentConfig;
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

    public List<String> getAppArgs() {
        return appArgs;
    }

    public NdiScriptOptions setAppArgs(List<String> appArgs) {
        this.appArgs = appArgs;
        return this;
    }

    public String getCwd() {
        return cwd;
    }

    public NdiScriptOptions setCwd(String cwd) {
        this.cwd = cwd;
        return this;
    }

    public String getIcon() {
        return icon;
    }

    public NdiScriptOptions setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public String getMenuPath() {
        return menuPath;
    }

    public NdiScriptOptions setMenuPath(String menuPath) {
        this.menuPath = menuPath;
        return this;
    }

    public boolean isCreateMenu() {
        return createMenu;
    }

    public NdiScriptOptions setCreateMenu(boolean createMenu) {
        this.createMenu = createMenu;
        return this;
    }

    public boolean isCreateShortcut() {
        return createShortcut;
    }

    public NdiScriptOptions setCreateShortcut(boolean createShortcut) {
        this.createShortcut = createShortcut;
        return this;
    }

    public boolean isCreateDesktop() {
        return createDesktop;
    }

    public NdiScriptOptions setCreateDesktop(boolean createDesktop) {
        this.createDesktop = createDesktop;
        return this;
    }
}
