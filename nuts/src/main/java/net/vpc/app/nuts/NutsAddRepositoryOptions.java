/**
 * ====================================================================
 *            Nuts : Network Updatable Things Service
 *                  (universal package manager)
 *
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 *
 * Copyright (C) 2016-2017 Taha BEN SALAH
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts;

import java.io.Serializable;
import java.util.Objects;

/**
 * repository creation options
 * @author vpc
 * @since 0.5.4
 */
public class NutsAddRepositoryOptions implements Serializable {
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
    private boolean enabled;

    /**
     * fail safe repository. when fail safe, repository will be ignored
     * if the location is not accessible
     */
    private boolean failSafe;

    /**
     * always create. Throw exception if found
     */
    private boolean create;

    /**
     * create a proxy for the created repository
     */
    private boolean proxy;

    /**
     * temporary repository
     */
    private boolean temporary;

    /**
     * repository deploy order
     */
    private int deployOrder;

    /**
     * current session
     */
    private NutsSession session;

    /**
     * repository config information
     */
    private NutsRepositoryConfig config;

    /**
     * default constructor
     */
    public NutsAddRepositoryOptions() {
        this.enabled = true;
    }

    /**
     * copy constructor
     * @param other other
     */
    public NutsAddRepositoryOptions(NutsAddRepositoryOptions other) {
        this.name = other.name;
        this.location = other.location;
        this.enabled = other.enabled;
        this.failSafe = other.failSafe;
        this.create = other.create;
        this.config = other.config;
        this.proxy = other.proxy;
        this.temporary = other.temporary;
        this.deployOrder = other.deployOrder;
        this.session = other.session;
    }

    /**
     * current session
     * @return current session
     */
    public NutsSession getSession() {
        return session;
    }

    /**
     * current session
     * @param value new value
     * @return {@code this} instance
     */
    public NutsAddRepositoryOptions setSession(NutsSession value) {
        this.session = value;
        return this;
    }

    /**
     * temporary repository
     * @return temporary repository
     */
    public boolean isTemporary() {
        return temporary;
    }

    /**
     * temporary repository
     * @param value new value
     * @return {@code this} instance
     */
    public NutsAddRepositoryOptions setTemporary(boolean value) {
        this.temporary = value;
        return this;
    }

    /**
     * repository name (should no include special space or characters)
     * @return repository name (should no include special space or characters)
     */
    public String getName() {
        return name;
    }

    /**
     * repository name (should no include special space or characters)
     * @param value new value
     * @return {@code this} instance
     */
    public NutsAddRepositoryOptions setName(String value) {
        this.name = value;
        return this;
    }

    /**
     * repository location
     * @return repository location
     */
    public String getLocation() {
        return location;
    }

    /**
     * repository location
     * @param value new value
     * @return {@code this} instance
     */
    public NutsAddRepositoryOptions setLocation(String value) {
        this.location = value;
        return this;
    }

    /**
     * enabled repository
     * @return enabled repository
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * enabled repository
     * @param value new value
     * @return {@code this} instance
     */
    public NutsAddRepositoryOptions setEnabled(boolean value) {
        this.enabled = value;
        return this;
    }

    /**
     * fail safe repository. when fail safe, repository will be ignored
     * if the location is not accessible
     * @return fail safe repository
     */
    public boolean isFailSafe() {
        return failSafe;
    }

    /**
     * fail safe repository. when fail safe, repository will be ignored
     * if the location is not accessible
     * @param value new value
     * @return {@code this} instance
     */
    public NutsAddRepositoryOptions setFailSafe(boolean value) {
        this.failSafe = value;
        return this;
    }

    /**
     * always create.
     * @return always create
     */
    public boolean isCreate() {
        return create;
    }

    /**
     * always create. Throw exception if found
     * @param value new value
     * @return {@code this} instance
     */
    public NutsAddRepositoryOptions setCreate(boolean value) {
        this.create = value;
        return this;
    }

    /**
     * repository config information
     * @return repository config information
     */
    public NutsRepositoryConfig getConfig() {
        return config;
    }

    /**
     * repository config information
     * @param value new value
     * @return {@code this} instance
     */
    public NutsAddRepositoryOptions setConfig(NutsRepositoryConfig value) {
        this.config = value;
        return this;
    }

    /**
     * is create a proxy for the created repository
     * @return is create a proxy for the created repository
     */
    public boolean isProxy() {
        return proxy;
    }

    /**
     * create a proxy for the created repository
     * @param value new value
     * @return {@code this} instance
     */
    public NutsAddRepositoryOptions setProxy(boolean value) {
        this.proxy = value;
        return this;
    }

    /**
     * repository deploy order
     * @return repository deploy order
     */
    public int getDeployOrder() {
        return deployOrder;
    }

    /**
     * repository deploy order
     * @param value new value
     * @return {@code this} instance
     */
    public NutsAddRepositoryOptions setDeployOrder(int value) {
        this.deployOrder = value;
        return this;
    }

    /**
     * create a copy of this instance
     * @return a copy of this instance
     */
    public NutsAddRepositoryOptions copy() {
        return new NutsAddRepositoryOptions(this);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NutsAddRepositoryOptions that = (NutsAddRepositoryOptions) o;
        return enabled == that.enabled &&
                failSafe == that.failSafe &&
                create == that.create &&
                proxy == that.proxy &&
                temporary == that.temporary &&
                deployOrder == that.deployOrder &&
                Objects.equals(name, that.name) &&
                Objects.equals(location, that.location) &&
                Objects.equals(session, that.session) &&
                Objects.equals(config, that.config);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, location, enabled, failSafe, create, proxy, temporary, deployOrder, session, config);
    }

    @Override
    public String toString() {
        return "NutsAddRepositoryOptions{" +
                "name='" + name + '\'' +
                ", location='" + location + '\'' +
                ", enabled=" + enabled +
                ", failSafe=" + failSafe +
                ", create=" + create +
                ", proxy=" + proxy +
                ", temporary=" + temporary +
                ", deployOrder=" + deployOrder +
                ", session=" + session +
                ", config=" + config +
                '}';
    }
}
