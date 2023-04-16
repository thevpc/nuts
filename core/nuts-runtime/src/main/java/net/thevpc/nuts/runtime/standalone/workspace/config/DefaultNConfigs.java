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
package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.io.NSessionTerminal;
import net.thevpc.nuts.io.NSystemTerminal;
import net.thevpc.nuts.runtime.standalone.dependency.solver.NDependencySolverUtils;
import net.thevpc.nuts.runtime.standalone.session.NSessionUtils;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceExt;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.spi.*;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author thevpc
 */
public class DefaultNConfigs implements NConfigs, NConfigsExt {

    private final DefaultNWorkspaceConfigModel model;
    private NSession session;

    public DefaultNConfigs(NSession session) {
        this.session = session;
        NWorkspace w = this.session.getWorkspace();
        NWorkspaceExt e = (NWorkspaceExt) w;
        this.model = e.getModel().configModel;
    }

    @Override
    public int getSupportLevel(NSupportLevelContext context) {
        return DEFAULT_SUPPORT;
    }

    public DefaultNWorkspaceConfigModel getModel() {
        return model;
    }

    @Override
    public NWorkspaceStoredConfig stored() {
        checkSession();
        return model.stored();
    }

    @Override
    public boolean isReadOnly() {
        checkSession();
        return model.isReadOnly();
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
    public NWorkspaceBootConfig loadBootConfig(String _ws, boolean global, boolean followLinks) {
        checkSession();
        return model.loadBootConfig(_ws, global, followLinks, session);
    }

    @Override
    public boolean isExcludedExtension(String extensionId, NWorkspaceOptions options) {
        checkSession();
        return model.isExcludedExtension(extensionId, options, session);
    }

    @Override
    public boolean isSupportedRepositoryType(String repositoryType) {
        checkSession();
        return model.isSupportedRepositoryType(repositoryType, session);
    }

    @Override
    public List<NAddRepositoryOptions> getDefaultRepositories() {
        checkSession();
        return model.getDefaultRepositories(session);
    }

    @Override
    public Set<String> getAvailableArchetypes() {
        checkSession();
        return model.getAvailableArchetypes(session);
    }

    @Override
    public NPath resolveRepositoryPath(String repositoryLocation) {
        checkSession();
        return model.resolveRepositoryPath(NPath.of(repositoryLocation, session), session);
    }

    @Override
    public NIndexStoreFactory getIndexStoreClientFactory() {
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
        return model.isSystem();
    }

    public NSession getSession() {
        return session;
    }

    public NConfigs setSession(NSession session) {
        this.session = NWorkspaceUtils.bindSession(model.getWorkspace(), session);
        return this;
    }

    protected void checkSession() {
        NSessionUtils.checkSession(model.getWorkspace(), session);
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
                + ", workspace=" + ((model.getCurrentConfig() == null) ? "NULL" : ("'" +
                NLocations.of(NSessionUtils.defaultSession(model.getWorkspace()))
                        .getWorkspaceLocation() + '\''))
                + '}';
    }

    public List<String> getDependencySolverNames() {
        checkSession();
        // the first element is always the default one,
        // the rest is lexicographically sorter
        return Arrays.stream(model.getDependencySolvers(getSession()))
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
        return model.createDependencySolver(name, getSession());
    }

    @Override
    public NSystemTerminal getSystemTerminal() {
        checkSession();
        return NWorkspaceExt.of(session).getModel().bootModel.getSystemTerminal();
    }

    @Override
    public NConfigs setSystemTerminal(NSystemTerminalBase terminal) {
        checkSession();
        NWorkspaceExt.of(session).getModel().bootModel.setSystemTerminal(terminal, getSession());
        return this;
    }

    @Override
    public NSessionTerminal getDefaultTerminal() {
        checkSession();
        return model.getTerminal();
    }

    @Override
    public NConfigs setDefaultTerminal(NSessionTerminal terminal) {
        checkSession();
        model.setTerminal(terminal, session);
        return this;
    }

    @Override
    public Map<String, String> getConfigMap() {
        checkSession();
        return model.getConfigMap();
    }

    @Override
    public NOptional<NLiteral> getConfigProperty(String property) {
        checkSession();
        return model.getConfigProperty(property, getSession());
    }

    @Override
    public NConfigs setConfigProperty(String property, String value) {
        checkSession();
        model.setConfigProperty(property, value, session);
        model.save(getSession());
        return this;
    }

}
