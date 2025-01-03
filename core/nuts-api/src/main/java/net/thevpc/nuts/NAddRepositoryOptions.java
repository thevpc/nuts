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
package net.thevpc.nuts;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Objects;

/**
 * repository creation options
 *
 * @author thevpc
 * @app.category Config
 * @since 0.5.4
 */
public class NAddRepositoryOptions implements Serializable, Cloneable {

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
     * repository config information
     */
    private NRepositoryConfig config;

    /**
     * repository model used for creating the repository
     */
    private NRepositoryModel repositoryModel;

    /**
     * repository processing order, use one from {@code ORDER_USER_LOCAL,ORDER_USER_REMOTE,ORDER_SYSTEM_LOCAL}
     */
    private int order;

    /**
     * default constructor
     */
    public NAddRepositoryOptions() {
    }

    /**
     * copy constructor
     *
     * @param other other
     */
    public NAddRepositoryOptions(NAddRepositoryOptions other) {
        this.name = other.name;
        this.location = other.location;
        this.enabled = other.enabled;
        this.failSafe = other.failSafe;
        this.create = other.create;
        this.temporary = other.temporary;
        this.deployWeight = other.deployWeight;
        this.order = other.order;
        this.config = other.config == null ? null : other.config.copy();
        this.repositoryModel = other.repositoryModel == null ? null : other.repositoryModel/*.copy()*/;
    }

    /**
     * Creates and returns a copy of this object.
     *
     * @return a new instance of {@code NAddRepositoryOptions} that is a copy of this object
     */
    public NAddRepositoryOptions copy() {
        return clone();
    }

    /**
     * Creates and returns a copy of this {@code NAddRepositoryOptions} object.
     *
     * @return a new instance of {@code NAddRepositoryOptions} that is a copy of this object
     * @throws RuntimeException if cloning is not supported
     */
    @Override
    protected NAddRepositoryOptions clone() {
        try {
            NAddRepositoryOptions o = (NAddRepositoryOptions) super.clone();
            o.config = o.config == null ? null : o.config.copy();
            o.repositoryModel = o.repositoryModel == null ? null : o.repositoryModel/*.copy()*/;
            return o;
        } catch (CloneNotSupportedException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * repository model
     *
     * @return repository model
     */
    public NRepositoryModel getRepositoryModel() {
        return repositoryModel;
    }

    /**
     * set repository model
     *
     * @param repositoryModel repository model
     * @return {@code this instance}
     */
    public NAddRepositoryOptions setRepositoryModel(NRepositoryModel repositoryModel) {
        this.repositoryModel = repositoryModel;
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
    public NAddRepositoryOptions setOrder(int order) {
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
    public NAddRepositoryOptions setTemporary(boolean value) {
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
    public NAddRepositoryOptions setName(String value) {
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
    public NAddRepositoryOptions setLocation(String value) {
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
    public NAddRepositoryOptions setEnabled(boolean value) {
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
    public NAddRepositoryOptions setFailSafe(boolean value) {
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
    public NAddRepositoryOptions setCreate(boolean value) {
        this.create = value;
        return this;
    }

    /**
     * repository config information
     *
     * @return repository config information
     */
    public NRepositoryConfig getConfig() {
        return config;
    }

    /**
     * repository config information
     *
     * @param value new value
     * @return {@code this} instance
     */
    public NAddRepositoryOptions setConfig(NRepositoryConfig value) {
        this.config = value;
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
    public NAddRepositoryOptions setDeployWeight(int value) {
        this.deployWeight = value;
        return this;
    }

    /**
     * Computes the hash code for the object based on its fields.
     * This method uses the {@link Objects#hash} utility to generate
     * a hash code considering the fields: name, location, enabled,
     * failSafe, create, temporary, order, deployWeight, and config.
     *
     * @return an integer representing the hash code of this object
     */
    @Override
    public int hashCode() {
        return Objects.hash(name, location, enabled, failSafe, create,
                temporary, order, deployWeight, config);
    }

    /**
     * Compares this object to the specified object for equality. The comparison
     * is based on each field of the class including name, location, enabled state,
     * fail safe, creation flag, temporary status, order, deploy weight, and configuration.
     *
     * @param o the object to be compared for equality with this object
     * @return {@code true} if the specified object is equal to this instance, otherwise {@code false}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NAddRepositoryOptions that = (NAddRepositoryOptions) o;
        return enabled == that.enabled
                && failSafe == that.failSafe
                && create == that.create
                && temporary == that.temporary
                && order == that.order
                && deployWeight == that.deployWeight
                && Objects.equals(name, that.name)
                && Objects.equals(location, that.location)
                && Objects.equals(config, that.config)
                ;
    }

    /**
     * Returns a string representation of the object. The string includes
     * the values of the primary fields such as name, location, enabled, failSafe,
     * create, temporary, deployOrder, order, and config. This representation
     * is mainly for debugging purposes and provides an overview of the object's state.
     *
     * @return a string representation of the {@code NAddRepositoryOptions} object
     */
    @Override
    public String toString() {
        return "NutsAddRepositoryOptions{"
                + "name='" + name + '\''
                + ", location='" + location + '\''
                + ", enabled=" + enabled
                + ", failSafe=" + failSafe
                + ", create=" + create
                + ", temporary=" + temporary
                + ", deployOrder=" + deployWeight
                + ", order=" + order
                + ", config=" + config
                + '}';
    }
}
