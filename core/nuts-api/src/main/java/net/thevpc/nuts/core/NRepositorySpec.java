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
import net.thevpc.nuts.util.NGetter;
import net.thevpc.nuts.util.NSetter;
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

    @NGetter
    public String[] aliases() {
        return aliases;
    }

    @NSetter
    public NRepositorySpec aliases(String... aliases) {
        this.aliases = aliases;
        return this;
    }

    /**
     * repository model
     *
     * @return repository model
     */
    @NGetter
    public NRepositoryModel sourceModel() {
        return sourceModel;
    }

    /**
     * set repository model
     *
     * @param sourceModel repository model
     * @return {@code this instance}
     */
    @NSetter
    public NRepositorySpec sourceModel(NRepositoryModel sourceModel) {
        this.sourceModel = sourceModel;
        return this;
    }

    /**
     * repository processing order. Lower values ensure processing (using, searching,...)
     * repositories before others.
     *
     * @return order
     */
    @NGetter
    public int order() {
        return order;
    }

    /**
     * set repository order number
     *
     * @param order order
     * @return {@code this instance}
     */
    @NSetter
    public NRepositorySpec order(int order) {
        this.order = order;
        return this;
    }

    /**
     * temporary repository
     *
     * @return temporary repository
     */
    @NGetter
    public boolean isTemporary() {
        return temporary;
    }

    /**
     * temporary repository
     *
     * @param value new value
     * @return {@code this} instance
     */
    @NSetter
    public NRepositorySpec temporary(boolean value) {
        this.temporary = value;
        return this;
    }

    /**
     * repository name (should no include special space or characters)
     *
     * @return repository name (should no include special space or characters)
     */
    @NGetter
    public String name() {
        return name;
    }

    /**
     * repository name (should no include special space or characters)
     *
     * @param value new value
     * @return {@code this} instance
     */
    @NSetter
    public NRepositorySpec name(String value) {
        this.name = value;
        return this;
    }

    /**
     * repository location
     *
     * @return repository location
     */
    @NGetter
    public String location() {
        return location;
    }

    /**
     * repository location
     *
     * @param value new value
     * @return {@code this} instance
     */
    @NSetter
    public NRepositorySpec location(String value) {
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
    @NSetter
    public NRepositorySpec enabled(boolean value) {
        this.enabled = value;
        return this;
    }

    /**
     * fail safe repository. when fail safe, repository will be ignored if the
     * location is not accessible
     *
     * @return fail safe repository
     */
    @NGetter
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
    @NSetter
    public NRepositorySpec failSafe(boolean value) {
        this.failSafe = value;
        return this;
    }

    /**
     * always create.
     *
     * @return always create
     */
    @NGetter
    public boolean isCreate() {
        return create;
    }

    /**
     * always create. Throw exception if found
     *
     * @param value new value
     * @return {@code this} instance
     */
    @NSetter
    public NRepositorySpec create(boolean value) {
        this.create = value;
        return this;
    }

    /**
     * repository deploy order
     *
     * @return repository deploy order
     */
    @NGetter
    public int deployWeight() {
        return deployWeight;
    }

    /**
     * repository deploy order
     *
     * @param value new value
     * @return {@code this} instance
     */
    @NSetter
    public NRepositorySpec deployWeight(int value) {
        this.deployWeight = value;
        return this;
    }

    @NGetter
    public String uuid() {
        return uuid;
    }

    public NRepositorySpec mergeConfig(NRepositoryConfig config) {
        if (config == null) return this;
        if (config.getUuid() != null) {
            this.uuid(config.getUuid());
        }
        if (config.getLocation()!=null) {
            this.sourceLocation(config.getLocation().copy());
        }
        if (config.getStoreStrategy()!=null) {
            this.storeStrategy(config.getStoreStrategy());
        }
        if (config.getGroups()!=null) {
            this.groups(config.getGroups());
        }
        if (config.getEnv()!=null) {
            if(env()==null){
                env(new HashMap<>(config.getEnv()));
            }else{
                env().putAll(config.getEnv());
            }
        }
        if (config.getTags()!=null) {
            if(tags()==null){
                tags(new LinkedHashSet<>(Arrays.asList(config.getTags())).toArray(new String[0]));
            }else{
                LinkedHashSet<String> a = new LinkedHashSet<>(Arrays.asList(this.tags()));
                a.addAll(Arrays.asList(config.getTags()));
                tags(a.toArray(new String[0]));
            }
        }
        if (config.getStoreLocations()!=null) {
            if(storeLocations()==null){
                storeLocations(new HashMap<>(config.getStoreLocations()));
            }else{
                storeLocations().putAll(config.getStoreLocations());
            }
        }
        if (config.getMirrors()!=null) {
            if(mirrors()==null){
                mirrors(new ArrayList<>(config.getMirrors()));
            }else{
                mirrors().addAll(config.getMirrors());
            }
        }
        this.mirrors(config.getMirrors() == null ? null : new ArrayList<>(config.getMirrors().stream().map(x -> x.copy()).collect(Collectors.toList())));
        this.indexEnabled(config.isIndexEnabled());
        if (config.getAuthenticationAgent() != null) {
            this.authenticationAgent(config.getAuthenticationAgent());
        }
        return this;
    }

    public NRepositoryConfig toConfig() {
        NRepositoryConfig config=new NRepositoryConfig();
        config.setUuid(this.uuid());
        config.setLocation(this.sourceLocation()==null?null:this.sourceLocation().copy());
        config.setStoreStrategy(this.storeStrategy());
        config.setGroups(this.groups());
        config.setEnv(this.env()==null?null:new HashMap<>(this.env()));
        config.setTags(this.tags()==null?null:Arrays.copyOf(this.tags(), this.tags().length));
        config.setStoreLocations(this.storeLocations()==null?null:new HashMap<>(this.storeLocations()));
        config.setMirrors(this.mirrors()==null?null:new ArrayList<>(this.mirrors().stream().map(x->x.copy()).collect(Collectors.toList())));
        config.setIndexEnabled(this.isIndexEnabled());
        config.setAuthenticationAgent(this.authenticationAgent());
        return config;
    }

    @NSetter
    public NRepositorySpec uuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public NRepositoryLocation sourceLocation() {
        return sourceLocation;
    }

    @NSetter
    public NRepositorySpec sourceLocation(NRepositoryLocation sourceLocation) {
        this.sourceLocation = sourceLocation;
        return this;
    }

    public Map<NStoreType, String> storeLocations() {
        return storeLocations;
    }

    @NSetter
    public NRepositorySpec storeLocations(Map<NStoreType, String> storeLocations) {
        this.storeLocations = storeLocations;
        return this;
    }

    public NStoreStrategy storeStrategy() {
        return storeStrategy;
    }

    @NSetter
    public NRepositorySpec storeStrategy(NStoreStrategy storeStrategy) {
        this.storeStrategy = storeStrategy;
        return this;
    }

    public String groups() {
        return groups;
    }

    @NSetter
    public NRepositorySpec groups(String groups) {
        this.groups = groups;
        return this;
    }

    public Map<String, String> env() {
        return env;
    }

    @NSetter
    public NRepositorySpec env(Map<String, String> env) {
        this.env = env;
        return this;
    }

    public List<NRepositoryRef> mirrors() {
        return mirrors;
    }

    @NSetter
    public NRepositorySpec mirrors(List<NRepositoryRef> mirrors) {
        this.mirrors = mirrors;
        return this;
    }

    public boolean isIndexEnabled() {
        return indexEnabled;
    }

    @NSetter
    public NRepositorySpec indexEnabled(boolean indexEnabled) {
        this.indexEnabled = indexEnabled;
        return this;
    }

    public String authenticationAgent() {
        return authenticationAgent;
    }

    @NSetter
    public NRepositorySpec authenticationAgent(String authenticationAgent) {
        this.authenticationAgent = authenticationAgent;
        return this;
    }

    @NGetter
    public String[] tags() {
        return tags;
    }

    @NSetter
    public NRepositorySpec tags(String... tags) {
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
