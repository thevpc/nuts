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
 *
 * Copyright [2020] [thevpc]
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an 
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language 
 * governing permissions and limitations under the License.
 * <br>
 * ====================================================================
*/
package net.thevpc.nuts.runtime.standalone.workspace.config.compat.v502;

import java.io.Serializable;
import java.util.*;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.workspace.config.NutsStoreLocationsMap;

/**
 *
 * @author thevpc
 * @since 0.5.4
 */
public class NutsRepositoryConfig502 implements Serializable {

    private static final long serialVersionUID = 1;
    /**
     * Api version having created the config
     */
    private String uuid;
    private String name;
    private String type;
    private String location;
    private String programsStoreLocation = null;
    private String configStoreLocation = null;
    private String varStoreLocation = null;
    private String libStoreLocation = null;
    private String logStoreLocation = null;
    private String tempStoreLocation = null;
    private String cacheStoreLocation = null;
    private String runStoreLocation = null;
    private NutsStoreLocationStrategy storeLocationStrategy = null;
    private String groups;
    private Map<String,String> env;
    private List<NutsRepositoryRef> mirrors;
    private List<NutsUserConfig> users;
    private boolean indexEnabled;
    private String authenticationAgent;

    public NutsRepositoryConfig502() {
    }

    public NutsRepositoryConfig502(NutsRepositoryConfig502 other) {
        this.name = other.getName();
        this.uuid = other.getUuid();
        this.location = other.getLocation();
        this.type = other.getType();
        this.groups = other.getGroups();
        this.programsStoreLocation = other.programsStoreLocation;
        this.configStoreLocation = other.configStoreLocation;
        this.varStoreLocation = other.varStoreLocation;
        this.libStoreLocation = other.libStoreLocation;
        this.logStoreLocation = other.logStoreLocation;
        this.tempStoreLocation = other.tempStoreLocation;
        this.cacheStoreLocation = other.cacheStoreLocation;
        this.storeLocationStrategy = other.storeLocationStrategy;
        this.indexEnabled = other.indexEnabled;
        this.authenticationAgent = other.authenticationAgent;
        this.mirrors = other.getMirrors() == null ? null : new ArrayList<>(other.getMirrors());
        this.users = other.getUsers() == null ? null : new ArrayList<>(other.getUsers());
        if (other.getEnv() == null) {
            this.env = null;
        } else {
            this.env = new LinkedHashMap<>();
            this.env.putAll(other.getEnv());
        }
    }

    public NutsRepositoryConfig502(String name, String location, String type) {
        this.name = name;
        this.location = location;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public NutsRepositoryConfig502 setName(String name) {
        this.name = name;
        return this;
    }

    public String getUuid() {
        return uuid;
    }

    public NutsRepositoryConfig502 setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getType() {
        return type;
    }

    public NutsRepositoryConfig502 setType(String type) {
        this.type = type;
        return this;
    }

    public String getLocation() {
        return location;
    }

    public NutsRepositoryConfig502 setLocation(String location) {
        this.location = location;
        return this;
    }

    public String getProgramsStoreLocation() {
        return programsStoreLocation;
    }

    public NutsRepositoryConfig502 setProgramsStoreLocation(String programsStoreLocation) {
        this.programsStoreLocation = programsStoreLocation;
        return this;
    }

    public String getConfigStoreLocation() {
        return configStoreLocation;
    }

    public NutsRepositoryConfig502 setConfigStoreLocation(String configStoreLocation) {
        this.configStoreLocation = configStoreLocation;
        return this;
    }

    public String getVarStoreLocation() {
        return varStoreLocation;
    }

    public NutsRepositoryConfig502 setVarStoreLocation(String varStoreLocation) {
        this.varStoreLocation = varStoreLocation;
        return this;
    }

    public String getLibStoreLocation() {
        return libStoreLocation;
    }

    public NutsRepositoryConfig502 setLibStoreLocation(String libStoreLocation) {
        this.libStoreLocation = libStoreLocation;
        return this;
    }

    public String getLogStoreLocation() {
        return logStoreLocation;
    }

    public NutsRepositoryConfig502 setLogStoreLocation(String logStoreLocation) {
        this.logStoreLocation = logStoreLocation;
        return this;
    }

    public String getTempStoreLocation() {
        return tempStoreLocation;
    }

    public NutsRepositoryConfig502 setTempStoreLocation(String tempStoreLocation) {
        this.tempStoreLocation = tempStoreLocation;
        return this;
    }

    public String getCacheStoreLocation() {
        return cacheStoreLocation;
    }

    public NutsRepositoryConfig502 setCacheStoreLocation(String cacheStoreLocation) {
        this.cacheStoreLocation = cacheStoreLocation;
        return this;
    }

    public String getRunStoreLocation() {
        return runStoreLocation;
    }

    public NutsRepositoryConfig502 setRunStoreLocation(String runStoreLocation) {
        this.runStoreLocation = runStoreLocation;
        return this;
    }

    public NutsStoreLocationStrategy getStoreLocationStrategy() {
        return storeLocationStrategy;
    }

    public NutsRepositoryConfig502 setStoreLocationStrategy(NutsStoreLocationStrategy storeLocationStrategy) {
        this.storeLocationStrategy = storeLocationStrategy;
        return this;
    }

    public String getGroups() {
        return groups;
    }

    public NutsRepositoryConfig502 setGroups(String groups) {
        this.groups = groups;
        return this;
    }

    public Map<String,String> getEnv() {
        return env;
    }

    public NutsRepositoryConfig502 setEnv(Map<String,String> env) {
        this.env = env;
        return this;
    }

    public List<NutsRepositoryRef> getMirrors() {
        return mirrors;
    }

    public NutsRepositoryConfig502 setMirrors(List<NutsRepositoryRef> mirrors) {
        this.mirrors = mirrors;
        return this;
    }

    public NutsRepositoryConfig502 setUsers(List<NutsUserConfig> users) {
        this.users = users;
        return this;
    }

    public List<NutsUserConfig> getUsers() {
        return users;
    }

    public boolean isIndexEnabled() {
        return indexEnabled;
    }

    public NutsRepositoryConfig502 setIndexEnabled(boolean indexEnabled) {
        this.indexEnabled = indexEnabled;
        return this;
    }

    public String getAuthenticationAgent() {
        return authenticationAgent;
    }

    public NutsRepositoryConfig502 setAuthenticationAgent(String authenticationAgent) {
        this.authenticationAgent = authenticationAgent;
        return this;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 59 * hash + Objects.hashCode(this.uuid);
        hash = 59 * hash + Objects.hashCode(this.name);
        hash = 59 * hash + Objects.hashCode(this.type);
        hash = 59 * hash + Objects.hashCode(this.location);
        hash = 59 * hash + Objects.hashCode(this.programsStoreLocation);
        hash = 59 * hash + Objects.hashCode(this.configStoreLocation);
        hash = 59 * hash + Objects.hashCode(this.varStoreLocation);
        hash = 59 * hash + Objects.hashCode(this.libStoreLocation);
        hash = 59 * hash + Objects.hashCode(this.logStoreLocation);
        hash = 59 * hash + Objects.hashCode(this.tempStoreLocation);
        hash = 59 * hash + Objects.hashCode(this.cacheStoreLocation);
        hash = 59 * hash + Objects.hashCode(this.runStoreLocation);
        hash = 59 * hash + Objects.hashCode(this.storeLocationStrategy);
        hash = 59 * hash + Objects.hashCode(this.groups);
        hash = 59 * hash + Objects.hashCode(this.env);
        hash = 59 * hash + Objects.hashCode(this.mirrors);
        hash = 59 * hash + Objects.hashCode(this.users);
        hash = 59 * hash + (this.indexEnabled ? 1 : 0);
        hash = 59 * hash + Objects.hashCode(this.authenticationAgent);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final NutsRepositoryConfig502 other = (NutsRepositoryConfig502) obj;
        if (this.indexEnabled != other.indexEnabled) {
            return false;
        }
        if (!Objects.equals(this.uuid, other.uuid)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
        if (!Objects.equals(this.type, other.type)) {
            return false;
        }
        if (!Objects.equals(this.location, other.location)) {
            return false;
        }
        if (!Objects.equals(this.programsStoreLocation, other.programsStoreLocation)) {
            return false;
        }
        if (!Objects.equals(this.configStoreLocation, other.configStoreLocation)) {
            return false;
        }
        if (!Objects.equals(this.varStoreLocation, other.varStoreLocation)) {
            return false;
        }
        if (!Objects.equals(this.libStoreLocation, other.libStoreLocation)) {
            return false;
        }
        if (!Objects.equals(this.logStoreLocation, other.logStoreLocation)) {
            return false;
        }
        if (!Objects.equals(this.tempStoreLocation, other.tempStoreLocation)) {
            return false;
        }
        if (!Objects.equals(this.cacheStoreLocation, other.cacheStoreLocation)) {
            return false;
        }
        if (!Objects.equals(this.runStoreLocation, other.runStoreLocation)) {
            return false;
        }
        if (!Objects.equals(this.groups, other.groups)) {
            return false;
        }
        if (!Objects.equals(this.authenticationAgent, other.authenticationAgent)) {
            return false;
        }
        if (this.storeLocationStrategy != other.storeLocationStrategy) {
            return false;
        }
        if (!Objects.equals(this.env, other.env)) {
            return false;
        }
        if (!Objects.equals(this.mirrors, other.mirrors)) {
            return false;
        }
        if (!Objects.equals(this.users, other.users)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return "NutsRepositoryConfig{" + "uuid=" + uuid + ", name=" + name + ", type=" + type + ", location=" + location + ", programsStoreLocation=" + programsStoreLocation + ", configStoreLocation=" + configStoreLocation + ", varStoreLocation=" + varStoreLocation + ", libStoreLocation=" + libStoreLocation + ", logStoreLocation=" + logStoreLocation + ", tempStoreLocation=" + tempStoreLocation + ", cacheStoreLocation=" + cacheStoreLocation + ", runStoreLocation=" + runStoreLocation + ", storeLocationStrategy=" + storeLocationStrategy + ", groups=" + groups + ", env=" + env + ", mirrors=" + mirrors + ", users=" + users + ", indexEnabled=" + indexEnabled + ", authenticationAgent=" + authenticationAgent + '}';
    }

    public NutsRepositoryConfig toRepositoryConfig() {
        NutsRepositoryConfig c = new NutsRepositoryConfig();
        c.setConfigVersion("0.5.2");
        c.setAuthenticationAgent(authenticationAgent);
        c.setEnv(env);
        c.setGroups(groups);
        c.setIndexEnabled(indexEnabled);
        c.setLocation(location);
        c.setMirrors(mirrors);
        c.setName(name);
        c.setStoreLocationStrategy(storeLocationStrategy);
        c.setType(type);
        c.setStoreLocations(
                new NutsStoreLocationsMap(null)
                        .set(NutsStoreLocation.APPS, programsStoreLocation)
                        .set(NutsStoreLocation.CONFIG, configStoreLocation)
                        .set(NutsStoreLocation.VAR, varStoreLocation)
                        .set(NutsStoreLocation.LOG, logStoreLocation)
                        .set(NutsStoreLocation.TEMP, tempStoreLocation)
                        .set(NutsStoreLocation.CACHE, cacheStoreLocation)
                        .set(NutsStoreLocation.LIB, libStoreLocation)
                .toMap()
        );
        return c;
    }
}
