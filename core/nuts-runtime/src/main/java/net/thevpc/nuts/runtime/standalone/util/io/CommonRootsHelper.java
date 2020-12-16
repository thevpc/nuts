/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.util.io;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import net.thevpc.nuts.NutsIdFilter;
import net.thevpc.nuts.runtime.standalone.NutsPatternIdFilter;
import net.thevpc.nuts.runtime.core.filters.id.NutsIdFilterAnd;
import net.thevpc.nuts.runtime.core.filters.id.NutsIdFilterOr;

/**
 *
 * @author thevpc
 */
public class CommonRootsHelper {

    private static Set<PathBase> resolveRootIdAnd(Set<PathBase> a, Set<PathBase> b) {
        Set<PathBase> e = new HashSet<>();
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        if (a.isEmpty()) {
            return Collections.EMPTY_SET;
        }
        if (b.isEmpty()) {
            return Collections.EMPTY_SET;
        }
        PathBase[] aa = a.toArray(new PathBase[0]);
        PathBase[] bb = b.toArray(new PathBase[0]);
        HashSet<PathBase> h = new HashSet<>();
        for (int i = 0; i < aa.length; i++) {
            for (int j = 0; j < bb.length; j++) {
                h.add(commonRoot(aa[i], bb[j]));
            }
        }
        //TODO
        return compact(h);
    }

    private static Set<PathBase> compact(Set<PathBase> a) {
        Map<String, PathBase> x = new HashMap<>();
        if (a != null) {
            for (PathBase t : a) {
                PathBase o = x.get(t.name);
                if (o == null || (!o.deep && t.deep)) {
                    x.put(t.name, t);
                }
            }
        }
        return new HashSet<>(x.values());
    }

    private static Set<PathBase> resolveRootIdOr(Set<PathBase> a, Set<PathBase> b) {
        Map<String, PathBase> x = new HashMap<>();
        if (a != null) {
            for (PathBase t : a) {
                PathBase o = x.get(t.name);
                if (o == null || (!o.deep && t.deep)) {
                    x.put(t.name, t);
                }
            }
        }
        if (b != null) {
            for (PathBase t : a) {
                PathBase o = x.get(t.name);
                if (o == null || (!o.deep && t.deep)) {
                    x.put(t.name, t);
                }
            }
        }
        return new HashSet<>(x.values());
    }

    private static PathBase commonRoot(PathBase a, PathBase b) {
        String[] aa = a.name.split("[.]");
        String[] bb = b.name.split("[.]");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(aa.length, bb.length); i++) {
            if (aa[i].equals(bb[i])) {
                if (sb.length() > 0) {
                    sb.append(".");
                }
                sb.append(aa[i]);
            }
        }
        return new PathBase(sb.toString(), a.deep || b.deep);
    }

    private static Set<PathBase> resolveRootId(String id) {
        String g = id;
        if (g == null) {
            g = "";
        }
        g = g.trim();
        if (g.isEmpty()) {
            return new HashSet<PathBase>(Arrays.asList(new PathBase(g, true)));
        }
        int i = g.indexOf("*");
        boolean deep = false;
        if (i >= 0) {
            deep = true;
            g = g.substring(0, i);
            int j = g.indexOf(".");
            if (j >= 0) {
                g = g.substring(0, j);
            }
        }
        return new HashSet<PathBase>(Arrays.asList(new PathBase(g, deep)));
    }

    public static List<PathBase> resolveRootPaths(NutsIdFilter filter) {
        return CommonRootsHelper.resolveRootIds(filter).stream().map(x -> (x == null || x.name.isEmpty()) ? new PathBase(".", x.deep) : new PathBase(
                x.name.replace('.', File.separatorChar), x.deep
        )).collect(Collectors.toList());
    }

    public static Set<PathBase> resolveRootIds(NutsIdFilter filter) {
        Set<PathBase> v = resolveRootId0(filter);
        if (v == null) {
            HashSet<PathBase> s = new HashSet<>();
            s.add(new PathBase("", true));
            return s;
        }
        return v;
    }

    public static Set<PathBase> resolveRootId0(NutsIdFilter filter) {
        if (filter == null) {
            return null;
        }
        if (filter instanceof NutsIdFilterAnd) {
            NutsIdFilterAnd f = ((NutsIdFilterAnd) filter);
            Set<PathBase> xx = null;
            for (NutsIdFilter g : f.getChildren()) {
                xx = resolveRootIdAnd(xx, resolveRootId0(g));
            }
            return xx;
        }
        if (filter instanceof NutsIdFilterOr) {
            NutsIdFilterOr f = ((NutsIdFilterOr) filter);

            NutsIdFilter[] y = f.getChildren();
            if (y.length == 0) {
                return null;
            }
            Set<PathBase> xx = resolveRootId0(y[0]);
            for (int i = 1; i < y.length; i++) {
                xx = resolveRootIdOr(xx, resolveRootId0(y[i]));
            }
            return xx;
        }
        if (filter instanceof NutsPatternIdFilter) {
            NutsPatternIdFilter f = ((NutsPatternIdFilter) filter);
            return resolveRootId(f.getId().getGroupId());
        }
        return null;
    }

    public static class PathBase {

        private String name;
        private boolean deep;

        public PathBase(String name, boolean deep) {
            this.name = name;
            this.deep = deep;
        }

        public String getName() {
            return name;
        }

        public boolean isDeep() {
            return deep;
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 37 * hash + Objects.hashCode(this.name);
            hash = 37 * hash + (this.deep ? 1 : 0);
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
            final PathBase other = (PathBase) obj;
            if (this.deep != other.deep) {
                return false;
            }
            if (!Objects.equals(this.name, other.name)) {
                return false;
            }
            return true;
        }

    }
}
