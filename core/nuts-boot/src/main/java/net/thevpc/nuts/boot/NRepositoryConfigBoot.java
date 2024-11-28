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
 * <br>
 * ====================================================================
 */
package net.thevpc.nuts.boot;


import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author thevpc
 * @app.category Config
 * @since 0.5.4
 */
public class NRepositoryConfigBoot implements Serializable,Cloneable {

    private static final long serialVersionUID = 1;
    private String uuid;
    private String name;
    private NRepositoryLocationBoot location;
    private Map<String, String> storeLocations = null;
    private String storeStrategy = null;
    private String groups;
    private Map<String, String> env;
    private List<NRepositoryRefBoot> mirrors;
    private List<NUserConfigBoot> users;
    private boolean indexEnabled;
    private String authenticationAgent;
    private String[] tags;

    public NRepositoryConfigBoot() {
    }

    public NRepositoryConfigBoot copy(){
        return clone();
    }

    @Override
    protected NRepositoryConfigBoot clone(){
        try {
            NRepositoryConfigBoot o = (NRepositoryConfigBoot) super.clone();
            if(o.location!=null){
                o.location=o.location.copy();
            }
            if(o.storeLocations!=null) {
                o.storeLocations = new LinkedHashMap<>(storeLocations);
            }
            if(o.env!=null) {
                o.env=new LinkedHashMap<>(o.env);
            }
            if(o.mirrors!=null) {
                o.mirrors=o.mirrors.stream().map(NRepositoryRefBoot::copy).collect(Collectors.toList());
            }
            if(o.users!=null) {
                o.users=o.users.stream().map(NUserConfigBoot::copy).collect(Collectors.toList());
            }
            if(o.tags!=null) {
                o.tags=Arrays.copyOf(tags,tags.length);
            }
            return o;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public String[] getTags() {
        return tags;
    }

    public NRepositoryConfigBoot setTags(String[] tags) {
        this.tags = tags;
        return this;
    }

    public String getName() {
        return name;
    }

    public NRepositoryConfigBoot setName(String name) {
        this.name = name;
        return this;
    }

    public String getUuid() {
        return uuid;
    }

    public NRepositoryConfigBoot setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

//    public String getType() {
//        return type;
//    }
//
//    public NutsRepositoryConfig setType(String type) {
//        this.type = type;
//        return this;
//    }

    public NRepositoryLocationBoot getLocation() {
        return location;
    }

    public NRepositoryConfigBoot setLocation(NRepositoryLocationBoot location) {
        this.location = location;
        return this;
    }

    public String getStoreStrategy() {
        return storeStrategy;
    }

    public NRepositoryConfigBoot setStoreStrategy(String storeStrategy) {
        this.storeStrategy = storeStrategy;
        return this;
    }

    public String getGroups() {
        return groups;
    }

    public NRepositoryConfigBoot setGroups(String groups) {
        this.groups = groups;
        return this;
    }

    public Map<String, String> getEnv() {
        return env;
    }

    public NRepositoryConfigBoot setEnv(Map<String, String> env) {
        this.env = env;
        return this;
    }

    public List<NRepositoryRefBoot> getMirrors() {
        return mirrors;
    }

    public NRepositoryConfigBoot setMirrors(List<NRepositoryRefBoot> mirrors) {
        this.mirrors = mirrors;
        return this;
    }

    public List<NUserConfigBoot> getUsers() {
        return users;
    }

    public NRepositoryConfigBoot setUsers(List<NUserConfigBoot> users) {
        this.users = users;
        return this;
    }

    public boolean isIndexEnabled() {
        return indexEnabled;
    }

    public NRepositoryConfigBoot setIndexEnabled(boolean indexEnabled) {
        this.indexEnabled = indexEnabled;
        return this;
    }

    public String getAuthenticationAgent() {
        return authenticationAgent;
    }

    public NRepositoryConfigBoot setAuthenticationAgent(String authenticationAgent) {
        this.authenticationAgent = authenticationAgent;
        return this;
    }

    public Map<String, String> getStoreLocations() {
        return storeLocations;
    }

    public NRepositoryConfigBoot setStoreLocations(Map<String, String> storeLocations) {
        this.storeLocations = storeLocations;
        return this;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 53 * hash + Objects.hashCode(this.uuid);
        hash = 53 * hash + Objects.hashCode(this.name);
//        hash = 53 * hash + Objects.hashCode(this.type);
        hash = 53 * hash + Objects.hashCode(this.location);
        hash = 53 * hash + Objects.hashCode(this.storeLocations);
        hash = 53 * hash + Objects.hashCode(this.storeStrategy);
        hash = 53 * hash + Objects.hashCode(this.groups);
        hash = 53 * hash + Objects.hashCode(this.env);
        hash = 53 * hash + Objects.hashCode(this.mirrors);
        hash = 53 * hash + Objects.hashCode(this.users);
        hash = 53 * hash + (this.indexEnabled ? 1 : 0);
        hash = 53 * hash + Objects.hashCode(this.authenticationAgent);
        hash = 53 * hash + Arrays.hashCode(this.tags);
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
        final NRepositoryConfigBoot other = (NRepositoryConfigBoot) obj;
        if (this.indexEnabled != other.indexEnabled) {
            return false;
        }
        if (!Objects.equals(this.uuid, other.uuid)) {
            return false;
        }
        if (!Objects.equals(this.name, other.name)) {
            return false;
        }
//        if (!Objects.equals(this.type, other.type)) {
//            return false;
//        }
        if (!Objects.equals(this.location, other.location)) {
            return false;
        }
        if (!Objects.equals(this.groups, other.groups)) {
            return false;
        }
        if (!Objects.equals(this.authenticationAgent, other.authenticationAgent)) {
            return false;
        }
        if (!Objects.equals(this.storeLocations, other.storeLocations)) {
            return false;
        }
        if (this.storeStrategy != other.storeStrategy) {
            return false;
        }
        if (!Objects.equals(this.env, other.env)) {
            return false;
        }
        if (!Objects.equals(this.mirrors, other.mirrors)) {
            return false;
        }
        if (!Arrays.equals(this.tags, other.tags)) {
            return false;
        }
        return Objects.equals(this.users, other.users);
    }

    @Override
    public String toString() {
        return "NutsRepositoryConfig{" + ", uuid=" + uuid + ", name=" + name
//                + ", type=" + type
                + ", location=" + location + ", storeLocations=" + (storeLocations == null ? "null" : storeLocations.toString()) + ", storeStrategy=" + storeStrategy + ", groups=" + groups + ", env=" + env + ", mirrors=" + mirrors + ", users="
                + users + ", indexEnabled=" + indexEnabled
                + ", authenticationAgent=" + authenticationAgent
                + ", tags=" + (tags==null?"[]":Arrays.toString(tags))
                + '}';
    }

}
