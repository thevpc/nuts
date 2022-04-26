/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repository.impl.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.id.filter.NutsIdFilterAnd;
import net.thevpc.nuts.runtime.standalone.id.filter.NutsIdFilterOr;
import net.thevpc.nuts.runtime.standalone.id.filter.NutsPatternIdFilter;

import java.util.*;

/**
 * @author thevpc
 */
public class CommonRootsByIdHelper {

    private static Set<NutsId> resolveRootIdAnd(Set<NutsId> a, Set<NutsId> b, NutsSession session) {
        if (a == null) {
            return b;
        }
        if (b == null) {
            return a;
        }
        if (a.isEmpty()) {
            return Collections.emptySet();
        }
        if (b.isEmpty()) {
            return Collections.emptySet();
        }
        NutsId[] aa = a.toArray(new NutsId[0]);
        NutsId[] bb = b.toArray(new NutsId[0]);
        HashSet<NutsId> h = new HashSet<>();
        for (NutsId path : aa) {
            for (NutsId nutsPath : bb) {
                h.add(commonRoot(path, nutsPath, session));
            }
        }
        //TODO
        return compact(h);
    }

    private static Set<NutsId> compact(Set<NutsId> a) {
        Map<String, NutsId> x = new HashMap<>();
        if (a != null) {
            for (NutsId t : a) {
                String ts = pathOf(t);
                NutsId o = x.get(ts);
                if (o == null || (!deepOf(o) && deepOf(t))) {
                    x.put(ts, t);
                }
            }
        }
        return new HashSet<>(x.values());
    }

    static private boolean deepOf(NutsId p) {
        String g = p.getGroupId();
        String a = p.getArtifactId();
        return g.contains("*")
                || a.contains("*");
    }

    static private String pathOf(NutsId p) {
        String a = p.getArtifactId();
        String g = p.getGroupId();
        if (a.equals("*")) {
            if (g.equals("*")) {
                return "*";
            }
            int x = g.lastIndexOf('.');
            if (x >= 0) {
                String pp = g.substring(0, x);
                if (pp.isEmpty()) {
                    return "*";
                }
                return pp;
            }
            return "*";
        } else {
            return g;
        }
    }

    private static Set<NutsId> resolveRootIdOr(Set<NutsId> a, Set<NutsId> b) {
        Map<String, NutsId> x = new HashMap<>();
        if (a != null) {
            for (NutsId t : a) {
                String ts = pathOf(t);
                NutsId o = x.get(ts);
                if (o == null || (!deepOf(o) && deepOf(t))) {
                    x.put(ts, t);
                }
            }
        }
        if (b != null) {
            for (NutsId t : b) {
                String ts = pathOf(t);
                NutsId o = x.get(ts);
                if (o == null || (!deepOf(o) && deepOf(t))) {
                    x.put(ts, t);
                }
            }
        }
        return new HashSet<>(x.values());
    }

    private static NutsId commonRoot(NutsId a, NutsId b, NutsSession session) {
        if (a.getShortName().equals(b.getShortName())) {
            return a;
        }
        String[] aa = a.getGroupId().split("[.]");
        String[] bb = b.getGroupId().split("[.]");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(aa.length, bb.length); i++) {
            if (aa[i].equals(bb[i])) {
                if (sb.length() > 0) {
                    sb.append(".");
                }
                sb.append(aa[i]);
            }
        }
        return NutsId.of(sb.toString() + ":*").get( session);
    }

    private static Set<NutsId> resolveRootId(String groupId, String artifactId, String version, NutsSession session) {
        return new HashSet<>(Collections.singletonList(NutsIdBuilder.of()
                .setGroupId(NutsBlankable.isBlank(groupId) ? "*" : groupId)
                .setArtifactId(NutsBlankable.isBlank(artifactId) ? "*" : artifactId)
                .build()));
    }

    public static List<NutsId> resolveRootPaths(NutsIdFilter filter, NutsSession session) {
        return new ArrayList<>(CommonRootsByIdHelper.resolveRootIds(filter, session));
    }

    public static Set<NutsId> resolveRootIds(NutsIdFilter filter, NutsSession session) {
        Set<NutsId> v = resolveRootId0(filter, session);
        if (v == null) {
            HashSet<NutsId> s = new HashSet<>();
            s.add(NutsId.of("*:*").get( session));
            return s;
        }
        return v;
    }

    public static Set<NutsId> resolveRootId0(NutsIdFilter filter, NutsSession session) {
        if (filter == null) {
            return null;
        }
        if (filter instanceof NutsIdFilterAnd) {
            NutsIdFilterAnd f = ((NutsIdFilterAnd) filter);
            Set<NutsId> xx = null;
            for (NutsIdFilter g : f.getChildren()) {
                xx = resolveRootIdAnd(xx, resolveRootId0(g, session), session);
            }
            return xx;
        }
        if (filter instanceof NutsIdFilterOr) {
            NutsIdFilterOr f = ((NutsIdFilterOr) filter);

            NutsIdFilter[] y = f.getChildren();
            if (y.length == 0) {
                return null;
            }
            Set<NutsId> xx = resolveRootId0(y[0], session);
            for (int i = 1; i < y.length; i++) {
                xx = resolveRootIdOr(xx, resolveRootId0(y[i], session));
            }
            return xx;
        }
        if (filter instanceof NutsPatternIdFilter) {
            NutsPatternIdFilter f = ((NutsPatternIdFilter) filter);
            return resolveRootId(f.getId().getGroupId(), f.getId().getArtifactId(), f.getId().getVersion().toString(), session);
        }
        return null;
    }
}
