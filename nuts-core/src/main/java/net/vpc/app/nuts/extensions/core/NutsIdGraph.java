/**
 * ====================================================================
 * Nuts : Network Updatable Things Service
 * (universal package manager)
 * <p>
 * is a new Open Source Package Manager to help install packages
 * and libraries for runtime execution. Nuts is the ultimate companion for
 * maven (and other build managers) as it helps installing all package
 * dependencies at runtime. Nuts is not tied to java and is a good choice
 * to share shell scripts and other 'things' . Its based on an extensible
 * architecture to help supporting a large range of sub managers / repositories.
 * <p>
 * Copyright (C) 2016-2017 Taha BEN SALAH
 * <p>
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * <p>
 * You should have received a copy of the GNU General Public License along
 * with this program; if not, write to the Free Software Foundation, Inc.,
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA.
 * ====================================================================
 */
package net.vpc.app.nuts.extensions.core;

import net.vpc.app.nuts.NutsId;
import net.vpc.app.nuts.extensions.util.CoreNutsUtils;

import java.util.*;

public class NutsIdGraph {

    private final Map<NutsId, NutsList> allVertices = new LinkedHashMap<>();
    private final Map<NutsId, NutsId> ids = new LinkedHashMap<>();
    private final Map<String, Set<NutsId>> flatVersions = new LinkedHashMap<>();
    private final Set<NutsId> roots = new LinkedHashSet<>();

    public NutsIdGraph() {
    }

    public int compareIds(NutsId id1, NutsId id2) {
        if (id1 == null && id2 == null) {
            return 0;
        }
        if (id1 == null) {
            return -1;
        }
        if (id2 == null) {
            return 1;
        }
        int x = id1.getVersion().compareTo(id2.getVersion());
        if (x != 0) {
            return x;
        }
        return CoreNutsUtils.compareScopes(id1.getQueryMap().get("scope"), id2.getQueryMap().get("scope"));
    }

    public NutsId resolveBest(NutsId id1, NutsId id2) {
        if (id1 == null && id2 == null) {
            return null;
        }
        if (id1 == null) {
            return id2;
        }
        if (id2 == null) {
            return id1;
        }
        int x = id1.getVersion().compareTo(id2.getVersion());
        int c = CoreNutsUtils.compareScopes(id1.getQueryMap().get("scope"), id2.getQueryMap().get("scope"));
        if (x != 0) {
            if (c == 0) {
                //better version with same scope
                return x < 0 ? id1 : id2;
            } else {
                return c < 0 ? id1 : id2;
//                String s=c<0?id1.getQueryMap().get("scope"):id2.getQueryMap().get("scope");
//                return (x<0?id1:id2).setQueryProperty("scope",s);
            }
        }
        return c < 0 ? id1 : id2;
    }

    public void fixConflicts() {
        List<NutsId> toRemove = new ArrayList<>();
        for (Map.Entry<String, Set<NutsId>> conflict : resolveConflicts().entrySet()) {
            NutsId best = null;
            for (NutsId n : conflict.getValue()) {
                best = resolveBest(best, n);
            }
            for (NutsId n : conflict.getValue()) {
                if (!n.equals(best)) {
                    remove(n);
                }
            }
        }
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
        ids.remove(id);
        allVertices.remove(id);
        Set<NutsId> old = flatVersions.get(id.getSimpleName());
        if (old != null) {
            old.remove(id);
            if (old.isEmpty()) {
                flatVersions.remove(id.getSimpleName());
            }
        }
        //now remove all vertex to this id
        for (Iterator<Map.Entry<NutsId, NutsList>> iterator = allVertices.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<NutsId, NutsList> v = iterator.next();
            v.getValue().remove(id);
            if (v.getValue().isEmpty()) {
                iterator.remove();
            }
        }
    }

    public void addRoot(NutsId id) {
        roots.add(id);
    }

    public boolean contains(NutsId id) {
        return allVertices.containsKey(id);
    }

    public NutsId getNutsId(NutsId id) {
        return ids.get(id);
    }

    public void set(NutsId id) {
        NutsId old = ids.get(id);
        if (old == null) {
            ids.put(id, id);
        }
        Set<NutsId> versions = flatVersions.get(id.getSimpleName());
        if (versions == null) {
            versions = new HashSet<>();
        }
        versions.add(id);
        flatVersions.put(id.getSimpleName(), versions);
    }

    public void add(NutsId from, NutsId to) {
        set(from);
        set(to);
        NutsList vertices = allVertices.get(from);
        if (vertices == null) {
            vertices = new NutsList();
            allVertices.put(from, vertices);
        }
        vertices.add(to);

    }

    public NutsId[] collect(NutsId[] ids) {
        Set<NutsId> collected=new HashSet<>();
        for (NutsId id : ids) {
            visit(id,collected);
        }
        return collected.toArray(new NutsId[0]);
    }

    public void visit(NutsId id, Collection<NutsId> collected) {
        Set<NutsId> visited = new HashSet<>();
        Stack<NutsId> stack = new Stack<>();
        stack.push(id);
        visited.add(id);
        while (!stack.isEmpty()) {
            NutsId i = stack.pop();
            if (i != null) {
                NutsId f = getNutsId(i);
                if (f != null) {
                    collected.add(f);
                    NutsList next = allVertices.get(i);
                    if (next != null) {
                        for (NutsId j : next.list.values()) {
                            if (!visited.contains(j)) {
                                visited.add(j.getLongNameId());
                                stack.push(j);
                            }
                        }
                    }
                }
            }
        }
    }

    public static class NutsList {
        LinkedHashMap<NutsId, NutsId> list = new LinkedHashMap<>();

        public void add(NutsId id) {
            list.put(id/*.getLongNameId()*/, id);
        }

        public void remove(NutsId id) {
            list.remove(id/*.getLongNameId()*/);
        }

        public boolean isEmpty() {
            return list.isEmpty();
        }
    }
}
