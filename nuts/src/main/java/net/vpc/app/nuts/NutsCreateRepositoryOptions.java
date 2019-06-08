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

/**
 *
 * @author vpc
 * @since 0.5.4
 */
public class NutsCreateRepositoryOptions {

    private String name;
    private String location;
    private boolean enabled;
    private boolean failSafe;
    private boolean create;
    private boolean proxy;
    private boolean temporary;
    private int deployOrder;
    private NutsRepositoryConfig config;

    public NutsCreateRepositoryOptions() {
        this.enabled = true;
    }

    public NutsCreateRepositoryOptions(NutsCreateRepositoryOptions o) {
        this.name = o.name;
        this.location = o.location;
        this.enabled = o.enabled;
        this.failSafe = o.failSafe;
        this.create = o.create;
        this.config = o.config;
        this.proxy = o.proxy;
        this.temporary = o.temporary;
        this.deployOrder = o.deployOrder;
    }

    public boolean isTemporary() {
        return temporary;
    }

    public NutsCreateRepositoryOptions setTemporary(boolean temporary) {
        this.temporary = temporary;
        return this;
    }

    public String getName() {
        return name;
    }

    public NutsCreateRepositoryOptions setName(String name) {
        this.name = name;
        return this;
    }

    public String getLocation() {
        return location;
    }

    public NutsCreateRepositoryOptions setLocation(String location) {
        this.location = location;
        return this;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public NutsCreateRepositoryOptions setEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    public boolean isFailSafe() {
        return failSafe;
    }

    public NutsCreateRepositoryOptions setFailSafe(boolean failSafe) {
        this.failSafe = failSafe;
        return this;
    }

    public boolean isCreate() {
        return create;
    }

    public NutsCreateRepositoryOptions setCreate(boolean create) {
        this.create = create;
        return this;
    }

    public NutsRepositoryConfig getConfig() {
        return config;
    }

    public NutsCreateRepositoryOptions setConfig(NutsRepositoryConfig config) {
        this.config = config;
        return this;
    }

    public NutsCreateRepositoryOptions copy() {
        return new NutsCreateRepositoryOptions(this);
    }

    public boolean isProxy() {
        return proxy;
    }

    public NutsCreateRepositoryOptions setProxy(boolean proxy) {
        this.proxy = proxy;
        return this;
    }

    public int getDeployOrder() {
        return deployOrder;
    }

    public NutsCreateRepositoryOptions setDeployOrder(int deployPriority) {
        this.deployOrder = deployPriority;
        return this;
    }

}
