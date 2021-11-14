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
package net.thevpc.nuts.runtime.standalone.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.config.NutsWorkspaceConfigManagerExt;
import net.thevpc.nuts.runtime.core.model.CoreNutsWorkspaceOptions;
import net.thevpc.nuts.runtime.core.util.CoreNutsUtils;
import net.thevpc.nuts.runtime.standalone.solvers.NutsDependencySolverUtils;
import net.thevpc.nuts.runtime.standalone.util.NutsWorkspaceUtils;
import net.thevpc.nuts.spi.NutsDependencySolver;
import net.thevpc.nuts.spi.NutsDependencySolverFactory;
import net.thevpc.nuts.spi.NutsIndexStoreFactory;
import net.thevpc.nuts.spi.NutsSystemTerminalBase;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;

/**
 * @author thevpc
 */
public class DefaultNutsWorkspaceConfigManager implements NutsWorkspaceConfigManager, NutsWorkspaceConfigManagerExt {

    private final DefaultNutsWorkspaceConfigModel model;
    private NutsSession session;

    public DefaultNutsWorkspaceConfigManager(DefaultNutsWorkspaceConfigModel model) {
        this.model = model;
    }

    public DefaultNutsWorkspaceConfigModel getModel() {
        return model;
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
    public NutsPath resolveRepositoryPath(String repositoryLocation) {
        checkSession();
        return model.resolveRepositoryPath(NutsPath.of(repositoryLocation,session), session);
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

    public NutsSession getSession() {
        return session;
    }

    public NutsWorkspaceConfigManager setSession(NutsSession session) {
        this.session = NutsWorkspaceUtils.bindSession(model.getWorkspace(), session);
        return this;
    }

    @Override
    public String getHashName(Object o) {
        if (o == null) {
            return "null";
        }
        if (o instanceof String && o.toString().isEmpty()) {
            return "empty";
        }
        if (o instanceof NutsWorkspace) {
            return getWorkspaceHashName(((NutsWorkspace) o).getLocation());
        }
        if (o instanceof NutsSession) {
            return getWorkspaceHashName(((NutsSession) o).getWorkspace().getLocation());
        }
        if (o instanceof Integer) {
            int i = (int) o;
            return CoreNutsUtils.COLOR_NAMES[Math.abs(i) % CoreNutsUtils.COLOR_NAMES.length];
        }
        return getHashName(o.hashCode());
    }

    @Override
    public String getWorkspaceHashName(String path) {
        if (path == null) {
            path = "";
        }
        String n;
        String p;
        if (path.contains("\\") || path.contains("/") || path.equals(".") || path.equals("..")) {
            Path pp = Paths.get(path).toAbsolutePath().normalize();
            n = pp.getFileName().toString();
            p = pp.getParent() == null ? null : pp.getParent().toString();
        } else {
            n = path;
            p = "";
        }
        if (p == null) {
            return ("Root " + n).trim();
        } else {
            Path root = Paths.get(NutsUtilPlatforms.getWorkspaceLocation(
                    null,
                    false,
                    null
            )).getParent().getParent();
            if (p.equals(root.toString())) {
                return n;
            }
            return (getHashName(p) + " " + n).trim();
        }
    }

    protected void checkSession() {
        NutsWorkspaceUtils.checkSession(model.getWorkspace(), session);
    }

    public String getWorkspaceHashName() {
        return getHashName(model.getWorkspace());
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
                NutsWorkspaceUtils.defaultSession(model.getWorkspace())
                        .locations().getWorkspaceLocation() + '\''))
                + '}';
    }

    public String[] getDependencySolverNames() {
        checkSession();
        // the first element is always the default one,
        // the rest is lexicographically sorter
        return Arrays.stream(model.getDependencySolvers(getSession()))
                .map(NutsDependencySolverFactory::getName)
                .sorted(new Comparator<String>() {
                    @Override
                    public int compare(String o1, String o2) {
                        if(!o1.equals(o2)){
                            String n = NutsDependencySolverUtils.resolveSolverName(session.getDependencySolver());
                            if(o1.equals(n)){
                                return -1;
                            }
                            if(o2.equals(n)){
                                return 1;
                            }
                        }
                        return o1.compareTo(o2);
                    }
                })
                .toArray(String[]::new)
                ;
    }

    public NutsDependencySolver createDependencySolver(String name) {
        checkSession();
        return model.createDependencySolver(name, getSession());
    }

    @Override
    public ExecutorService executorService() {
        checkSession();
        return model.executorService(getSession());
    }

    @Override
    public NutsSystemTerminal getSystemTerminal() {
        checkSession();
        return model.getSystemTerminal();
    }

    @Override
    public NutsWorkspaceConfigManager setSystemTerminal(NutsSystemTerminalBase terminal) {
        checkSession();
        model.setSystemTerminal(terminal, session);
        return this;
    }

    @Override
    public NutsSessionTerminal getDefaultTerminal() {
        checkSession();
        return model.getTerminal();
    }

    @Override
    public NutsWorkspaceConfigManager setDefaultTerminal(NutsSessionTerminal terminal) {
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
    public NutsVal getConfigProperty(String property) {
        checkSession();
        return model.getConfigProperty(property);
    }

    @Override
    public NutsWorkspaceConfigManager setConfigProperty(String property, String value) {
        checkSession();
        model.setConfigProperty(property, value, session);
        model.save(getSession());
        return this;
    }

}
