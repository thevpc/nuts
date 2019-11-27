/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.main.config;

import net.vpc.app.nuts.*;

import java.io.Serializable;
import java.util.*;

/**
 *
 * @author vpc
 * @since 0.5.4
 */
public final class NutsWorkspaceConfigMain implements Serializable {

    private static final long serialVersionUID = 2;
    /**
     * Api version having created the config
     */
    private String configVersion = null;

    private List<NutsRepositoryRef> repositories;
    private List<NutsCommandAliasFactoryConfig> commandFactories;
    private Map<String,String> env = new LinkedHashMap<>();
    private List<NutsSdkLocation> sdk = new ArrayList<>();
    private List<String> imports = new ArrayList<>();

    public NutsWorkspaceConfigMain() {
    }

    public List<NutsRepositoryRef> getRepositories() {
        return repositories;
    }

    public NutsWorkspaceConfigMain setRepositories(List<NutsRepositoryRef> repositories) {
        this.repositories = repositories;
        return this;
    }

    public List<String> getImports() {
        return imports;
    }

    public NutsWorkspaceConfigMain setImports(List<String> imports) {
        this.imports = imports;
        return this;
    }

    public NutsWorkspaceConfigMain setCommandFactories(List<NutsCommandAliasFactoryConfig> commandFactories) {
        this.commandFactories = commandFactories;
        return this;
    }

    public NutsWorkspaceConfigMain setSdk(List<NutsSdkLocation> sdk) {
        this.sdk = sdk;
        return this;
    }

    public Map<String,String> getEnv() {
        return env;
    }

    public NutsWorkspaceConfigMain setEnv(Map<String,String> env) {
        this.env = env;
        return this;
    }

    public List<NutsSdkLocation> getSdk() {
        return sdk;
    }

    public String getConfigVersion() {
        return configVersion;
    }

    public NutsWorkspaceConfigMain setConfigVersion(String configVersion) {
        this.configVersion = configVersion;
        return this;
    }

    public List<NutsCommandAliasFactoryConfig> getCommandFactories() {
        return commandFactories;
    }

}
