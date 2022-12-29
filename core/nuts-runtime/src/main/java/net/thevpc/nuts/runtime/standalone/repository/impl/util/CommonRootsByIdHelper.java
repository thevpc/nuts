/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repository.impl.util;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.id.filter.NIdFilterAnd;
import net.thevpc.nuts.runtime.standalone.id.filter.NIdFilterOr;
import net.thevpc.nuts.runtime.standalone.id.filter.NPatternIdFilter;

import java.util.*;

/**
 * @author thevpc
 */
public class CommonRootsByIdHelper {

    private static Set<NId> resolveRootIdAnd(Set<NId> a, Set<NId> b, NSession session) {
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
        NId[] aa = a.toArray(new NId[0]);
        NId[] bb = b.toArray(new NId[0]);
        HashSet<NId> h = new HashSet<>();
        for (NId path : aa) {
            for (NId nutsPath : bb) {
                h.add(commonRoot(path, nutsPath, session));
            }
        }
        //TODO
        return compact(h);
    }

    private static Set<NId> compact(Set<NId> a) {
        Map<String, NId> x = new HashMap<>();
        if (a != null) {
            for (NId t : a) {
                String ts = pathOf(t);
                NId o = x.get(ts);
                if (o == null || (!deepOf(o) && deepOf(t))) {
                    x.put(ts, t);
                }
            }
        }
        return new HashSet<>(x.values());
    }

    static private boolean deepOf(NId p) {
        String g = p.getGroupId();
        String a = p.getArtifactId();
        return g.contains("*")
                || a.contains("*");
    }

    static private String pathOf(NId p) {
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

    private static Set<NId> resolveRootIdOr(Set<NId> a, Set<NId> b) {
        Map<String, NId> x = new HashMap<>();
        if (a != null) {
            for (NId t : a) {
                String ts = pathOf(t);
                NId o = x.get(ts);
                if (o == null || (!deepOf(o) && deepOf(t))) {
                    x.put(ts, t);
                }
            }
        }
        if (b != null) {
            for (NId t : b) {
                String ts = pathOf(t);
                NId o = x.get(ts);
                if (o == null || (!deepOf(o) && deepOf(t))) {
                    x.put(ts, t);
                }
            }
        }
        return new HashSet<>(x.values());
    }

    private static NId commonRoot(NId a, NId b, NSession session) {
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
        return NId.of(sb.toString() + ":*").get( session);
    }

    private static Set<NId> resolveRootId(String groupId, String artifactId, String version, NSession session) {
        return new HashSet<>(Collections.singletonList(NIdBuilder.of()
                .setGroupId(NBlankable.isBlank(groupId) ? "*" : groupId)
                .setArtifactId(NBlankable.isBlank(artifactId) ? "*" : artifactId)
                .build()));
    }

    public static List<NId> resolveRootPaths(NIdFilter filter, NSession session) {
        return new ArrayList<>(CommonRootsByIdHelper.resolveRootIds(filter, session));
    }

    public static Set<NId> resolveRootIds(NIdFilter filter, NSession session) {
        Set<NId> v = resolveRootId0(filter, session);
        if (v == null) {
            HashSet<NId> s = new HashSet<>();
            s.add(NId.of("*:*").get( session));
            return s;
        }
        return v;
    }

    public static Set<NId> resolveRootId0(NIdFilter filter, NSession session) {
        if (filter == null) {
            return null;
        }
        if (filter instanceof NIdFilterAnd) {
            NIdFilterAnd f = ((NIdFilterAnd) filter);
            Set<NId> xx = null;
            for (NIdFilter g : f.getChildren()) {
                xx = resolveRootIdAnd(xx, resolveRootId0(g, session), session);
            }
            return xx;
        }
        if (filter instanceof NIdFilterOr) {
            NIdFilterOr f = ((NIdFilterOr) filter);

            NIdFilter[] y = f.getChildren();
            if (y.length == 0) {
                return null;
            }
            Set<NId> xx = resolveRootId0(y[0], session);
            for (int i = 1; i < y.length; i++) {
                xx = resolveRootIdOr(xx, resolveRootId0(y[i], session));
            }
            return xx;
        }
        if (filter instanceof NPatternIdFilter) {
            NPatternIdFilter f = ((NPatternIdFilter) filter);
            return resolveRootId(f.getId().getGroupId(), f.getId().getArtifactId(), f.getId().getVersion().toString(), session);
        }
        return null;
    }
}
