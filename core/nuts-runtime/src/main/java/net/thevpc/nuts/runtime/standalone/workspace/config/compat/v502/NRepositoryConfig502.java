/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <br>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . It's based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <br>
 *
 * Copyright [2020] [thevpc]
 * Licensed under the GNU LESSER GENERAL PUBLIC LICENSE Version 3 (the "License");
 * you may  not use this file except in compliance with the License. You may obtain
 * a copy of the License at https://www.gnu.org/licenses/lgpl-3.0.en.html
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
import net.thevpc.nuts.NStoreStrategy;
import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.NUserConfig;
import net.thevpc.nuts.runtime.standalone.workspace.config.NStoreLocationsMap;
import net.thevpc.nuts.spi.NRepositoryLocation;
import net.thevpc.nuts.util.NBlankable;

/**
 *
 * @author thevpc
 * @since 0.5.4
 */
public class NRepositoryConfig502 implements Serializable {

    private static final long serialVersionUID = 1;
    public static final NVersion CONFIG_VERSION_502 = NVersion.get("0.5.2").get();
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
    private NStoreStrategy storeLocationStrategy = null;
    private String groups;
    private Map<String,String> env;
    private List<NRepositoryRef> mirrors;
    private List<NUserConfig> users;
    private boolean indexEnabled;
    private String authenticationAgent;

    public NRepositoryConfig502() {
    }

    public NRepositoryConfig502(NRepositoryConfig502 other) {
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

    public NRepositoryConfig502(String name, String location, String type) {
        this.name = name;
        this.location = location;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public NRepositoryConfig502 setName(String name) {
        this.name = name;
        return this;
    }

    public String getUuid() {
        return uuid;
    }

    public NRepositoryConfig502 setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getType() {
        return type;
    }

    public NRepositoryConfig502 setType(String type) {
        this.type = type;
        return this;
    }

    public String getLocation() {
        return location;
    }

    public NRepositoryConfig502 setLocation(String location) {
        this.location = location;
        return this;
    }

    public String getProgramsStoreLocation() {
        return programsStoreLocation;
    }

    public NRepositoryConfig502 setProgramsStoreLocation(String programsStoreLocation) {
        this.programsStoreLocation = programsStoreLocation;
        return this;
    }

    public String getConfigStoreLocation() {
        return configStoreLocation;
    }

    public NRepositoryConfig502 setConfigStoreLocation(String configStoreLocation) {
        this.configStoreLocation = configStoreLocation;
        return this;
    }

    public String getVarStoreLocation() {
        return varStoreLocation;
    }

    public NRepositoryConfig502 setVarStoreLocation(String varStoreLocation) {
        this.varStoreLocation = varStoreLocation;
        return this;
    }

    public String getLibStoreLocation() {
        return libStoreLocation;
    }

    public NRepositoryConfig502 setLibStoreLocation(String libStoreLocation) {
        this.libStoreLocation = libStoreLocation;
        return this;
    }

    public String getLogStoreLocation() {
        return logStoreLocation;
    }

    public NRepositoryConfig502 setLogStoreLocation(String logStoreLocation) {
        this.logStoreLocation = logStoreLocation;
        return this;
    }

    public String getTempStoreLocation() {
        return tempStoreLocation;
    }

    public NRepositoryConfig502 setTempStoreLocation(String tempStoreLocation) {
        this.tempStoreLocation = tempStoreLocation;
        return this;
    }

    public String getCacheStoreLocation() {
        return cacheStoreLocation;
    }

    public NRepositoryConfig502 setCacheStoreLocation(String cacheStoreLocation) {
        this.cacheStoreLocation = cacheStoreLocation;
        return this;
    }

    public String getRunStoreLocation() {
        return runStoreLocation;
    }

    public NRepositoryConfig502 setRunStoreLocation(String runStoreLocation) {
        this.runStoreLocation = runStoreLocation;
        return this;
    }

    public NStoreStrategy getStoreLocationStrategy() {
        return storeLocationStrategy;
    }

    public NRepositoryConfig502 setStoreLocationStrategy(NStoreStrategy storeLocationStrategy) {
        this.storeLocationStrategy = storeLocationStrategy;
        return this;
    }

    public String getGroups() {
        return groups;
    }

    public NRepositoryConfig502 setGroups(String groups) {
        this.groups = groups;
        return this;
    }

    public Map<String,String> getEnv() {
        return env;
    }

    public NRepositoryConfig502 setEnv(Map<String,String> env) {
        this.env = env;
        return this;
    }

    public List<NRepositoryRef> getMirrors() {
        return mirrors;
    }

    public NRepositoryConfig502 setMirrors(List<NRepositoryRef> mirrors) {
        this.mirrors = mirrors;
        return this;
    }

    public NRepositoryConfig502 setUsers(List<NUserConfig> users) {
        this.users = users;
        return this;
    }

    public List<NUserConfig> getUsers() {
        return users;
    }

    public boolean isIndexEnabled() {
        return indexEnabled;
    }

    public NRepositoryConfig502 setIndexEnabled(boolean indexEnabled) {
        this.indexEnabled = indexEnabled;
        return this;
    }

    public String getAuthenticationAgent() {
        return authenticationAgent;
    }

    public NRepositoryConfig502 setAuthenticationAgent(String authenticationAgent) {
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
        final NRepositoryConfig502 other = (NRepositoryConfig502) obj;
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
        return "NutsRepositoryConfig507{" + "uuid=" + uuid + ", name=" + name + ", type=" + type + ", location=" + location + ", programsStoreLocation=" + programsStoreLocation + ", configStoreLocation=" + configStoreLocation + ", varStoreLocation=" + varStoreLocation + ", libStoreLocation=" + libStoreLocation + ", logStoreLocation=" + logStoreLocation + ", tempStoreLocation=" + tempStoreLocation + ", cacheStoreLocation=" + cacheStoreLocation + ", runStoreLocation=" + runStoreLocation + ", storeLocationStrategy=" + storeLocationStrategy + ", groups=" + groups + ", env=" + env + ", mirrors=" + mirrors + ", users=" + users + ", indexEnabled=" + indexEnabled + ", authenticationAgent=" + authenticationAgent + '}';
    }

    public NRepositoryConfig toRepositoryConfig() {
        NRepositoryConfig c = new NRepositoryConfig();
        c.setConfigVersion(CONFIG_VERSION_502);
        c.setAuthenticationAgent(authenticationAgent);
        c.setEnv(env);
        c.setGroups(groups);
        c.setIndexEnabled(indexEnabled);
        c.setMirrors(mirrors);
        c.setName(name);
        c.setStoreStrategy(storeLocationStrategy);
        NRepositoryLocation loc = NRepositoryLocation.of(location);
        if(!NBlankable.isBlank(type)){
            loc=loc.setLocationType(type);
        }
        c.setLocation(NRepositoryLocation.of(loc.toString()));
        c.setStoreLocations(
                new NStoreLocationsMap(null)
                        .set(NStoreType.BIN, programsStoreLocation)
                        .set(NStoreType.CONF, configStoreLocation)
                        .set(NStoreType.VAR, varStoreLocation)
                        .set(NStoreType.LOG, logStoreLocation)
                        .set(NStoreType.TEMP, tempStoreLocation)
                        .set(NStoreType.CACHE, cacheStoreLocation)
                        .set(NStoreType.LIB, libStoreLocation)
                .toMap()
        );
        return c;
    }
}
