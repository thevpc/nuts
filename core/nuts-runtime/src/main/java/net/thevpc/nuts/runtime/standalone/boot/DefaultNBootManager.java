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
 * Copyright [2020] [thevpc]  
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License"); 
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts.runtime.standalone.boot;

import net.thevpc.nuts.*;
import net.thevpc.nuts.boot.NBootClassLoaderNode;
import net.thevpc.nuts.env.NBootManager;
import net.thevpc.nuts.env.NBootOptions;
import net.thevpc.nuts.NConstants;
import net.thevpc.nuts.NWorkspaceTerminalOptions;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNConfigs;
import net.thevpc.nuts.runtime.standalone.workspace.config.DefaultNWorkspaceConfigModel;
import net.thevpc.nuts.spi.NSupportLevelContext;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NOptional;

import java.net.URL;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;

/**
 * @author thevpc
 */
public class DefaultNBootManager implements NBootManager {

    private DefaultNBootModel model;

    public DefaultNBootManager(NWorkspace ws) {
        NWorkspaceExt e = (NWorkspaceExt) ws;
        this.model = e.getModel().bootModel;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    public NBootModel getModel() {
        return model;
    }

    @Override
    public boolean isFirstBoot() {
        return model.isFirstBoot();
    }

    @Override
    public NOptional<NLiteral> getCustomBootOption(String... names) {
        return model.getCustomBootOption(names);
    }


    @Override
    public NBootOptions getBootOptions() {
        return _configModel().getBootModel().getBootEffectiveOptions();
    }

    @Override
    public ClassLoader getBootClassLoader() {
        return _configModel().getBootClassLoader();
    }

    @Override
    public List<URL> getBootClassWorldURLs() {
        return Collections.unmodifiableList(_configModel().getBootClassWorldURLs());
    }

    @Override
    public String getBootRepositories() {
        return _configModel().getBootRepositories();
    }

    @Override
    public Instant getCreationStartTime() {
        return _configModel().getCreationStartTime();
    }

    @Override
    public Instant getCreationFinishTime() {
        return _configModel().getCreationFinishTime();
    }

    @Override
    public Duration getCreationDuration() {
        return _configModel().getCreateDuration();
    }

    public NBootClassLoaderNode getBootRuntimeClassLoaderNode() {
        return model.bOptions.getRuntimeBootDependencyNode().get();
    }

    public List<NBootClassLoaderNode> getBootExtensionClassLoaderNode() {
        return model.bOptions.getExtensionBootDependencyNodes().orElseGet(Collections::emptyList);
    }

    @Override
    public NWorkspaceTerminalOptions getBootTerminal() {
        return model.getBootTerminal();
    }

    private DefaultNWorkspaceConfigModel _configModel() {
        DefaultNConfigs config = (DefaultNConfigs) NConfigs.of();
        DefaultNWorkspaceConfigModel configModel = config.getModel();
        return configModel;
    }

}
