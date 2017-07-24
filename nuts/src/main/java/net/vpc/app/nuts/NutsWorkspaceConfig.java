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

import net.vpc.app.nuts.util.JsonTransient;
import net.vpc.app.nuts.util.NutsUtils;
import net.vpc.app.nuts.util.StringUtils;

import java.util.*;

/**
 * Created by vpc on 1/7/17.
 */
public class NutsWorkspaceConfig {
    private static final long serialVersionUID=1;
    private String workspace = null;
    private Map<String, NutsRepositoryLocation> repositories = new LinkedHashMap<>();
    private List<String> extensions = new ArrayList<>();
    private Properties env = new Properties();
    private Map<String, NutsSecurityEntityConfig> security = new HashMap<>();
    private Set<String> imports = new HashSet<>();
    private long instanceSerialVersionUID = serialVersionUID;

    public long getInstanceSerialVersionUID() {
        return instanceSerialVersionUID;
    }

    public void setInstanceSerialVersionUID(long instanceSerialVersionUID) {
        this.instanceSerialVersionUID = instanceSerialVersionUID;
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

    public void setRepositories(NutsRepositoryLocation[] repositories) {
        this.repositories.clear();
        for (NutsRepositoryLocation repository : repositories) {
            addRepository(repository);
        }
    }

    public void addImport(String importExpression) {
        if (importExpression != null && !importExpression.trim().isEmpty()) {
            for (String s : importExpression.split("[,;: ]")) {
                imports.add(s.trim());
            }
        }
    }

    public void removeAllImports() {
        imports.clear();
    }

    public void removeImport(String importExpression) {
        if (importExpression != null && !importExpression.trim().isEmpty()) {
            for (String s : importExpression.split("[,;: ]")) {
                imports.remove(s.trim());
            }
        }
    }

    public void setImports(String[] imports) {
        if(imports!=null){
            for (String anImport : imports) {
                addImport(anImport);
            }
        }
    }

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


    public NutsRepositoryLocation getRepository(String repositoryId) {
        return this.repositories.get(repositoryId);
    }

    public void addRepository(NutsRepositoryLocation repository) {
        if (repository == null) {
            throw new IllegalArgumentException("Invalid Repository");
        }
        if (StringUtils.isEmpty(repository.getId())) {
            throw new IllegalArgumentException("Invalid Repository Id");
        }
        if (StringUtils.isEmpty(repository.getType())) {
            repository.setType(NutsConstants.DEFAULT_REPOSITORY_TYPE);
        }
        if (this.repositories.containsKey(repository.getId())) {
            throw new IllegalArgumentException("Duplicate Repository Id " + repository.getId());
        }
        this.repositories.put(repository.getId(), repository);
    }

    public void removeRepository(String repositoryId) {
        if (repositoryId == null) {
            throw new IllegalArgumentException("Invalid Null Repository");
        }
        this.repositories.remove(repositoryId);
    }

    public boolean addExtension(NutsId extensionId) {
        if (extensionId == null) {
            throw new IllegalArgumentException("Invalid Extension");
        }
        NutsId oldId2 = NutsUtils.finNutsIdByFullNameInStrings(extensionId, extensions);
        if (oldId2 == null) {
            this.extensions.add(extensionId.toString());
            return true;
        }
        return false;
    }

    public void addExtension(String extensionId) {
        addExtension(NutsId.parseOrError(extensionId));
    }

    public boolean removeExtension(NutsId extensionId) {
        if (extensionId == null) {
            throw new IllegalArgumentException("Invalid Extension");
        }
        NutsId oldId2 = NutsUtils.finNutsIdByFullNameInStrings(extensionId, extensions);
        if (oldId2 != null) {
            this.extensions.remove(oldId2.toString());
            return true;
        }
        return false;
    }

    public String[] getExtensions() {
        return extensions.toArray(new String[extensions.size()]);
    }

    public void setExtensions(String[] extensions) {
        this.extensions = new ArrayList<>();
        for (String extensionId : extensions) {
            addExtension(extensionId);
        }
    }

    public Properties getEnv() {
        return env;
    }

    public void setEnv(Properties env) {
        this.env = env;
    }

    public void setEnv(String property, String value) {
        if (StringUtils.isEmpty(value)) {
            getEnv().remove(property);
        } else {
            getEnv().setProperty(property, value);
        }
    }

    public String getEnv(String property, String defaultValue) {
        String o = getEnv().getProperty(property);
        if (StringUtils.isEmpty(o)) {
            return defaultValue;
        }
        return o;
    }

    public void removeSecurity(String securityId) {
        security.remove(securityId);
    }

    public void setSecurity(NutsSecurityEntityConfig securityEntityConfig) {
        if (securityEntityConfig != null) {
            security.put(securityEntityConfig.getUser(), securityEntityConfig);
        }
    }

    public NutsSecurityEntityConfig getSecurity(String id) {
        NutsSecurityEntityConfig config = security.get(id);
        if (config == null) {
            if (NutsConstants.USER_ADMIN.equals(id) || NutsConstants.USER_ANONYMOUS.equals(id)) {
                config = new NutsSecurityEntityConfig(id, null, null, null);
                security.put(id, config);
            }
        }
        return config;
    }

    public NutsSecurityEntityConfig[] getSecurity() {
        return security.values().toArray(new NutsSecurityEntityConfig[security.size()]);
    }

    public void setSecurity(NutsSecurityEntityConfig[] securityEntityConfigs) {
        this.security.clear();
        for (NutsSecurityEntityConfig conf : securityEntityConfigs) {
            setSecurity(conf);
        }
    }

    public static class NutsRepositoryLocation {
        @JsonTransient
        private static final long serialVersionUID=1;

        private String id;
        private String type;
        private String location;
        private boolean enabled = true;
        private long instanceSerialVersionUID = serialVersionUID;

        public NutsRepositoryLocation() {
        }

        public NutsRepositoryLocation(String id, String location, String type) {
            this.id = id;
            this.type = type;
            this.location = location;
        }

        public long getInstanceSerialVersionUID() {
            return instanceSerialVersionUID;
        }

        public void setInstanceSerialVersionUID(long instanceSerialVersionUID) {
            this.instanceSerialVersionUID = instanceSerialVersionUID;
        }


        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getId() {
            return id;
        }

        public NutsRepositoryLocation setId(String id) {
            this.id = id;
            return this;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getLocation() {
            return location;
        }

        public void setLocation(String location) {
            this.location = location;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }

            NutsRepositoryLocation that = (NutsRepositoryLocation) o;

            if (type != null ? !type.equals(that.type) : that.type != null) {
                return false;
            }
            return location != null ? location.equals(that.location) : that.location == null;

        }

        @Override
        public int hashCode() {
            int result = type != null ? type.hashCode() : 0;
            result = 31 * result + (location != null ? location.hashCode() : 0);
            return result;
        }
    }

}
