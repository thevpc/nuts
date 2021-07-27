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
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.model.CoreNutsWorkspaceOptions;
import net.thevpc.nuts.spi.NutsIndexStoreFactory;
import java.net.URL;
import java.util.*;
import net.thevpc.nuts.runtime.core.config.NutsWorkspaceConfigManagerExt;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

/**
 * @author thevpc
 */
public class DefaultNutsWorkspaceConfigManager implements NutsWorkspaceConfigManager, NutsWorkspaceConfigManagerExt{

    private DefaultNutsWorkspaceConfigModel model;
    private NutsSession session;

    public DefaultNutsWorkspaceConfigManager(DefaultNutsWorkspaceConfigModel model) {
        this.model = model;
    }

    public DefaultNutsWorkspaceConfigModel getModel() {
        return model;
    }

    public NutsSession getSession() {
        return session;
    }

    public NutsWorkspaceConfigManager setSession(NutsSession session) {
        this.session = session;
        return this;
    }

    @Override
    public NutsWorkspaceStoredConfig stored() {
        checkSession();
        return model.stored();
    }

    @Override
    public boolean isReadOnly() {
        checkSession();
        return model.isReadOnly();
    }

    protected void checkSession() {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
    }

    @Override
    public boolean save(boolean force) {
        checkSession();
        return model.save(force, session);
    }

    @Override
    public boolean save() {
        checkSession();
        return model.save(session);
    }

    @Override
    public NutsWorkspaceBootConfig loadBootConfig(String _ws, boolean global, boolean followLinks) {
        checkSession();
        return model.loadBootConfig(_ws, global, followLinks, session);
    }

    @Override
    public NutsWorkspaceOptionsBuilder optionsBuilder() {
        checkSession();
        return new CoreNutsWorkspaceOptions(session);
    }

    @Override
    public boolean isExcludedExtension(String extensionId, NutsWorkspaceOptions options) {
        checkSession();
        return model.isExcludedExtension(extensionId, options, session);
    }


    @Override
    public NutsId createContentFaceId(NutsId id, NutsDescriptor desc) {
        checkSession();
        return model.createContentFaceId(id, desc);
    }

    @Override
    public NutsWorkspaceListManager createWorkspaceListManager(String name) {
        checkSession();
        return model.createWorkspaceListManager(name, session);
    }

    @Override
    public boolean isSupportedRepositoryType(String repositoryType) {
        checkSession();
        return model.isSupportedRepositoryType(repositoryType, session);
    }

    @Override
    public NutsAddRepositoryOptions[] getDefaultRepositories() {
        checkSession();
        return model.getDefaultRepositories(session);
    }

    @Override
    public Set<String> getAvailableArchetypes() {
        checkSession();
        return model.getAvailableArchetypes(session);
    }

    @Override
    public String resolveRepositoryPath(String repositoryLocation) {
        checkSession();
        return model.resolveRepositoryPath(repositoryLocation, session);
    }

    @Override
    public NutsIndexStoreFactory getIndexStoreClientFactory() {
        checkSession();
        return model.getIndexStoreClientFactory();
    }


    @Override
    public String getJavaCommand() {
        checkSession();
        return model.getJavaCommand();
    }

    @Override
    public String getJavaOptions() {
        checkSession();
        return model.getJavaOptions();
    }

    @Override
    public boolean isGlobal() {
        checkSession();
        return model.isGlobal();
    }

    @Override
    public String toString() {
        String s1 = "NULL";
        String s2 = "NULL";
        s1 = String.valueOf(model.getWorkspace().getApiId());
        s2 = String.valueOf(model.getWorkspace().getRuntimeId());
        return "NutsWorkspaceConfig{"
                + "workspaceBootId=" + s1
                + ", workspaceRuntimeId=" + s2
                + ", workspace=" + ((model.getCurrentConfig() == null) ? "NULL" : ("'" + model.getWorkspace().locations().getWorkspaceLocation() + '\''))
                + '}';
    }

}
