package net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.base;

import net.thevpc.nuts.runtime.standalone.wscommands.settings.subcommands.ndi.NdiScriptOptions;

import java.util.ArrayList;
import java.util.List;

public class CreateNutsScriptCommand {
    private List<String> idsToInstall= new ArrayList<>();
    private NdiScriptOptions options= new NdiScriptOptions();
//    private String switchWorkspaceLocation;
//    private String linkName;
//    private Boolean persistentConfig;
//    private List<String> executorOptions= new ArrayList<>();
//    private boolean env;
//    private boolean fetch;
//    private boolean terminalMode;
//    private NutsExecutionType execType;
//    private List<String> appArgs= new ArrayList<>();
//    private String cwd;
//    private String icon;
//    private String menuPath;
//    private boolean createShortcut;
//    private boolean createMenu;
//    private boolean createDesktop;

    public NdiScriptOptions getOptions() {
        return options;
    }


//    public boolean isTerminalMode() {
//        return terminalMode;
//    }
//
//    public CreateNutsScriptCommand setTerminalMode(boolean terminalMode) {
//        this.terminalMode = terminalMode;
//        return this;
//    }
//
//    public String getIcon() {
//        return icon;
//    }
//
//    public CreateNutsScriptCommand setIcon(String icon) {
//        this.icon = icon;
//        return this;
//    }
//
    public List<String> getIdsToInstall() {
        return idsToInstall;
    }

    public CreateNutsScriptCommand setIdsToInstall(List<String> idsToInstall) {
        this.idsToInstall = idsToInstall;
        return this;
    }
//
//    public String getSwitchWorkspaceLocation() {
//        return switchWorkspaceLocation;
//    }
//
//    public CreateNutsScriptCommand setSwitchWorkspaceLocation(String switchWorkspaceLocation) {
//        this.switchWorkspaceLocation = switchWorkspaceLocation;
//        return this;
//    }
//
//    public String getLinkName() {
//        return linkName;
//    }
//
//    public CreateNutsScriptCommand setLinkName(String linkName) {
//        this.linkName = linkName;
//        return this;
//    }
//
//    public Boolean getPersistentConfig() {
//        return persistentConfig;
//    }
//
//    public CreateNutsScriptCommand setPersistentConfig(Boolean persistentConfig) {
//        this.persistentConfig = persistentConfig;
//        return this;
//    }
//
//    public List<String> getExecutorOptions() {
//        return executorOptions;
//    }
//
//    public CreateNutsScriptCommand setExecutorOptions(List<String> executorOptions) {
//        this.executorOptions = executorOptions;
//        return this;
//    }
//
//    public boolean isEnv() {
//        return env;
//    }
//
//    public CreateNutsScriptCommand setEnv(boolean env) {
//        this.env = env;
//        return this;
//    }
//
//    public boolean isFetch() {
//        return fetch;
//    }
//
//    public CreateNutsScriptCommand setFetch(boolean fetch) {
//        this.fetch = fetch;
//        return this;
//    }
//
//    public NutsExecutionType getExecType() {
//        return execType;
//    }
//
//    public CreateNutsScriptCommand setExecType(NutsExecutionType execType) {
//        this.execType = execType;
//        return this;
//    }
//
//    public String getCwd() {
//        return cwd;
//    }
//
//    public CreateNutsScriptCommand setCwd(String cwd) {
//        this.cwd = cwd;
//        return this;
//    }
//
//    public List<String> getAppArgs() {
//        return appArgs;
//    }
//
//    public CreateNutsScriptCommand setAppArgs(List<String> appArgs) {
//        this.appArgs = appArgs;
//        return this;
//    }
//
//    public String getMenuPath() {
//        return menuPath;
//    }
//
//    public CreateNutsScriptCommand setMenuPath(String menuPath) {
//        this.menuPath = menuPath;
//        return this;
//    }
//
//    public boolean isCreateMenu() {
//        return createMenu;
//    }
//
//    public CreateNutsScriptCommand setCreateMenu(boolean createMenu) {
//        this.createMenu = createMenu;
//        return this;
//    }
//
//    public boolean isCreateDesktop() {
//        return createDesktop;
//    }
//
//    public CreateNutsScriptCommand setCreateDesktop(boolean createDesktop) {
//        this.createDesktop = createDesktop;
//        return this;
//    }
//
//
//    public boolean isCreateShortcut() {
//        return createShortcut;
//    }
//
//    public CreateNutsScriptCommand setCreateShortcut(boolean createShortcut) {
//        this.createShortcut = createShortcut;
//        return this;
//    }
}
