/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * <br>
 * <p>
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may
 * not use this file except in compliance with the License. You may obtain a
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts;

import net.thevpc.nuts.util.NSupportMode;

import java.util.ArrayList;
import java.util.List;

public class NLauncherOptions implements Cloneable {
    private boolean createAlias;
    private boolean createScript;
    private NSupportMode createMenuLauncher;
    private NSupportMode createDesktopLauncher;
    private NSupportMode createUserLauncher;
    private boolean installExtensions;
    private String alias;
    private String shortcutName;
    private String customShortcutPath;
    private String customScriptPath;
    private String icon;
    private String menuCategory;
    private boolean openTerminal;

    private Boolean switchWorkspace;
    private NId id;
    private List<String> args = new ArrayList<>();

    private List<String> nutsOptions = new ArrayList<>();

    private String switchWorkspaceLocation;
    private String workingDirectory;

    public boolean isCreateScript() {
        return createScript;
    }

    public NLauncherOptions setCreateScript(boolean createScript) {
        this.createScript = createScript;
        return this;
    }

    public NSupportMode getCreateMenuLauncher() {
        return createMenuLauncher;
    }

    public NLauncherOptions setCreateMenuLauncher(NSupportMode createMenuShortcut) {
        this.createMenuLauncher = createMenuShortcut;
        return this;
    }

    public NSupportMode getCreateDesktopLauncher() {
        return createDesktopLauncher;
    }

    public NLauncherOptions setCreateDesktopLauncher(NSupportMode createDesktopLauncher) {
        this.createDesktopLauncher = createDesktopLauncher;
        return this;
    }

    public NSupportMode getCreateUserLauncher() {
        return createUserLauncher;
    }

    public NLauncherOptions setCreateUserLauncher(NSupportMode createUserLauncher) {
        this.createUserLauncher = createUserLauncher;
        return this;
    }

    public String getShortcutName() {
        return shortcutName;
    }

    public NLauncherOptions setShortcutName(String shortcutName) {
        this.shortcutName = shortcutName;
        return this;
    }

    public String getCustomShortcutPath() {
        return customShortcutPath;
    }

    public NLauncherOptions setCustomShortcutPath(String customShortcutPath) {
        this.customShortcutPath = customShortcutPath;
        return this;
    }

    public String getIcon() {
        return icon;
    }

    public NLauncherOptions setIcon(String icon) {
        this.icon = icon;
        return this;
    }

    public String getMenuCategory() {
        return menuCategory;
    }

    public NLauncherOptions setMenuCategory(String menuCategory) {
        this.menuCategory = menuCategory;
        return this;
    }

    public boolean isOpenTerminal() {
        return openTerminal;
    }

    public NLauncherOptions setOpenTerminal(boolean openTerminal) {
        this.openTerminal = openTerminal;
        return this;
    }

    public NId getId() {
        return id;
    }

    public NLauncherOptions setId(NId id) {
        this.id = id;
        return this;
    }

    public List<String> getArgs() {
        return args;
    }

    public NLauncherOptions setArgs(List<String> args) {
        this.args = args;
        return this;
    }

    public List<String> getNutsOptions() {
        return nutsOptions;
    }

    public NLauncherOptions setNutsOptions(List<String> nutsOptions) {
        this.nutsOptions = nutsOptions;
        return this;
    }

    public String getAlias() {
        return alias;
    }

    public NLauncherOptions setAlias(String alias) {
        this.alias = alias;
        return this;
    }

    public boolean isCreateAlias() {
        return createAlias;
    }

    public NLauncherOptions setCreateAlias(boolean createAlias) {
        this.createAlias = createAlias;
        return this;
    }

    public boolean isInstallExtensions() {
        return installExtensions;
    }

    public NLauncherOptions setInstallExtensions(boolean installExtensions) {
        this.installExtensions = installExtensions;
        return this;
    }

    public String getCustomScriptPath() {
        return customScriptPath;
    }

    public NLauncherOptions setCustomScriptPath(String customScriptPath) {
        this.customScriptPath = customScriptPath;
        return this;
    }

    public Boolean getSwitchWorkspace() {
        return switchWorkspace;
    }

    public NLauncherOptions setSwitchWorkspace(Boolean switchWorkspace) {
        this.switchWorkspace = switchWorkspace;
        return this;
    }

    public String getSwitchWorkspaceLocation() {
        return switchWorkspaceLocation;
    }

    public NLauncherOptions setSwitchWorkspaceLocation(String switchWorkspaceLocation) {
        this.switchWorkspaceLocation = switchWorkspaceLocation;
        return this;
    }

    public String getWorkingDirectory() {
        return workingDirectory;
    }

    public NLauncherOptions setWorkingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
        return this;
    }

    public NLauncherOptions copy() {
        try {
            NLauncherOptions c = (NLauncherOptions) super.clone();
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
