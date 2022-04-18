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
 * <p>
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
package net.thevpc.nuts.runtime.standalone.boot;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.session.NutsSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NutsWorkspaceUtils;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNutsWorkspaceConfigManager;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNutsWorkspaceConfigModel;

import java.net.URL;
import java.util.Collections;
import java.util.List;

/**
 * @author thevpc
 */
public class DefaultNutsBootManager implements NutsBootManager {

    private DefaultNutsBootModel model;
    private NutsSession session;

    public DefaultNutsBootManager(DefaultNutsBootModel model) {
        this.model = model;
    }

    public NutsBootModel getModel() {
        return model;
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    @Override
    public NutsBootManager setSession(NutsSession session) {
        this.session = NutsWorkspaceUtils.bindSession(model.getWorkspace(), session);
        return this;
    }

    @Override
    public boolean isFirstBoot() {
        return model.isFirstBoot();
    }

    @Override
    public Boolean getBootCustomBoolArgument(Boolean undefinedValue, Boolean emptyValue, Boolean errValue, String... names) {
        checkSession();
        for (String name : names) {
            NutsArgument q = model.getCustomBootOptions().get(name);
            if (q != null) {
                return q.getBooleanValue(emptyValue, errValue);
            }
        }
        return undefinedValue;
    }

    @Override
    public NutsArgument getBootCustomArgument(String... names) {
        checkSession();
        for (String name : names) {
            NutsArgument q = model.getCustomBootOptions().get(name);
            if (q != null) {
                return q;
            }
        }
        return null;
    }

    @Override
    public NutsWorkspaceOptions getBootOptions() {
        checkSession();
        return _configModel().getOptions(getSession());
    }

    @Override
    public ClassLoader getBootClassLoader() {
        checkSession();
        return _configModel().getBootClassLoader();
    }

    @Override
    public List<URL> getBootClassWorldURLs() {
        checkSession();
        return Collections.unmodifiableList(_configModel().getBootClassWorldURLs());
    }

    @Override
    public String getBootRepositories() {
        checkSession();
        return _configModel().getBootRepositories();
    }

    @Override
    public long getCreationStartTimeMillis() {
        checkSession();
        return _configModel().getCreationStartTimeMillis();
    }

    @Override
    public long getCreationFinishTimeMillis() {
        checkSession();
        return _configModel().getCreationFinishTimeMillis();
    }

    @Override
    public long getCreationTimeMillis() {
        checkSession();
        return _configModel().getCreationTimeMillis();
    }

    public NutsClassLoaderNode getBootRuntimeClassLoaderNode() {
        return model.bOptions.getRuntimeBootDependencyNode();
    }

//    public ee(){
//        NutsWorkspaceModel wsModel = ((NutsWorkspaceExt) session.getWorkspace()).getModel();
//        wsModel.bootModel.getWorkspaceInitInformation().getOptions();
//
//    }

    public List<NutsClassLoaderNode> getBootExtensionClassLoaderNode() {
        return model.bOptions.getExtensionBootDependencyNodes();
    }

    @Override
    public NutsBootTerminal getBootTerminal() {
        return model.getBootTerminal();
    }

    private DefaultNutsWorkspaceConfigModel _configModel() {
        DefaultNutsWorkspaceConfigManager config = (DefaultNutsWorkspaceConfigManager) session.config();
        DefaultNutsWorkspaceConfigModel configModel = config.getModel();
        return configModel;
    }

    private void checkSession() {
        NutsSessionUtils.checkSession(model.getWorkspace(), session);
    }

}
