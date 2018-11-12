/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.io.Serializable;
import java.util.*;

public final class NutsWorkspaceConfig implements Serializable {

    private static final long serialVersionUID = 1;
    private boolean secure = false;
    private String workspace = null;
    private String componentsLocation = null;
    private final Map<String, NutsRepositoryLocation> repositories = new LinkedHashMap<>();
    private List<NutsId> extensions = new ArrayList<>();
    private Properties env = new Properties();
    private Map<String, NutsUserConfig> security = new HashMap<>();
    private Map<String, List<NutsSdkLocation>> sdk = new HashMap<>();
    private String[] imports = new String[0];

    public NutsWorkspaceConfig() {
    }

    public NutsWorkspaceConfig(NutsWorkspaceConfig other) {
        this.secure = other.isSecure();
        this.workspace = other.getWorkspace();
        this.componentsLocation = other.getComponentsLocation();
        for (NutsRepositoryLocation repository : other.getRepositories()) {
            this.repositories.put(repository.getId(), repository);
        }
        for (NutsUserConfig repository : other.getSecurity()) {
            this.security.put(repository.getUser(), repository);
        }
        for (Map.Entry<String, List<NutsSdkLocation>> e : other.getSdk().entrySet()) {
            List<NutsSdkLocation> value = e.getValue();
            this.sdk.put(e.getKey(), value==null?new ArrayList<>() : new ArrayList<>(value));
        }
        this.extensions.addAll(Arrays.asList(other.getExtensions()));
        this.env.putAll(other.getEnv());
        this.imports = other.getImports();
    }

    public String getComponentsLocation() {
        return componentsLocation;
    }

    public void setComponentsLocation(String componentsLocation) {
        this.componentsLocation = componentsLocation;
    }

    public String getWorkspace() {
        return workspace;
    }


    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }


    public NutsRepositoryLocation[] getRepositories() {
        return repositories.values().toArray(new NutsRepositoryLocation[repositories.size()]);
    }


    public String[] getImports() {
        return imports;
    }


    public void setImports(String[] imports) {
        this.imports = imports;
    }


    public NutsRepositoryLocation getRepository(String repositoryId) {
        return this.repositories.get(repositoryId);
    }


    public void addRepository(NutsRepositoryLocation repository) {
        this.repositories.put(repository.getId(), repository);
    }


    public void removeRepository(String repositoryId) {
        this.repositories.remove(repositoryId);
    }


    public boolean containsRepository(String repositoryId) {
        return repositories.containsKey(repositoryId);
    }


    public void addExtension(NutsId extensionId) {
        this.extensions.add(extensionId);
    }


    public void removeExtension(NutsId extensionId) {
        extensions.remove(extensionId);
    }


    public NutsId[] getExtensions() {
        return extensions.toArray(new NutsId[extensions.size()]);
    }


    public Properties getEnv() {
        return env;
    }


    public void setEnv(Properties env) {
        this.env = env;
    }


    public void removeSecurity(String securityId) {
        security.remove(securityId);
    }


    public void setSecurity(NutsUserConfig securityEntityConfig) {
        if (securityEntityConfig != null) {
            security.put(securityEntityConfig.getUser(), securityEntityConfig);
        }
    }


    public NutsUserConfig getSecurity(String id) {
        return security.get(id);
    }


    public NutsUserConfig[] getSecurity() {
        return security.values().toArray(new NutsUserConfig[security.size()]);
    }

    public Map<String, List<NutsSdkLocation>> getSdk() {
        return sdk==null?new HashMap<>() :sdk;
    }


    public boolean isSecure() {
        return secure;
    }


    public void setSecure(boolean secure) {
        this.secure = secure;
    }


}
