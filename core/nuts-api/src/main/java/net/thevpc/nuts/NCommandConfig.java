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
package net.thevpc.nuts;

import net.thevpc.nuts.reserved.NReservedLangUtils;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Command Alias definition class Config
 *
 * @author thevpc
 * @app.category Config
 * @since 0.5.4
 */
public class NCommandConfig extends NConfigItem {
    private static final long serialVersionUID = 1;

    /**
     * alias definition
     */
    private NId owner;

    /**
     * alias name
     */
    private String name;

    /**
     * alias factory id
     */
    private String factoryId;

    /**
     * alias command arguments
     */
    private List<String> command;

    /**
     * alias command execution options
     */
    private List<String> executorOptions;

    /**
     * alias help command (command to display help)
     */
    private List<String> helpCommand;

    /**
     * alias help text (meaningful if helpCommand is not defined)
     */
    private String helpText;

    /**
     * alias definition
     *
     * @return alias definition
     */
    public NId getOwner() {
        return owner;
    }

    /**
     * alias definition
     *
     * @param value new value
     * @return {@code this} instance
     */
    public NCommandConfig setOwner(NId value) {
        this.owner = value;
        return this;
    }

    /**
     * alias factory id
     *
     * @return alias factory id
     */
    public String getFactoryId() {
        return factoryId;
    }

    /**
     * alias factory id
     *
     * @param value new value
     * @return {@code this} instance
     */
    public NCommandConfig setFactoryId(String value) {
        this.factoryId = value;
        return this;
    }

    /**
     * alias command arguments
     *
     * @return alias command arguments
     */
    public List<String> getCommand() {
        return command;
    }

    /**
     * alias command arguments
     *
     * @param value new value
     * @return {@code this} instance
     */
    public NCommandConfig setCommand(String... value) {
        this.command = NReservedLangUtils.nonNullList(Arrays.asList(value));
        return this;
    }
    public NCommandConfig setCommand(List<String> value) {
        this.command = NReservedLangUtils.nonNullList(value);
        return this;
    }

    /**
     * alias name
     *
     * @return alias name
     */
    public String getName() {
        return name;
    }

    /**
     * alias name
     *
     * @param value new value
     * @return {@code this} instance
     */
    public NCommandConfig setName(String value) {
        this.name = value;
        return this;
    }

    /**
     * alias command execution options
     *
     * @return alias command execution options
     */
    public List<String> getExecutorOptions() {
        return executorOptions;
    }

    /**
     * alias command execution options
     *
     * @param value new value
     * @return {@code this} instance
     */
    public NCommandConfig setExecutorOptions(List<String> value) {
        this.executorOptions = value;
        return this;
    }

    /**
     * alias help command (command to display help)
     *
     * @return alias help command (command to display help)
     */
    public List<String> getHelpCommand() {
        return helpCommand;
    }

    /**
     * alias help command (command to display help)
     *
     * @param value new value
     * @return {@code this} instance
     */
    public NCommandConfig setHelpCommand(String... value) {
        this.helpCommand = NReservedLangUtils.nonNullList(Arrays.asList(value));
        return this;
    }
    public NCommandConfig setHelpCommand(List<String> value) {
        this.helpCommand = NReservedLangUtils.nonNullList(value);
        return this;
    }

    /**
     * alias help text (meaningful if helpCommand is not defined)
     *
     * @return alias help text (meaningful if helpCommand is not defined)
     */
    public String getHelpText() {
        return helpText;
    }

    /**
     * alias help text (meaningful if helpCommand is not defined)
     *
     * @param value new value
     * @return {@code this} instance
     */
    public NCommandConfig setHelpText(String value) {
        this.helpText = value;
        return this;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(owner, name, factoryId, helpText,command,executorOptions,helpCommand);
        return result;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NCommandConfig that = (NCommandConfig) o;
        return Objects.equals(owner, that.owner) &&
                Objects.equals(name, that.name) &&
                Objects.equals(factoryId, that.factoryId) &&
                Objects.equals(command, that.command) &&
                Objects.equals(executorOptions, that.executorOptions) &&
                Objects.equals(helpCommand, that.helpCommand) &&
                Objects.equals(helpText, that.helpText);
    }

    @Override
    public String toString() {
        return "NutsCommandConfig{" +
                "owner=" + owner +
                ", name='" + name + '\'' +
                ", factoryId='" + factoryId + '\'' +
                ", command=" + command +
                ", executorOptions=" + executorOptions +
                ", helpCommand=" + helpCommand +
                ", helpText='" + helpText + '\'' +
                '}';
    }
}
