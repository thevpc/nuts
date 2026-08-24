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

/**
 * NLauncherOptions class.
 *
 * @author thevpc
 * @since 0.8.0
 */
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

    /**
     * Creates a new instance of create script.
     *
     * @return create script result
     */
    public boolean createScript() {
        return createScript;
    }

    /**
     * Creates a new instance of create script.
     *
     * @param createScript create script
     * @return create script result
     */
    public NLauncherOptions createScript(boolean createScript) {
        this.createScript = createScript;
        return this;
    }

    /**
     * Creates a new instance of create menu launcher.
     *
     * @return create menu launcher result
     */
    public NSupportMode createMenuLauncher() {
        return createMenuLauncher;
    }

    /**
     * Creates a new instance of create menu launcher.
     *
     * @param createMenuShortcut create menu shortcut
     * @return create menu launcher result
     */
    public NLauncherOptions createMenuLauncher(NSupportMode createMenuShortcut) {
        this.createMenuLauncher = createMenuShortcut;
        return this;
    }

    /**
     * Creates a new instance of create desktop launcher.
     *
     * @return create desktop launcher result
     */
    public NSupportMode createDesktopLauncher() {
        return createDesktopLauncher;
    }

    /**
     * Creates a new instance of create desktop launcher.
     *
     * @param createDesktopLauncher create desktop launcher
     * @return create desktop launcher result
     */
    public NLauncherOptions createDesktopLauncher(NSupportMode createDesktopLauncher) {
        this.createDesktopLauncher = createDesktopLauncher;
        return this;
    }

    /**
     * Creates a new instance of create user launcher.
     *
     * @return create user launcher result
     */
    public NSupportMode createUserLauncher() {
        return createUserLauncher;
    }

    /**
     * Creates a new instance of create user launcher.
     *
     * @param createUserLauncher create user launcher
     * @return create user launcher result
     */
    public NLauncherOptions createUserLauncher(NSupportMode createUserLauncher) {
        this.createUserLauncher = createUserLauncher;
        return this;
    }

    /**
     * Shortcut name.
     *
     * @return shortcut name result
     */
    public String shortcutName() {
        return shortcutName;
    }

    /**
     * Shortcut name.
     *
     * @param shortcutName shortcut name
     * @return shortcut name result
     */
    public NLauncherOptions shortcutName(String shortcutName) {
        this.shortcutName = shortcutName;
        return this;
    }

    /**
     * Custom shortcut path.
     *
     * @return custom shortcut path result
     */
    public String customShortcutPath() {
        return customShortcutPath;
    }

    /**
     * Custom shortcut path.
     *
     * @param customShortcutPath custom shortcut path
     * @return custom shortcut path result
     */
    public NLauncherOptions customShortcutPath(String customShortcutPath) {
        this.customShortcutPath = customShortcutPath;
        return this;
    }

    /**
     * Icon.
     *
     * @return icon result
     */
    public String icon() {
        return icon;
    }

    /**
     * Icon.
     *
     * @param icon icon
     * @return icon result
     */
    public NLauncherOptions icon(String icon) {
        this.icon = icon;
        return this;
    }

    /**
     * Menu category.
     *
     * @return menu category result
     */
    public String menuCategory() {
        return menuCategory;
    }

    /**
     * Menu category.
     *
     * @param menuCategory menu category
     * @return menu category result
     */
    public NLauncherOptions menuCategory(String menuCategory) {
        this.menuCategory = menuCategory;
        return this;
    }

    /**
     * Checks if is open terminal.
     *
     * @return is open terminal result
     */
    public boolean isOpenTerminal() {
        return openTerminal;
    }

    /**
     * Open terminal.
     *
     * @param openTerminal open terminal
     * @return open terminal result
     */
    public NLauncherOptions openTerminal(boolean openTerminal) {
        this.openTerminal = openTerminal;
        return this;
    }

    /**
     * Id.
     *
     * @return id result
     */
    public NId id() {
        return id;
    }

    /**
     * Id.
     *
     * @param id id
     * @return id result
     */
    public NLauncherOptions id(NId id) {
        this.id = id;
        return this;
    }

    /**
     * Args.
     *
     * @return args result
     */
    public List<String> args() {
        return args;
    }

    /**
     * Args.
     *
     * @param args args
     * @return args result
     */
    public NLauncherOptions args(List<String> args) {
        this.args = args;
        return this;
    }

    /**
     * Nuts options.
     *
     * @return nuts options result
     */
    public List<String> nutsOptions() {
        return nutsOptions;
    }

    /**
     * Nuts options.
     *
     * @param nutsOptions nuts options
     * @return nuts options result
     */
    public NLauncherOptions nutsOptions(List<String> nutsOptions) {
        this.nutsOptions = nutsOptions;
        return this;
    }

    /**
     * Alias.
     *
     * @return alias result
     */
    public String alias() {
        return alias;
    }

    /**
     * Alias.
     *
     * @param alias alias
     * @return alias result
     */
    public NLauncherOptions alias(String alias) {
        this.alias = alias;
        return this;
    }

    /**
     * Checks if is create alias.
     *
     * @return is create alias result
     */
    public boolean isCreateAlias() {
        return createAlias;
    }

    /**
     * Creates a new instance of create alias.
     *
     * @param createAlias create alias
     * @return create alias result
     */
    public NLauncherOptions createAlias(boolean createAlias) {
        this.createAlias = createAlias;
        return this;
    }

    /**
     * Checks if is install extensions.
     *
     * @return is install extensions result
     */
    public boolean isInstallExtensions() {
        return installExtensions;
    }

    /**
     * Install extensions.
     *
     * @param installExtensions install extensions
     * @return install extensions result
     */
    public NLauncherOptions installExtensions(boolean installExtensions) {
        this.installExtensions = installExtensions;
        return this;
    }

    /**
     * Custom script path.
     *
     * @return custom script path result
     */
    public String customScriptPath() {
        return customScriptPath;
    }

    /**
     * Custom script path.
     *
     * @param customScriptPath custom script path
     * @return custom script path result
     */
    public NLauncherOptions customScriptPath(String customScriptPath) {
        this.customScriptPath = customScriptPath;
        return this;
    }

    /**
     * Switch workspace.
     *
     * @return switch workspace result
     */
    public Boolean switchWorkspace() {
        return switchWorkspace;
    }

    /**
     * Switch workspace.
     *
     * @param switchWorkspace switch workspace
     * @return switch workspace result
     */
    public NLauncherOptions switchWorkspace(Boolean switchWorkspace) {
        this.switchWorkspace = switchWorkspace;
        return this;
    }

    /**
     * Switch workspace location.
     *
     * @return switch workspace location result
     */
    public String switchWorkspaceLocation() {
        return switchWorkspaceLocation;
    }

    /**
     * Switch workspace location.
     *
     * @param switchWorkspaceLocation switch workspace location
     * @return switch workspace location result
     */
    public NLauncherOptions switchWorkspaceLocation(String switchWorkspaceLocation) {
        this.switchWorkspaceLocation = switchWorkspaceLocation;
        return this;
    }

    /**
     * Working directory.
     *
     * @return working directory result
     */
    public String workingDirectory() {
        return workingDirectory;
    }

    /**
     * Working directory.
     *
     * @param workingDirectory working directory
     * @return working directory result
     */
    public NLauncherOptions workingDirectory(String workingDirectory) {
        this.workingDirectory = workingDirectory;
        return this;
    }

    /**
     * Copy.
     *
     * @return copy result
     */
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
            /**
             * Unsupported operation exception.
             *
             * @param e e
             * @return unsupported operation exception result
             */
            throw new UnsupportedOperationException(e);
        }
    }
}
