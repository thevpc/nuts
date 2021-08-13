package net.thevpc.nuts;

public class NutsLauncherOptions implements Cloneable {
    private boolean createAlias;
    private boolean createScript;
    private boolean createMenuShortcut;
    private boolean createDesktopShortcut;
    private boolean createCustomShortcut;
    private boolean installExtensions;
    private String alias;
    private String shortcutName;
    private String customShortcutPath;
    private String icon;
    private String menuCategory;
    private boolean openTerminal;
    private NutsId id;
    private String[] args;

    public boolean isCreateScript() {
        return createScript;
    }

    public NutsLauncherOptions setCreateScript(boolean createScript) {
        this.createScript = createScript;
        return this;
    }

    public boolean isCreateMenuShortcut() {
        return createMenuShortcut;
    }

    public NutsLauncherOptions setCreateMenuShortcut(boolean createMenuShortcut) {
        this.createMenuShortcut = createMenuShortcut;
        return this;
    }

    public boolean isCreateDesktopShortcut() {
        return createDesktopShortcut;
    }

    public NutsLauncherOptions setCreateDesktopShortcut(boolean createDesktopShortcut) {
        this.createDesktopShortcut = createDesktopShortcut;
        return this;
    }

    public boolean isCreateCustomShortcut() {
        return createCustomShortcut;
    }

    public NutsLauncherOptions setCreateCustomShortcut(boolean createCustomShortcut) {
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

    public String[] getArgs() {
        return args;
    }

    public NutsLauncherOptions setArgs(String[] args) {
        this.args = args;
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

    @Override
    protected NutsLauncherOptions clone() {
        try {
            return (NutsLauncherOptions) super.clone();
        } catch (CloneNotSupportedException e) {
            throw new UnsupportedOperationException(e);
        }
    }
}
