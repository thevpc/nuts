/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
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
package net.thevpc.nuts.runtime.standalone.workspace.config.compat.v507;

import net.thevpc.nuts.command.NCommandFactoryConfig;
import net.thevpc.nuts.core.NConfigItem;
import net.thevpc.nuts.platform.NPlatformLocation;
import net.thevpc.nuts.core.NRepositoryRef;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author thevpc
 * @since 0.5.4
 */
public final class NWorkspaceConfigMain507 extends NConfigItem {

    private static final long serialVersionUID = 4;

    private List<NRepositoryRef> repositories;
    private List<NCommandFactoryConfig> commandFactories;
    private Map<String,String> env = new LinkedHashMap<>();
    private List<NPlatformLocation> sdk = new ArrayList<>();
    private List<String> imports = new ArrayList<>();

    public NWorkspaceConfigMain507() {
    }

    public List<NRepositoryRef> getRepositories() {
        return repositories;
    }

    public NWorkspaceConfigMain507 setRepositories(List<NRepositoryRef> repositories) {
        this.repositories = repositories;
        return this;
    }

    public List<String> getImports() {
        return imports;
    }

    public NWorkspaceConfigMain507 setImports(List<String> imports) {
        this.imports = imports;
        return this;
    }

    public NWorkspaceConfigMain507 setCommandFactories(List<NCommandFactoryConfig> commandFactories) {
        this.commandFactories = commandFactories;
        return this;
    }

    public NWorkspaceConfigMain507 setSdk(List<NPlatformLocation> sdk) {
        this.sdk = sdk;
        return this;
    }

    public Map<String,String> getEnv() {
        return env;
    }

    public NWorkspaceConfigMain507 setEnv(Map<String,String> env) {
        this.env = env;
        return this;
    }

    public List<NPlatformLocation> getSdk() {
        return sdk;
    }

    public List<NCommandFactoryConfig> getCommandFactories() {
        return commandFactories;
    }

}
