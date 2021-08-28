package net.thevpc.nuts;

import java.util.ArrayList;
import java.util.List;

public class NutsLauncherOptions implements Cloneable {
    private boolean createAlias;
    private boolean createScript;
    private NutsSupportCondition createMenuShortcut;
    private NutsSupportCondition createDesktopShortcut;
    private NutsSupportCondition createCustomShortcut;
    private boolean installExtensions;
    private String alias;
    private String shortcutName;
    private String customShortcutPath;
    private String customScriptPath;
    private String icon;
    private String menuCategory;
    private boolean openTerminal;

    private Boolean systemWideConfig;
    private NutsId id;
    private List<String> args = new ArrayList<>();

    private List<String> nutsOptions = new ArrayList<>();

    private String switchWorkspaceLocation;
    private String workingDirectory;

    public boolean isCreateScript() {
        return createScript;
    }

    public NutsLauncherOptions setCreateScript(boolean createScript) {
        this.createScript = createScript;
        return this;
    }

    public NutsSupportCondition getCreateMenuShortcut() {
        return createMenuShortcut;
    }

    public NutsLauncherOptions setCreateMenuShortcut(NutsSupportCondition createMenuShortcut) {
        this.createMenuShortcut = createMenuShortcut;
        return this;
    }

    public NutsSupportCondition getCreateDesktopShortcut() {
        return createDesktopShortcut;
    }

    public NutsLauncherOptions setCreateDesktopShortcut(NutsSupportCondition createDesktopShortcut) {
        this.createDesktopShortcut = createDesktopShortcut;
        return this;
    }

    public NutsSupportCondition getCreateCustomShortcut() {
        return createCustomShortcut;
    }

    public NutsLauncherOptions setCreateCustomShortcut(NutsSupportCondition createCustomShortcut) {
        this.createCustomShortcut = createCustomShortcut;
        return this;
    }

    public String getShortcutName() {
        return shortcutName;
    }

    public NutsLauncherOptions setShortcutName(String shortcutName) {
        this.shortcutName = shortcutName;
        return this;
    }

    public String getCustomShortcutPath() {
        return customShortcutPath;
    }

    public NutsLauncherOptions setCustomShortcutPath(String customShortcutPath) {
        this.customShortcutPath = customShortcutPath;
        return this;
    }

    public String getIcon() {
        return icon;
    }

    public NutsLauncherOptions setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public String getMenuCategory() {
        return menuCategory;
    }

    public NutsLauncherOptions setMenuCategory(String menuCategory) {
        this.menuCategory = menuCategory;
        return this;
    }

    public boolean isOpenTerminal() {
        return openTerminal;
    }

    public NutsLauncherOptions setOpenTerminal(boolean openTerminal) {
        this.openTerminal = openTerminal;
        return this;
    }

    public NutsId getId() {
        return id;
    }

    public NutsLauncherOptions setId(NutsId id) {
        this.id = id;
        return this;
    }

    public List<String> getArgs() {
        return args;
    }

    public NutsLauncherOptions setArgs(List<String> args) {
        this.args = args;
        return this;
    }

    public List<String> getNutsOptions() {
        return nutsOptions;
    }

    public NutsLauncherOptions setNutsOptions(List<String> nutsOptions) {
        this.nutsOptions = nutsOptions;
        return this;
    }

    public String getAlias() {
        return alias;
    }

    public NutsLauncherOptions setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    public boolean isCreateAlias() {
        return createAlias;
    }

    public NutsLauncherOptions setCreateAlias(boolean createAlias) {
        this.createAlias = createAlias;
        return this;
    }

    public boolean isInstallExtensions() {
        return installExtensions;
    }

    public NutsLauncherOptions setInstallExtensions(boolean installExtensions) {
        this.installExtensions = installExtensions;
        return this;
    }

    public String getCustomScriptPath() {
        return customScriptPath;
    }

    public NutsLauncherOptions setCustomScriptPath(String customScriptPath) {
        this.customScriptPath = customScriptPath;
        return this;
    }

    public Boolean getSystemWideConfig() {
        return systemWideConfig;
    }

    public NutsLauncherOptions setSystemWideConfig(Boolean systemWideConfig) {
        this.systemWideConfig = systemWideConfig;
        return this;
    }

    public String getSwitchWorkspaceLocation() {
        return switchWorkspaceLocation;
    }

    public NutsLauncherOptions setSwitchWorkspaceLocation(String switchWorkspaceLocation) {
        this.switchWorkspaceLocation = switchWorkspaceLocation;
        return this;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public NutsLauncherOptions setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
        return this;
    }

    public NutsLauncherOptions copy() {
        try {
            NutsLauncherOptions c = (NutsLauncherOptions) super.clone();
            if (c.args != null) {
                c.args = new ArrayList<>(c.args);
            }
            if (c.nutsOptions != null) {
                c.nutsOptions = new ArrayList<>(c.nutsOptions);
            }
            return c;
        } catch (CloneNotSupportedException e) {
            throw new UnsupportedOperationException(e);
        }
    }
}
