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
 * Copyright [2020] [thevpc] Licensed under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0 Unless required by applicable law
 * or agreed to in writing, software distributed under the License is
 * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 * <br> ====================================================================
 */
package net.thevpc.nuts;

import java.io.Serializable;
import java.util.Objects;

/**
 * repository creation options
 *
 * @author thevpc
 * @since 0.5.4
 * @app.category Config
 */
public class NutsAddRepositoryOptions implements Serializable {

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
    private static final long serialVersionUID = 1;

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
     * fail safe repository. when fail safe, repository will be ignored if the
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
    private NutsRepositoryConfig config;

    /**
     * repository model used for creating the repository
     */
    private NutsRepositoryModel repositoryModel;

    /**
     * repository processing order, use one from {@code ORDER_USER_LOCAL,ORDER_USER_REMOTE,ORDER_SYSTEM_LOCAL}
     */
    private int order;

    /**
     * default constructor
     */
    public NutsAddRepositoryOptions() {
    }

    /**
     * copy constructor
     *
     * @param other other
     */
    public NutsAddRepositoryOptions(NutsAddRepositoryOptions other) {
        this.name = other.name;
        this.location = other.location;
        this.enabled = other.enabled;
        this.failSafe = other.failSafe;
        this.create = other.create;
        this.config = other.config;
        this.temporary = other.temporary;
        this.deployWeight = other.deployWeight;
        this.order = other.order;
        this.repositoryModel = other.repositoryModel;
    }

    /**
     * repository model
     * @return repository model
     */
    public NutsRepositoryModel getRepositoryModel() {
        return repositoryModel;
    }

    /**
     * set repository model
     * @param repositoryModel repository model
     * @return {@code this instance}
     */
    public NutsAddRepositoryOptions setRepositoryModel(NutsRepositoryModel repositoryModel) {
        this.repositoryModel = repositoryModel;
        return this;
    }

    /**
     * repository processing order. Lower values ensure processing (using, searching,...)
     * repositories before others.
     * @return order
     */
    public int getOrder() {
        return order;
    }

    /**
     * set repository order number
     * @param order order
     * @return {@code this instance}
     */
    public NutsAddRepositoryOptions setOrder(int order) {
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
    public NutsAddRepositoryOptions setTemporary(boolean value) {
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
    public NutsAddRepositoryOptions setName(String value) {
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
    public NutsAddRepositoryOptions setLocation(String value) {
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
    public NutsAddRepositoryOptions setEnabled(boolean value) {
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
    public NutsAddRepositoryOptions setFailSafe(boolean value) {
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
    public NutsAddRepositoryOptions setCreate(boolean value) {
        this.create = value;
        return this;
    }

    /**
     * repository config information
     *
     * @return repository config information
     */
    public NutsRepositoryConfig getConfig() {
        return config;
    }

    /**
     * repository config information
     *
     * @param value new value
     * @return {@code this} instance
     */
    public NutsAddRepositoryOptions setConfig(NutsRepositoryConfig value) {
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
    public NutsAddRepositoryOptions setDeployWeight(int value) {
        this.deployWeight = value;
        return this;
    }

    /**
     * create a copy of this instance
     *
     * @return a copy of this instance
     */
    public NutsAddRepositoryOptions copy() {
        return new NutsAddRepositoryOptions(this);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, location, enabled, failSafe, create,
                temporary, order, deployWeight, config);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        NutsAddRepositoryOptions that = (NutsAddRepositoryOptions) o;
        return enabled == that.enabled
                && failSafe == that.failSafe
                && create == that.create
                && temporary == that.temporary
                && order == that.order
                && deployWeight == that.deployWeight
                && Objects.equals(name, that.name)
                && Objects.equals(location, that.location)
                && Objects.equals(config, that.config);
    }

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
