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
package net.thevpc.nuts.command;

import net.thevpc.nuts.core.NConfigItem;
import net.thevpc.nuts.artifact.NId;
import net.thevpc.nuts.internal.NReservedLangUtils;
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NSetter;

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
    @NGetter
    public NId owner() {
        return owner;
    }

    /**
     * alias definition
     *
     * @param value new value
     * @return {@code this} instance
     */
    @NSetter
    public NCommandConfig owner(NId value) {
        this.owner = value;
        return this;
    }

    /**
     * alias factory id
     *
     * @return alias factory id
     */
    @NGetter
    public String factoryId() {
        return factoryId;
    }

    /**
     * alias factory id
     *
     * @param value new value
     * @return {@code this} instance
     */
    @NSetter
    public NCommandConfig factoryId(String value) {
        this.factoryId = value;
        return this;
    }

    /**
     * alias command arguments
     *
     * @return alias command arguments
     */
    @NGetter
    public List<String> command() {
        return command;
    }

    /**
     * alias command arguments
     *
     * @param value new value
     * @return {@code this} instance
     */
    public NCommandConfig command(String... value) {
        this.command = NReservedLangUtils.nonNullList(Arrays.asList(value));
        return this;
    }

    @NSetter
    public NCommandConfig command(List<String> value) {
        this.command = NReservedLangUtils.nonNullList(value);
        return this;
    }

    /**
     * alias name
     *
     * @return alias name
     */
    @NGetter
    public String name() {
        return name;
    }

    /**
     * alias name
     *
     * @param value new value
     * @return {@code this} instance
     */
    @NSetter
    public NCommandConfig name(String value) {
        this.name = value;
        return this;
    }

    /**
     * alias command execution options
     *
     * @return alias command execution options
     */
    @NGetter
    public List<String> executorOptions() {
        return executorOptions;
    }

    /**
     * alias command execution options
     *
     * @param value new value
     * @return {@code this} instance
     */
    @NSetter
    public NCommandConfig executorOptions(List<String> value) {
        this.executorOptions = value;
        return this;
    }

    /**
     * alias help command (command to display help)
     *
     * @return alias help command (command to display help)
     */
    @NGetter
    public List<String> helpCommand() {
        return helpCommand;
    }

    /**
     * alias help command (command to display help)
     *
     * @param value new value
     * @return {@code this} instance
     */
    public NCommandConfig helpCommand(String... value) {
        this.helpCommand = NReservedLangUtils.nonNullList(Arrays.asList(value));
        return this;
    }

    @NSetter
    public NCommandConfig helpCommand(List<String> value) {
        this.helpCommand = NReservedLangUtils.nonNullList(value);
        return this;
    }

    /**
     * alias help text (meaningful if helpCommand is not defined)
     *
     * @return alias help text (meaningful if helpCommand is not defined)
     */
    @NGetter
    public String helpText() {
        return helpText;
    }

    /**
     * alias help text (meaningful if helpCommand is not defined)
     *
     * @param value new value
     * @return {@code this} instance
     */
    @NSetter
    public NCommandConfig helpText(String value) {
        this.helpText = value;
        return this;
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(owner, name, factoryId, helpText, command, executorOptions, helpCommand);
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
