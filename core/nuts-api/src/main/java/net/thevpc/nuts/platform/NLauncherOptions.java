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
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.platform;

import net.thevpc.nuts.artifact.NId;
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

    public boolean createScript() {
        return createScript;
    }

    public NLauncherOptions createScript(boolean createScript) {
        this.createScript = createScript;
        return this;
    }

    public NSupportMode createMenuLauncher() {
        return createMenuLauncher;
    }

    public NLauncherOptions createMenuLauncher(NSupportMode createMenuShortcut) {
        this.createMenuLauncher = createMenuShortcut;
        return this;
    }

    public NSupportMode createDesktopLauncher() {
        return createDesktopLauncher;
    }

    public NLauncherOptions createDesktopLauncher(NSupportMode createDesktopLauncher) {
        this.createDesktopLauncher = createDesktopLauncher;
        return this;
    }

    public NSupportMode createUserLauncher() {
        return createUserLauncher;
    }

    public NLauncherOptions createUserLauncher(NSupportMode createUserLauncher) {
        this.createUserLauncher = createUserLauncher;
        return this;
    }

    public String shortcutName() {
        return shortcutName;
    }

    public NLauncherOptions shortcutName(String shortcutName) {
        this.shortcutName = shortcutName;
        return this;
    }

    public String customShortcutPath() {
        return customShortcutPath;
    }

    public NLauncherOptions customShortcutPath(String customShortcutPath) {
        this.customShortcutPath = customShortcutPath;
        return this;
    }

    public String icon() {
        return icon;
    }

    public NLauncherOptions icon(String icon) {
        this.icon = icon;
        return this;
    }

    public String menuCategory() {
        return menuCategory;
    }

    public NLauncherOptions menuCategory(String menuCategory) {
        this.menuCategory = menuCategory;
        return this;
    }

    public boolean isOpenTerminal() {
        return openTerminal;
    }

    public NLauncherOptions openTerminal(boolean openTerminal) {
        this.openTerminal = openTerminal;
        return this;
    }

    public NId id() {
        return id;
    }

    public NLauncherOptions id(NId id) {
        this.id = id;
        return this;
    }

    public List<String> args() {
        return args;
    }

    public NLauncherOptions args(List<String> args) {
        this.args = args;
        return this;
    }

    public List<String> nutsOptions() {
        return nutsOptions;
    }

    public NLauncherOptions nutsOptions(List<String> nutsOptions) {
        this.nutsOptions = nutsOptions;
        return this;
    }

    public String alias() {
        return alias;
    }

    public NLauncherOptions alias(String alias) {
        this.alias = alias;
        return this;
    }

    public boolean isCreateAlias() {
        return createAlias;
    }

    public NLauncherOptions createAlias(boolean createAlias) {
        this.createAlias = createAlias;
        return this;
    }

    public boolean isInstallExtensions() {
        return installExtensions;
    }

    public NLauncherOptions installExtensions(boolean installExtensions) {
        this.installExtensions = installExtensions;
        return this;
    }

    public String customScriptPath() {
        return customScriptPath;
    }

    public NLauncherOptions customScriptPath(String customScriptPath) {
        this.customScriptPath = customScriptPath;
        return this;
    }

    public Boolean switchWorkspace() {
        return switchWorkspace;
    }

    public NLauncherOptions switchWorkspace(Boolean switchWorkspace) {
        this.switchWorkspace = switchWorkspace;
        return this;
    }

    public String switchWorkspaceLocation() {
        return switchWorkspaceLocation;
    }

    public NLauncherOptions switchWorkspaceLocation(String switchWorkspaceLocation) {
        this.switchWorkspaceLocation = switchWorkspaceLocation;
        return this;
    }

    public String workingDirectory() {
        return workingDirectory;
    }

    public NLauncherOptions workingDirectory(String workingDirectory) {
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
