package net.thevpc.nuts.toolbox.nadmin.subcommands.ndi;

import net.thevpc.nuts.NutsActionSupportCondition;
import net.thevpc.nuts.NutsExecutionType;

import java.util.ArrayList;
import java.util.List;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.toolbox.nadmin.subcommands.ndi.base.NutsEnvInfo;

public class NdiScriptOptions implements Cloneable{

    private String id;
    private boolean forceBoot;
    private boolean fetch;
    private NutsExecutionType execType;
    private List<String> executorOptions;
    private NutsSession session;
    private String scriptPath;
    private String shortcutName;
    private String shortcutPath;
    private boolean includeEnv;
    private boolean addNutsScript;

    private List<String> appArgs= new ArrayList<>();
    private String workingDirectory;
    private String icon;
    private String menuCategory;
    private Boolean persistentConfig;
    private boolean terminalMode;
    private NutsActionSupportCondition createMenu= NutsActionSupportCondition.NEVER;
    private NutsActionSupportCondition createDesktop= NutsActionSupportCondition.NEVER;
    private NutsActionSupportCondition createShortcut= NutsActionSupportCondition.NEVER;
    private NutsEnvInfo env;
    private String switchWorkspaceLocation;

    public String getShortcutName() {
        return shortcutName;
    }

    public NdiScriptOptions setShortcutName(String shortcutName) {
        this.shortcutName = shortcutName;
        return this;
    }

    public String getShortcutPath() {
        return shortcutPath;
    }

    public NdiScriptOptions setShortcutPath(String shortcutPath) {
        this.shortcutPath = shortcutPath;
        return this;
    }

    public boolean isTerminalMode() {
        return terminalMode;
    }

    public NdiScriptOptions setTerminalMode(boolean terminalMode) {
        this.terminalMode = terminalMode;
        return this;
    }

    public NdiScriptOptions() {
    }

    public NdiScriptOptions setEnv(NutsEnvInfo env) {
        this.env = env;
        return this;
    }

    public Boolean getPersistentConfig() {
        return persistentConfig;
    }

    public NdiScriptOptions setPersistentConfig(Boolean persistentConfig) {
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

    public String getScriptPath() {
        return scriptPath;
    }

    public NdiScriptOptions setScriptPath(String scriptPath) {
        this.scriptPath = scriptPath;
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

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public NdiScriptOptions setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
        return this;
    }

    public String getIcon() {
        return icon;
    }

    public NdiScriptOptions setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public String getMenuCategory() {
        return menuCategory;
    }

    public NdiScriptOptions setMenuCategory(String menuCategory) {
        this.menuCategory = menuCategory;
        return this;
    }

    public NutsActionSupportCondition getCreateMenu() {
        return createMenu;
    }

    public NdiScriptOptions setCreateMenu(NutsActionSupportCondition createMenu) {
        this.createMenu = createMenu;
        return this;
    }

    public NutsActionSupportCondition getCreateShortcut() {
        return createShortcut;
    }

    public NdiScriptOptions getCreateShortcut(NutsActionSupportCondition createShortcut) {
        this.createShortcut = createShortcut;
        return this;
    }

    public String getSwitchWorkspaceLocation() {
        return switchWorkspaceLocation;
    }

    public NdiScriptOptions setSwitchWorkspaceLocation(String switchWorkspaceLocation) {
        this.switchWorkspaceLocation = switchWorkspaceLocation;
        return this;
    }

    public NutsActionSupportCondition getCreateDesktop() {
        return createDesktop;
    }

    public NdiScriptOptions setCreateDesktop(NutsActionSupportCondition createDesktop) {
        this.createDesktop = createDesktop;
        return this;
    }

    public NdiScriptOptions copy() {
        NdiScriptOptions c;
        try {
            c=(NdiScriptOptions) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new IllegalArgumentException(e);
        }
        c.setAppArgs(c.getAppArgs()==null?null:new ArrayList<>(c.getAppArgs()));
        c.setExecutorOptions(c.getExecutorOptions()==null?null:new ArrayList<>(c.getExecutorOptions()));
        return c;
    }

}
