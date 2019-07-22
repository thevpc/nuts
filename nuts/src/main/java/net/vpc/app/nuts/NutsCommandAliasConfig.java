/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.io.Serializable;

/**
 * Command Alias definition class Config
 * @author vpc
 * @since 0.5.4
 */
public class NutsCommandAliasConfig implements Serializable{
    private static final long serialVersionUID = 1;

    /**
     * alias definition
     */
    private NutsId owner;

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
    private String[] command;

    /**
     * alias command execution options
     */
    private String[] executorOptions;

    /**
     * alias help command (command to display help)
     */
    private String[] helpCommand;

    /**
     * alias help text (meaningful if helpCommand is not defined)
     */
    private String helpText;

    /**
     * alias definition
     * @return alias definition
     */
    public NutsId getOwner() {
        return owner;
    }

    /**
     * alias definition
     * @param value new value
     * @return {@code this} instance
     */
    public NutsCommandAliasConfig setOwner(NutsId value) {
        this.owner = value;
        return this;
    }

    /**
     * alias factory id
     * @return alias factory id
     */
    public String getFactoryId() {
        return factoryId;
    }

    /**
     * alias factory id
     * @param value new value
     * @return {@code this} instance
     */
    public NutsCommandAliasConfig setFactoryId(String value) {
        this.factoryId = value;
        return this;
    }

    /**
     * alias command arguments
     * @return alias command arguments
     */
    public String[] getCommand() {
        return command;
    }

    /**
     * alias command arguments
     * @param value new value
     * @return {@code this} instance
     */
    public NutsCommandAliasConfig setCommand(String... value) {
        this.command = value;
        return this;
    }

    /**
     * alias name
     * @return alias name
     */
    public String getName() {
        return name;
    }

    /**
     * alias name
     * @param value new value
     * @return {@code this} instance
     */
    public NutsCommandAliasConfig setName(String value) {
        this.name = value;
        return this;
    }

    /**
     * alias command execution options
     * @return alias command execution options
     */
    public String[] getExecutorOptions() {
        return executorOptions;
    }

    /**
     * alias command execution options
     * @param value new value
     * @return {@code this} instance
     */
    public NutsCommandAliasConfig setExecutorOptions(String[] value) {
        this.executorOptions = value;
        return this;
    }

    /**
     * alias help command (command to display help)
     * @return alias help command (command to display help)
     */
    public String[] getHelpCommand() {
        return helpCommand;
    }

    /**
     * alias help command (command to display help)
     * @param value new value
     * @return {@code this} instance
     */
    public NutsCommandAliasConfig setHelpCommand(String... value) {
        this.helpCommand = value;
        return this;
    }

    /**
     * alias help text (meaningful if helpCommand is not defined)
     * @return alias help text (meaningful if helpCommand is not defined)
     */
    public String getHelpText() {
        return helpText;
    }

    /**
     * alias help text (meaningful if helpCommand is not defined)
     * @param value new value
     * @return {@code this} instance
     */
    public NutsCommandAliasConfig setHelpText(String value) {
        this.helpText = value;
        return this;
    }

}
