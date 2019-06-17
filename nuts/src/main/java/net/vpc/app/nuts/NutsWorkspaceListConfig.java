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
import java.util.ArrayList;
import java.util.List;

/**
 * Class for managing a Workspace list
 *
 * @author Nasreddine Bac Ali
 * @date 2019-03-02
 * @since 0.5.4
 */
public class NutsWorkspaceListConfig implements Serializable {

    private static final long serialVersionUID = 2;
    private String uuid;
    private String name;
    private List<NutsWorkspaceLocation> workspaces;

    public NutsWorkspaceListConfig() {
    }

    public NutsWorkspaceListConfig(String uuid, String name) {
        this.uuid = uuid;
        this.name = name;
    }

    public NutsWorkspaceListConfig(NutsWorkspaceListConfig other) {
        this.uuid = other.getUuid();
        this.name = other.getName();
        this.workspaces = other.getWorkspaces() == null ? null : new ArrayList<>(other.getWorkspaces());
    }

    public String getUuid() {
        return uuid;
    }

    public NutsWorkspaceListConfig setUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }

    public String getName() {
        return name;
    }

    public NutsWorkspaceListConfig setName(String name) {
        this.name = name;
        return this;
    }

    public List<NutsWorkspaceLocation> getWorkspaces() {
        return workspaces;
    }

    public void setWorkspaces(List<NutsWorkspaceLocation> workspaces) {
        this.workspaces = workspaces;
    }

    @Override
    public String toString() {
        return "NutsWorkspaceListConfig{" + "uuid=" + uuid + ", name=" + name + ", workspaces=" + workspaces + '}';
    }
}
