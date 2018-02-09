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
package net.vpc.app.nuts.extensions.core;

import net.vpc.app.nuts.*;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;
import net.vpc.app.nuts.extensions.util.CoreStringUtils;

import java.util.*;

/**
 * Created by vpc on 1/7/17.
 */
public class NutsWorkspaceConfigImpl implements NutsWorkspaceConfig {
    private static final long serialVersionUID = 1;
    private boolean secure = false;
    private String workspace = null;
    private Map<String, NutsRepositoryLocation> repositories = new LinkedHashMap<>();
    private List<String> extensions = new ArrayList<>();
    private Properties env = new Properties();
    private Map<String, NutsSecurityEntityConfig> security = new HashMap<>();
    private Set<String> imports = new HashSet<>();
    private long instanceSerialVersionUID = serialVersionUID;

    @Override
    public long getInstanceSerialVersionUID() {
        return instanceSerialVersionUID;
    }

    @Override
    public void setInstanceSerialVersionUID(long instanceSerialVersionUID) {
        this.instanceSerialVersionUID = instanceSerialVersionUID;
    }

    @Override
    public String getWorkspace() {
        return workspace;
    }

    @Override
    public void setWorkspace(String workspace) {
        this.workspace = workspace;
    }

    @Override
    public NutsRepositoryLocation[] getRepositories() {
        return repositories.values().toArray(new NutsRepositoryLocation[repositories.size()]);
    }

    @Override
    public void setRepositories(NutsRepositoryLocation[] repositories) {
        this.repositories.clear();
        for (NutsRepositoryLocation repository : repositories) {
            addRepository(repository);
        }
    }

    @Override
    public void addImport(String importExpression) {
        if (importExpression != null && !importExpression.trim().isEmpty()) {
            for (String s : importExpression.split("[,;: ]")) {
                imports.add(s.trim());
            }
        }
    }

    @Override
    public void removeAllImports() {
        imports.clear();
    }

    @Override
    public void removeImport(String importExpression) {
        if (importExpression != null && !importExpression.trim().isEmpty()) {
            for (String s : importExpression.split("[,;: ]")) {
                imports.remove(s.trim());
            }
        }
    }

    @Override
    public String[] getImports() {
        HashSet<String> all = new HashSet<>(imports);
//        public static final String ENV_KEY_IMPORTS = "imports";
        //workaround
        String extraImports = env.getProperty("imports");
        if (extraImports != null) {
            for (String s : extraImports.split("[,;: ]")) {
                addImport(s);
            }
            env.remove("imports");
        }
        return all.toArray(new String[all.size()]);
    }

    @Override
    public void setImports(String[] imports) {
        if (imports != null) {
            for (String anImport : imports) {
                addImport(anImport);
            }
        }
    }

    @Override
    public NutsRepositoryLocation getRepository(String repositoryId) {
        return this.repositories.get(repositoryId);
    }

    @Override
    public void addRepository(NutsRepositoryLocation repository) {
        if (repository == null) {
            throw new NutsIllegalArgumentsException("Invalid Repository");
        }
        if (CoreStringUtils.isEmpty(repository.getId())) {
            throw new NutsIllegalArgumentsException("Invalid Repository Id");
        }
        if (CoreStringUtils.isEmpty(repository.getType())) {
            repository.setType(NutsConstants.DEFAULT_REPOSITORY_TYPE);
        }
        if (this.repositories.containsKey(repository.getId())) {
            throw new NutsIllegalArgumentsException("Duplicate Repository Id " + repository.getId());
        }
        this.repositories.put(repository.getId(), repository);
    }

    @Override
    public void removeRepository(String repositoryId) {
        if (repositoryId == null) {
            throw new NutsIllegalArgumentsException("Invalid Null Repository");
        }
        this.repositories.remove(repositoryId);
    }

    @Override
    public boolean addExtension(NutsId extensionId) {
        if (extensionId == null) {
            throw new NutsIllegalArgumentsException("Invalid Extension");
        }
        NutsId oldId2 = CoreNutsUtils.finNutsIdByFullNameInStrings(extensionId, extensions);
        if (oldId2 == null) {
            this.extensions.add(extensionId.toString());
            return true;
        }
        return false;
    }

    @Override
    public void addExtension(String extensionId) {
        addExtension(CoreNutsUtils.parseOrErrorNutsId(extensionId));
    }

    @Override
    public boolean removeExtension(NutsId extensionId) {
        if (extensionId == null) {
            throw new NutsIllegalArgumentsException("Invalid Extension");
        }
        NutsId oldId2 = CoreNutsUtils.finNutsIdByFullNameInStrings(extensionId, extensions);
        if (oldId2 != null) {
            this.extensions.remove(oldId2.toString());
            return true;
        }
        return false;
    }

    @Override
    public String[] getExtensions() {
        return extensions.toArray(new String[extensions.size()]);
    }

    @Override
    public void setExtensions(String[] extensions) {
        this.extensions = new ArrayList<>();
        for (String extensionId : extensions) {
            addExtension(extensionId);
        }
    }

    @Override
    public Properties getEnv() {
        return env;
    }

    @Override
    public void setEnv(Properties env) {
        this.env = env;
    }

    @Override
    public void setEnv(String property, String value) {
        if (CoreStringUtils.isEmpty(value)) {
            getEnv().remove(property);
        } else {
            getEnv().setProperty(property, value);
        }
    }

    @Override
    public String getEnv(String property, String defaultValue) {
        String o = getEnv().getProperty(property);
        if (CoreStringUtils.isEmpty(o)) {
            return defaultValue;
        }
        return o;
    }

    @Override
    public void removeSecurity(String securityId) {
        security.remove(securityId);
    }

    @Override
    public void setSecurity(NutsSecurityEntityConfig securityEntityConfig) {
        if (securityEntityConfig != null) {
            security.put(securityEntityConfig.getUser(), securityEntityConfig);
        }
    }

    @Override
    public NutsSecurityEntityConfig getSecurity(String id) {
        NutsSecurityEntityConfig config = security.get(id);
        if (config == null) {
            if (NutsConstants.USER_ADMIN.equals(id) || NutsConstants.USER_ANONYMOUS.equals(id)) {
                config = new NutsSecurityEntityConfigImpl(id, null, null, null);
                security.put(id, config);
            }
        }
        return config;
    }

    @Override
    public NutsSecurityEntityConfig[] getSecurity() {
        return security.values().toArray(new NutsSecurityEntityConfig[security.size()]);
    }

    @Override
    public void setSecurity(NutsSecurityEntityConfig[] securityEntityConfigs) {
        this.security.clear();
        for (NutsSecurityEntityConfig conf : securityEntityConfigs) {
            setSecurity(conf);
        }
    }

    @Override
    public boolean isSecure() {
        return secure;
    }

    @Override
    public void setSecure(boolean secure) {
        this.secure = secure;
    }
}
