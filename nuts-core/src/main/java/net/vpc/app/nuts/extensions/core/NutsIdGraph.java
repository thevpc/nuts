/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages and libraries
 * for runtime execution. Nuts is the ultimate companion for maven (and other
 * build managers) as it helps installing all package dependencies at runtime.
 * Nuts is not tied to java and is a good choice to share shell scripts and
 * other 'things' . Its based on an extensible architecture to help supporting a
 * large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 * <p>
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * <p>
 * You should have received a copy of the GNU General Public License along with
 * this program; if not, write to the Free Software Foundation, Inc., 51
 * Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.core;

import net.vpc.app.nuts.NutsFile;
import net.vpc.app.nuts.NutsId;

import java.util.*;

public class NutsIdGraph {

    private final Map<NutsId, List<NutsId>> allVertices = new LinkedHashMap<>();
    private final Map<NutsId, NutsFile> files = new LinkedHashMap<>();
    private final Map<String, Set<NutsId>> flatVersions = new LinkedHashMap<>();
    private final Set<NutsId> roots = new LinkedHashSet<>();

    public NutsIdGraph() {
    }

    public Map<String, Set<NutsId>> resolveConflicts() {
        Map<String, Set<NutsId>> all = new LinkedHashMap<>();
        for (Map.Entry<String, Set<NutsId>> v : flatVersions.entrySet()) {
            if (v.getValue().size() > 1) {
                all.put(v.getKey(), new HashSet<>(v.getValue()));
            }
        }
        return all;
    }

    public void remove(NutsId id) {
        files.remove(id);
        allVertices.remove(id);
        Set<NutsId> old = flatVersions.get(id.getFullName());
        if (old != null) {
            old.remove(id);
            if (old.isEmpty()) {
                flatVersions.remove(id.getFullName());
            }
        }
        //now remove all vertex to this id
        List<NutsId> fromToToRemove = new ArrayList<>();
        for (Map.Entry<NutsId, List<NutsId>> v : allVertices.entrySet()) {
            for (NutsId to : v.getValue()) {
                if (id.equals(to)) {
                    fromToToRemove.add(v.getKey());
                }
            }
        }
        for (NutsId nutsId : fromToToRemove) {
            List<NutsId> list = allVertices.get(nutsId);
            if (list != null) {
                list.remove(id);
                if (list.isEmpty()) {
                    allVertices.remove(nutsId);
                }
            }
        }
    }

    public void addRoot(NutsId id) {
        roots.add(id);
    }

    public boolean contains(NutsId id) {
        return allVertices.containsKey(id);
    }

    public NutsFile getNutsFile(NutsId id) {
        return files.get(id);
    }

    public void set(NutsFile from) {
        NutsFile old = files.get(from.getId());
        if (old == null) {
            files.put(from.getId(), from);
        }
    }

    public void add(NutsFile from, NutsFile to) {
        set(from);
        set(to);
        List<NutsId> vertices = allVertices.get(from.getId());
        if (vertices == null) {
            vertices = new ArrayList<>();
            allVertices.put(from.getId(), vertices);
        }
        vertices.add(to.getId());

        Set<NutsId> versions = flatVersions.get(from.getId().getFullName());
        if (versions == null) {
            versions = new HashSet<>();
        }
        versions.add(from.getId());
        flatVersions.put(from.getId().getFullName(), versions);
    }

    public void visit(NutsId id, List<NutsFile> collected) {
        Set<NutsId> visited = new HashSet<>();
        Stack<NutsId> stack = new Stack<>();
        stack.push(id);
        visited.add(id);
        while (!stack.isEmpty()) {
            NutsId i = stack.pop();
            if (i != null) {
                NutsFile f = getNutsFile(i);
                if (f != null) {
                    collected.add(f);
                    List<NutsId> next = allVertices.get(i);
                    if (next != null) {
                        for (NutsId j : next) {
                            if (!visited.contains(j)) {
                                visited.add(j);
                                stack.push(j);
                            }
                        }
                    }
                }
            }
        }
    }
}
