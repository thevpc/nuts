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
    private NSession session;

    public DefaultNConfigs(NSession session) {
        this.session = session;
        NWorkspace w = this.session.getWorkspace();
        NWorkspaceExt e = (NWorkspaceExt) w;
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
        checkSession();
        return cmodel.stored();
    }

    @Override
    public boolean isReadOnly() {
        checkSession();
        return cmodel.isReadOnly();
    }

    @Override
    public boolean save(boolean force) {
        checkSession();
        return cmodel.save(force, session);
    }

    @Override
    public boolean save() {
        checkSession();
        return cmodel.save(session);
    }

    @Override
    public NWorkspaceBootConfig loadBootConfig(String _ws, boolean global, boolean followLinks) {
        checkSession();
        return cmodel.loadBootConfig(_ws, global, followLinks, session);
    }

    @Override
    public boolean isSupportedRepositoryType(String repositoryType) {
        checkSession();
        return cmodel.isSupportedRepositoryType(repositoryType, session);
    }

    @Override
    public List<NAddRepositoryOptions> getDefaultRepositories() {
        checkSession();
        return cmodel.getDefaultRepositories(session);
    }

    @Override
    public Set<String> getAvailableArchetypes() {
        checkSession();
        return cmodel.getAvailableArchetypes(session);
    }

    @Override
    public NPath resolveRepositoryPath(String repositoryLocation) {
        checkSession();
        return cmodel.resolveRepositoryPath(NPath.of(repositoryLocation, session), session);
    }

    @Override
    public NIndexStoreFactory getIndexStoreClientFactory() {
        checkSession();
        return cmodel.getIndexStoreClientFactory();
    }

    @Override
    public String getJavaCommand() {
        checkSession();
        return cmodel.getJavaCommand();
    }

    @Override
    public String getJavaOptions() {
        checkSession();
        return cmodel.getJavaOptions();
    }

    @Override
    public boolean isSystemWorkspace() {
        checkSession();
        return cmodel.isSystem();
    }

    public NSession getSession() {
        return session;
    }

    public NConfigs setSession(NSession session) {
        this.session = NWorkspaceUtils.bindSession(cmodel.getWorkspace(), session);
        return this;
    }

    protected void checkSession() {
        NSessionUtils.checkSession(cmodel.getWorkspace(), session);
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
                NLocations.of(NSessionUtils.defaultSession(cmodel.getWorkspace()))
                        .getWorkspaceLocation() + '\''))
                + '}';
    }

    public List<String> getDependencySolverNames() {
        checkSession();
        // the first element is always the default one,
        // the rest is lexicographically sorter
        return Arrays.stream(cmodel.getDependencySolvers(getSession()))
                .map(NDependencySolverFactory::getName)
                .sorted(new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        if (!o1.equals(o2)) {
                            String n = NDependencySolverUtils.resolveSolverName(session.getDependencySolver());
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
        checkSession();
        return cmodel.createDependencySolver(name, getSession());
    }



    @Override
    public Map<String, String> getConfigMap() {
        checkSession();
        return cmodel.getConfigMap();
    }

    @Override
    public NOptional<NLiteral> getConfigProperty(String property) {
        checkSession();
        return cmodel.getConfigProperty(property, getSession());
    }

    @Override
    public NConfigs setConfigProperty(String property, String value) {
        checkSession();
        cmodel.setConfigProperty(property, value, session);
        cmodel.save(getSession());
        return this;
    }

}
