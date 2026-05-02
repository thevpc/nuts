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
 *
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
package net.thevpc.nuts.core;

import net.thevpc.nuts.platform.NStoreType;
import net.thevpc.nuts.spi.NRepositoryLocation;
import net.thevpc.nuts.util.NToStringBuilder;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * repository creation options
 *
 * @author thevpc
 * @app.category Config
 * @since 0.5.4
 */
public class NRepositorySpec implements Serializable, Cloneable {

    /**
     * Unique identifier for the serialization of the NAddRepositoryOptions class.
     * This value is used to ensure that a deserialized object matches the version
     * of the class definition in the code. If there is a mismatch,
     * an InvalidClassException will be thrown during deserialization.
     */
    private static final long serialVersionUID = 1;

    /**
     * Repository Order for local repositories, used for prioritising local access
     */
    public static final int ORDER_USER_LOCAL = 1000;

    /**
     * Repository Order for local system repositories, used for prioritising local access
     */
    public static final int ORDER_SYSTEM_LOCAL = 2000;

    /**
     * Repository Order for remote repositories, used for prioritising local access
     */
    public static final int ORDER_USER_REMOTE = 10000;

    /**
     * repository name (should no include special space or characters)
     */
    private String name;

    /**
     * repository location
     */
    private String location;

    /**
     * enabled repository
     */
    private boolean enabled = true;

    /**
     * fail-safe repository. when fail-safe, repository will be ignored if the
     * location is not accessible
     */
    private boolean failSafe;

    /**
     * always create. Throw exception if found
     */
    private boolean create;

    /**
     * temporary repository
     */
    private boolean temporary;

    /**
     * repository deploy order
     */
    private int deployWeight;


    /**
     * repository processing order, use one from {@code ORDER_USER_LOCAL,ORDER_USER_REMOTE,ORDER_SYSTEM_LOCAL}
     */
    private int order;



    //////////////////
    private String uuid;
    private NRepositoryLocation sourceLocation;
    /**
     * repository model used for creating the repository
     */
    private NRepositoryModel sourceModel;

    private Map<NStoreType, String> storeLocations = null;
    private NStoreStrategy storeStrategy = null;
    private String groups;
    private Map<String, String> env;
    private List<NRepositoryRef> mirrors;
    private boolean indexEnabled;
    private String authenticationAgent;
    private String[] tags;
    private String[] aliases;
    /////////////////
    /**
     * default constructor
     */
    public NRepositorySpec() {
    }

    /**
     * copy constructor
     *
     * @param other other
     */
    public NRepositorySpec(NRepositorySpec other) {
        this.name = other.name;
        this.location = other.location;
        this.enabled = other.enabled;
        this.failSafe = other.failSafe;
        this.create = other.create;
        this.temporary = other.temporary;
        this.deployWeight = other.deployWeight;
        this.order = other.order;
        this.sourceModel = other.sourceModel == null ? null : other.sourceModel/*.copy()*/;
        this.uuid= other.name;
        this.sourceLocation = other.sourceLocation ==null?null:other.sourceLocation.copy();
        this.storeLocations = other.storeLocations==null?null:new HashMap<>(other.storeLocations);
        this.storeStrategy = other.storeStrategy;
        this.groups= other.groups;
        this.env= other.env==null?null:new HashMap<>(other.env);
        this.mirrors= other.mirrors==null?null:new ArrayList<>(other.mirrors.stream().map(x->x.copy()).collect(Collectors.toList()));
        this.indexEnabled= other.indexEnabled;
        this.authenticationAgent= other.authenticationAgent;
        this.aliases= other.aliases==null?null:Arrays.copyOf(other.aliases, other.aliases.length);
    }



    /**
     * Creates and returns a copy of this object.
     *
     * @return a new instance of {@code NAddRepositoryOptions} that is a copy of this object
     */
    public NRepositorySpec copy() {
        return clone();
    }

    /**
     * Creates and returns a copy of this {@code NAddRepositoryOptions} object.
     *
     * @return a new instance of {@code NAddRepositoryOptions} that is a copy of this object
     * @throws RuntimeException if cloning is not supported
     */
    @Override
    protected NRepositorySpec clone() {
        try {
            NRepositorySpec o = (NRepositorySpec) super.clone();
            o.sourceModel = o.sourceModel == null ? null : o.sourceModel/*.copy()*/;

            if(o.sourceLocation !=null){
                o.sourceLocation =o.sourceLocation.copy();
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
            if(o.aliases!=null) {
                o.aliases=Arrays.copyOf(aliases,aliases.length);
            }
            return o;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    public String[] getAliases() {
        return aliases;
    }

    public NRepositorySpec setAliases(String... aliases) {
        this.aliases = aliases;
        return this;
    }

    /**
     * repository model
     *
     * @return repository model
     */
    public NRepositoryModel getSourceModel() {
        return sourceModel;
    }

    /**
     * set repository model
     *
     * @param sourceModel repository model
     * @return {@code this instance}
     */
    public NRepositorySpec setSourceModel(NRepositoryModel sourceModel) {
        this.sourceModel = sourceModel;
        return this;
    }

    /**
     * repository processing order. Lower values ensure processing (using, searching,...)
     * repositories before others.
     *
     * @return order
     */
    public int getOrder() {
        return order;
    }

    /**
     * set repository order number
     *
     * @param order order
     * @return {@code this instance}
     */
    public NRepositorySpec setOrder(int order) {
        this.order = order;
        return this;
    }

    /**
     * temporary repository
     *
     * @return temporary repository
     */
    public boolean isTemporary() {
        return temporary;
    }

    /**
     * temporary repository
     *
     * @param value new value
     * @return {@code this} instance
     */
    public NRepositorySpec setTemporary(boolean value) {
        this.temporary = value;
        return this;
    }

    /**
     * repository name (should no include special space or characters)
     *
     * @return repository name (should no include special space or characters)
     */
    public String getName() {
        return name;
    }

    /**
     * repository name (should no include special space or characters)
     *
     * @param value new value
     * @return {@code this} instance
     */
    public NRepositorySpec setName(String value) {
        this.name = value;
        return this;
    }

    /**
     * repository location
     *
     * @return repository location
     */
    public String getLocation() {
        return location;
    }

    /**
     * repository location
     *
     * @param value new value
     * @return {@code this} instance
     */
    public NRepositorySpec setLocation(String value) {
        this.location = value;
        return this;
    }

    /**
     * enabled repository
     *
     * @return enabled repository
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * enabled repository
     *
     * @param value new value
     * @return {@code this} instance
     */
    public NRepositorySpec setEnabled(boolean value) {
        this.enabled = value;
        return this;
    }

    /**
     * fail safe repository. when fail safe, repository will be ignored if the
     * location is not accessible
     *
     * @return fail safe repository
     */
    public boolean isFailSafe() {
        return failSafe;
    }

    /**
     * fail safe repository. when fail safe, repository will be ignored if the
     * location is not accessible
     *
     * @param value new value
     * @return {@code this} instance
     */
    public NRepositorySpec setFailSafe(boolean value) {
        this.failSafe = value;
        return this;
    }

    /**
     * always create.
     *
     * @return always create
     */
    public boolean isCreate() {
        return create;
    }

    /**
     * always create. Throw exception if found
     *
     * @param value new value
     * @return {@code this} instance
     */
    public NRepositorySpec setCreate(boolean value) {
        this.create = value;
        return this;
    }

    /**
     * repository deploy order
     *
     * @return repository deploy order
     */
    public int getDeployWeight() {
        return deployWeight;
    }

    /**
     * repository deploy order
     *
     * @param value new value
     * @return {@code this} instance
     */
    public NRepositorySpec setDeployWeight(int value) {
        this.deployWeight = value;
        return this;
    }

    public String getUuid() {
        return uuid;
    }

    public NRepositorySpec mergeConfig(NRepositoryConfig config) {
        if (config == null) return this;
        if (config.getUuid() != null) {
            this.setUuid(config.getUuid());
        }
        if (config.getLocation()!=null) {
            this.setSourceLocation(config.getLocation().copy());
        }
        if (config.getStoreStrategy()!=null) {
            this.setStoreStrategy(config.getStoreStrategy());
        }
        if (config.getGroups()!=null) {
            this.setGroups(config.getGroups());
        }
        if (config.getEnv()!=null) {
            if(getEnv()==null){
                setEnv(new HashMap<>(config.getEnv()));
            }else{
                getEnv().putAll(config.getEnv());
            }
        }
        if (config.getTags()!=null) {
            if(getTags()==null){
                setTags(new LinkedHashSet<>(Arrays.asList(config.getTags())).toArray(new String[0]));
            }else{
                LinkedHashSet<String> a = new LinkedHashSet<>(Arrays.asList(this.getTags()));
                a.addAll(Arrays.asList(config.getTags()));
                setTags(a.toArray(new String[0]));
            }
        }
        if (config.getStoreLocations()!=null) {
            if(getStoreLocations()==null){
                setStoreLocations(new HashMap<>(config.getStoreLocations()));
            }else{
                getStoreLocations().putAll(config.getStoreLocations());
            }
        }
        if (config.getMirrors()!=null) {
            if(getMirrors()==null){
                setMirrors(new ArrayList<>(config.getMirrors()));
            }else{
                getMirrors().addAll(config.getMirrors());
            }
        }
        this.setMirrors(config.getMirrors() == null ? null : new ArrayList<>(config.getMirrors().stream().map(x -> x.copy()).collect(Collectors.toList())));
        this.setIndexEnabled(config.isIndexEnabled());
        if (config.getAuthenticationAgent() != null) {
            this.setAuthenticationAgent(config.getAuthenticationAgent());
        }
        return this;
    }

    public NRepositoryConfig toConfig() {
        NRepositoryConfig config=new NRepositoryConfig();
        config.setUuid(this.getUuid());
        config.setLocation(this.getSourceLocation()==null?null:this.getSourceLocation().copy());
        config.setStoreStrategy(this.getStoreStrategy());
        config.setGroups(this.getGroups());
        config.setEnv(this.getEnv()==null?null:new HashMap<>(this.getEnv()));
        config.setTags(this.getTags()==null?null:Arrays.copyOf(this.getTags(), this.getTags().length));
        config.setStoreLocations(this.getStoreLocations()==null?null:new HashMap<>(this.getStoreLocations()));
        config.setMirrors(this.getMirrors()==null?null:new ArrayList<>(this.getMirrors().stream().map(x->x.copy()).collect(Collectors.toList())));
        config.setIndexEnabled(this.isIndexEnabled());
        config.setAuthenticationAgent(this.getAuthenticationAgent());
        return config;
    }

    public NRepositorySpec setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public NRepositoryLocation getSourceLocation() {
        return sourceLocation;
    }

    public NRepositorySpec setSourceLocation(NRepositoryLocation sourceLocation) {
        this.sourceLocation = sourceLocation;
        return this;
    }

    public Map<NStoreType, String> getStoreLocations() {
        return storeLocations;
    }

    public NRepositorySpec setStoreLocations(Map<NStoreType, String> storeLocations) {
        this.storeLocations = storeLocations;
        return this;
    }

    public NStoreStrategy getStoreStrategy() {
        return storeStrategy;
    }

    public NRepositorySpec setStoreStrategy(NStoreStrategy storeStrategy) {
        this.storeStrategy = storeStrategy;
        return this;
    }

    public String getGroups() {
        return groups;
    }

    public NRepositorySpec setGroups(String groups) {
        this.groups = groups;
        return this;
    }

    public Map<String, String> getEnv() {
        return env;
    }

    public NRepositorySpec setEnv(Map<String, String> env) {
        this.env = env;
        return this;
    }

    public List<NRepositoryRef> getMirrors() {
        return mirrors;
    }

    public NRepositorySpec setMirrors(List<NRepositoryRef> mirrors) {
        this.mirrors = mirrors;
        return this;
    }

    public boolean isIndexEnabled() {
        return indexEnabled;
    }

    public NRepositorySpec setIndexEnabled(boolean indexEnabled) {
        this.indexEnabled = indexEnabled;
        return this;
    }

    public String getAuthenticationAgent() {
        return authenticationAgent;
    }

    public NRepositorySpec setAuthenticationAgent(String authenticationAgent) {
        this.authenticationAgent = authenticationAgent;
        return this;
    }

    public String[] getTags() {
        return tags;
    }

    public NRepositorySpec setTags(String... tags) {
        this.tags = tags;
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NRepositorySpec that = (NRepositorySpec) o;
        return enabled == that.enabled && failSafe == that.failSafe && create == that.create && temporary == that.temporary && deployWeight == that.deployWeight && order == that.order && indexEnabled == that.indexEnabled && Objects.equals(name, that.name) && Objects.equals(location, that.location) && Objects.equals(uuid, that.uuid) && Objects.equals(sourceLocation, that.sourceLocation) && Objects.equals(sourceModel, that.sourceModel) && Objects.equals(storeLocations, that.storeLocations) && storeStrategy == that.storeStrategy && Objects.equals(groups, that.groups) && Objects.equals(env, that.env) && Objects.equals(mirrors, that.mirrors) && Objects.equals(authenticationAgent, that.authenticationAgent) && Objects.deepEquals(tags, that.tags) && Objects.deepEquals(aliases, that.aliases);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, location, enabled, failSafe, create, temporary, deployWeight, order, uuid, sourceLocation, sourceModel, storeLocations, storeStrategy, groups, env, mirrors, indexEnabled, authenticationAgent, Arrays.hashCode(tags), Arrays.hashCode(aliases));
    }

    @Override
    public String toString() {
        return NToStringBuilder.of("NRepositorySpec")
                .add("name",name)
                .addIfNonBlank("location",location)
                .addIfNonBlank("enabled",enabled)
                .addIfNonBlank("failSafe",failSafe)
                .addIfNonBlank("create",create)
                .addIfNonBlank("temporary",temporary)
                .addIfNonBlank("deployWeight",deployWeight)
                .addIfNonBlank("order",order)
                .addIfNonBlank("uuid",uuid)
                .addIfNonBlank("sourceLocation",sourceLocation)
                .addIfNonBlank("sourceModel",sourceModel)
                .addIfNonBlank("storeLocations",storeLocations)
                .addIfNonBlank("storeStrategy",storeStrategy)
                .addIfNonBlank("groups",groups)
                .addIfNonBlank("env",env)
                .addIfNonBlank("mirrors",mirrors)
                .addIfNonBlank("indexEnabled",indexEnabled)
                .addIfNonBlank("authenticationAgent",authenticationAgent)
                .addIfNonBlank("tags",tags==null?null:Arrays.toString(tags))
                .addIfNonBlank("aliases",aliases==null?null:Arrays.toString(aliases))
                .toString();
    }
}
