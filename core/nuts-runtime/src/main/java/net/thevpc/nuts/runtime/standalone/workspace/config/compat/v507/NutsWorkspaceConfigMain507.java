/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
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
package net.thevpc.nuts.runtime.standalone.workspace.config.compat.v507;

import net.thevpc.nuts.NutsCommandFactoryConfig;
import net.thevpc.nuts.NutsConfigItem;
import net.thevpc.nuts.NutsPlatformLocation;
import net.thevpc.nuts.NutsRepositoryRef;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author thevpc
 * @since 0.5.4
 */
public final class NutsWorkspaceConfigMain507 extends NutsConfigItem {

    private static final long serialVersionUID = 4;

    private List<NutsRepositoryRef> repositories;
    private List<NutsCommandFactoryConfig> commandFactories;
    private Map<String,String> env = new LinkedHashMap<>();
    private List<NutsPlatformLocation> sdk = new ArrayList<>();
    private List<String> imports = new ArrayList<>();

    public NutsWorkspaceConfigMain507() {
    }

    public List<NutsRepositoryRef> getRepositories() {
        return repositories;
    }

    public NutsWorkspaceConfigMain507 setRepositories(List<NutsRepositoryRef> repositories) {
        this.repositories = repositories;
        return this;
    }

    public List<String> getImports() {
        return imports;
    }

    public NutsWorkspaceConfigMain507 setImports(List<String> imports) {
        this.imports = imports;
        return this;
    }

    public NutsWorkspaceConfigMain507 setCommandFactories(List<NutsCommandFactoryConfig> commandFactories) {
        this.commandFactories = commandFactories;
        return this;
    }

    public NutsWorkspaceConfigMain507 setSdk(List<NutsPlatformLocation> sdk) {
        this.sdk = sdk;
        return this;
    }

    public Map<String,String> getEnv() {
        return env;
    }

    public NutsWorkspaceConfigMain507 setEnv(Map<String,String> env) {
        this.env = env;
        return this;
    }

    public List<NutsPlatformLocation> getSdk() {
        return sdk;
    }

    public List<NutsCommandFactoryConfig> getCommandFactories() {
        return commandFactories;
    }

}
