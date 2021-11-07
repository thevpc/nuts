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
import net.thevpc.nuts.runtime.core.NutsWorkspaceExt;
import net.thevpc.nuts.runtime.core.NutsWorkspaceModel;
import net.thevpc.nuts.runtime.standalone.DefaultNutsVal;
import net.thevpc.nuts.runtime.standalone.config.DefaultNutsWorkspaceConfigManager;
import net.thevpc.nuts.runtime.standalone.config.DefaultNutsWorkspaceConfigModel;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;

import java.net.URL;

/**
 *
 * @author vpc
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
    public boolean isFirstBoot() {
        return model.isFirstBoot();
    }

    @Override
    public NutsSession getSession() {
        return session;
    }

    public NutsClassLoaderNode getBootRuntimeClassLoaderNode(){
        return model.workspaceInitInformation.getRuntimeBootDependencyNode();
    }
    public NutsClassLoaderNode[] getBootExtensionClassLoaderNode(){
        return model.workspaceInitInformation.getExtensionBootDependencyNodes();
    }
    @Override
    public NutsBootManager setSession(NutsSession session) {
        this.session = NutsWorkspaceUtils.bindSession(model.getWorkspace(), session);
        return this;
    }

    @Override
    public NutsVal getCustomBootOption(String name) {
        checkSession();
        NutsVal q = model.getCustomBootOptions().get(name);
        if(q!=null){
            return q;
        }
        return new DefaultNutsVal(null);
    }

    @Override
    public NutsVal getCustomBootOption(String ... names) {
        checkSession();
        for (String name : names) {
            NutsVal q = model.getCustomBootOptions().get(name);
            if(q!=null){
                return q;
            }
        }
        return new DefaultNutsVal(null);
    }

    @Override
    public NutsWorkspaceOptions getBootOptions() {
        checkSession();
        return _configModel().getOptions(getSession());
    }

    private DefaultNutsWorkspaceConfigModel _configModel() {
        DefaultNutsWorkspaceConfigManager config = (DefaultNutsWorkspaceConfigManager) session.config();
        DefaultNutsWorkspaceConfigModel configModel = config.getModel();
        return configModel;
    }

    private void checkSession() {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
    }


    @Override
    public ClassLoader getBootClassLoader() {
        checkSession();
        return _configModel().getBootClassLoader();
    }

    @Override
    public URL[] getBootClassWorldURLs() {
        checkSession();
        return _configModel().getBootClassWorldURLs();
    }

//    public ee(){
//        NutsWorkspaceModel wsModel = ((NutsWorkspaceExt) session.getWorkspace()).getModel();
//        wsModel.bootModel.getWorkspaceInitInformation().getOptions();
//
//    }

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

}
