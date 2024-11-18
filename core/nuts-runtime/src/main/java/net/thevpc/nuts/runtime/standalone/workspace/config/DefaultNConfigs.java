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
package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.dependency.solver.NDependencySolverUtils;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.*;
import net.thevpc.nuts.util.NLiteral;
import net.thevpc.nuts.util.NOptional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
@NComponentScope(NScopeType.SESSION)
public class DefaultNConfigs implements NConfigs, NConfigsExt {

    private final DefaultNWorkspaceConfigModel cmodel;

    public DefaultNConfigs(NWorkspace ws) {
        NWorkspaceExt e = NWorkspaceExt.of(ws);
        this.cmodel = e.getModel().configModel;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return NConstants.Support.DEFAULT_SUPPORT;
    }

    public DefaultNWorkspaceConfigModel getModel() {
        return cmodel;
    }

    @Override
    public NWorkspaceStoredConfig stored() {
        return cmodel.stored();
    }

    @Override
    public boolean isReadOnly() {
        return cmodel.isReadOnly();
    }

    @Override
    public boolean save(boolean force) {
        return cmodel.save(force);
    }

    @Override
    public boolean save() {
        return cmodel.save();
    }

    @Override
    public NWorkspaceBootConfig loadBootConfig(String _ws, boolean global, boolean followLinks) {
        return cmodel.loadBootConfig(_ws, global, followLinks);
    }

    @Override
    public boolean isSupportedRepositoryType(String repositoryType) {
        return cmodel.isSupportedRepositoryType(repositoryType);
    }

    @Override
    public List<NAddRepositoryOptions> getDefaultRepositories() {
        return cmodel.getDefaultRepositories();
    }

    @Override
    public Set<String> getAvailableArchetypes() {
        return cmodel.getAvailableArchetypes();
    }

    @Override
    public NPath resolveRepositoryPath(String repositoryLocation) {
        return cmodel.resolveRepositoryPath(NPath.of(repositoryLocation));
    }

    @Override
    public NIndexStoreFactory getIndexStoreClientFactory() {
        return cmodel.getIndexStoreClientFactory();
    }

    @Override
    public String getJavaCommand() {
        return cmodel.getJavaCommand();
    }

    @Override
    public String getJavaOptions() {
        return cmodel.getJavaOptions();
    }

    @Override
    public boolean isSystemWorkspace() {
        return cmodel.isSystem();
    }


    @Override
    public String toString() {
        String s1 = "NULL";
        String s2 = "NULL";
        s1 = String.valueOf(cmodel.getWorkspace().getApiId());
        s2 = String.valueOf(cmodel.getWorkspace().getRuntimeId());
        return "NutsWorkspaceConfig{"
                + "workspaceBootId=" + s1
                + ", workspaceRuntimeId=" + s2
                + ", workspace=" + ((cmodel.getCurrentConfig() == null) ? "NULL" : ("'" +
                NLocations.of()
                        .getWorkspaceLocation() + '\''))
                + '}';
    }

    public List<String> getDependencySolverNames() {
        // the first element is always the default one,
        // the rest is lexicographically sorter
        return Arrays.stream(cmodel.getDependencySolvers())
                .map(NDependencySolverFactory::getName)
                .sorted(new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        if (!o1.equals(o2)) {
                            String n = NDependencySolverUtils.resolveSolverName(NSession.of().get().getDependencySolver());
                            if (o1.equals(n)) {
                                return -1;
                            }
                            if (o2.equals(n)) {
                                return 1;
                            }
                        }
                        return o1.compareTo(o2);
                    }
                })
                .collect(Collectors.toList());
    }

    public NDependencySolver createDependencySolver(String name) {
        return cmodel.createDependencySolver(name);
    }



    @Override
    public Map<String, String> getConfigMap() {
        return cmodel.getConfigMap();
    }

    @Override
    public NOptional<NLiteral> getConfigProperty(String property) {
        return cmodel.getConfigProperty(property);
    }

    @Override
    public NConfigs setConfigProperty(String property, String value) {
        cmodel.setConfigProperty(property, value);
        cmodel.save();
        return this;
    }

}
