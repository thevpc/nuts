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
package net.thevpc.nuts.core;

import net.thevpc.nuts.platform.NStoreType;
import net.thevpc.nuts.spi.NRepositoryLocation;
import net.thevpc.nuts.util.NToStringBuilder;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author thevpc
 * @app.category Config
 * @since 0.5.4
 */
public class NRepositoryConfig extends NConfigItem implements Serializable,Cloneable {

    private static final long serialVersionUID = 1;
    private String uuid;
    private String name;
    private NRepositoryLocation location;
    private Map<NStoreType, String> storeLocations = null;
    private NStoreStrategy storeStrategy = null;
    private String groups;
    private Map<String, String> env;
    private List<NRepositoryRef> mirrors;
    private boolean indexEnabled;
    private String authenticationAgent;
    private String[] tags;

    public NRepositoryConfig() {
    }

    public NRepositoryConfig copy(){
        return clone();
    }

    @Override
    protected NRepositoryConfig clone(){
        try {
            NRepositoryConfig o = (NRepositoryConfig) super.clone();
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
                o.mirrors=o.mirrors.stream().map(NRepositoryRef::copy).collect(Collectors.toList());
            }
            if(o.tags!=null) {
                o.tags=Arrays.copyOf(tags,tags.length);
            }
            return o;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public String[] tags() {
        return tags;
    }

    public NRepositoryConfig tags(String[] tags) {
        this.tags = tags;
        return this;
    }

    public String name() {
        return name;
    }

    public NRepositoryConfig name(String name) {
        this.name = name;
        return this;
    }

    public String uuid() {
        return uuid;
    }

    public NRepositoryConfig uuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public NRepositoryLocation location() {
        return location;
    }

    public NRepositoryConfig location(NRepositoryLocation location) {
        this.location = location;
        return this;
    }

    public NStoreStrategy storeStrategy() {
        return storeStrategy;
    }

    public NRepositoryConfig storeStrategy(NStoreStrategy storeStrategy) {
        this.storeStrategy = storeStrategy;
        return this;
    }

    public String groups() {
        return groups;
    }

    public NRepositoryConfig groups(String groups) {
        this.groups = groups;
        return this;
    }

    public Map<String, String> env() {
        return env;
    }

    public NRepositoryConfig env(Map<String, String> env) {
        this.env = env;
        return this;
    }

    public List<NRepositoryRef> mirrors() {
        return mirrors;
    }

    public NRepositoryConfig mirrors(List<NRepositoryRef> mirrors) {
        this.mirrors = mirrors;
        return this;
    }

    public boolean isIndexEnabled() {
        return indexEnabled;
    }

    public NRepositoryConfig indexEnabled(boolean indexEnabled) {
        this.indexEnabled = indexEnabled;
        return this;
    }

    public String authenticationAgent() {
        return authenticationAgent;
    }

    public NRepositoryConfig authenticationAgent(String authenticationAgent) {
        this.authenticationAgent = authenticationAgent;
        return this;
    }

    public Map<NStoreType, String> storeLocations() {
        return storeLocations;
    }

    public NRepositoryConfig storeLocations(Map<NStoreType, String> storeLocations) {
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
        final NRepositoryConfig other = (NRepositoryConfig) obj;
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
        return true;
    }

    @Override
    public String toString() {
        return new NToStringBuilder("NutsRepositoryConfig")
                .add("name",name)
                .addIfNonBlank("uuid",uuid)
                .addIfNonBlank("location",location)
                .addIfNonBlank("storeLocations",storeLocations)
                .addIfNonBlank("storeStrategy",storeStrategy)
                .addIfNonBlank("groups",groups)
                .addIfNonBlank("env",env)
                .addIfNonBlank("mirrors",mirrors)
                .addIfNonBlank("indexEnabled",indexEnabled)
                .addIfNonBlank("authenticationAgent",authenticationAgent)
                .addIfNonBlank("tags",tags)
                .toString();
    }

}
