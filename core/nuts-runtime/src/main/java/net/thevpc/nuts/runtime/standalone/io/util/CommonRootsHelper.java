/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.io.util;

import java.util.*;

import net.thevpc.nuts.NutsIdFilter;
import net.thevpc.nuts.NutsPath;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.runtime.standalone.id.filter.NutsIdFilterAnd;
import net.thevpc.nuts.runtime.standalone.id.filter.NutsIdFilterOr;
import net.thevpc.nuts.runtime.standalone.id.filter.NutsPatternIdFilter;

/**
 * @author thevpc
 */
public class CommonRootsHelper {

    private static Set<NutsPath> resolveRootIdAnd(Set<NutsPath> a, Set<NutsPath> b, NutsSession session) {
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
        NutsPath[] aa = a.toArray(new NutsPath[0]);
        NutsPath[] bb = b.toArray(new NutsPath[0]);
        HashSet<NutsPath> h = new HashSet<>();
        for (NutsPath path : aa) {
            for (NutsPath nutsPath : bb) {
                h.add(commonRoot(path, nutsPath, session));
            }
        }
        //TODO
        return compact(h);
    }

    private static Set<NutsPath> compact(Set<NutsPath> a) {
        Map<String, NutsPath> x = new HashMap<>();
        if (a != null) {
            for (NutsPath t : a) {
                String ts=pathOf(t);
                NutsPath o = x.get(ts);
                if (o == null || (!deepOf(o) && deepOf(t))) {
                    x.put(ts, t);
                }
            }
        }
        return new HashSet<>(x.values());
    }

    static private boolean deepOf(NutsPath p){
        return p.getName().equals("*");
    }
    static private String pathOf(NutsPath p){
        if(p.getName().equals("*")){
            p=p.getParent();
        }
        if(p==null){
            return "";
        }
        return p.toString();
    }

    private static Set<NutsPath> resolveRootIdOr(Set<NutsPath> a, Set<NutsPath> b) {
        Map<String, NutsPath> x = new HashMap<>();
        if (a != null) {
            for (NutsPath t : a) {
                String ts=pathOf(t);
                NutsPath o = x.get(ts);
                if (o == null || (!deepOf(o) && deepOf(t))) {
                    x.put(ts, t);
                }
            }
        }
        if (b != null) {
            for (NutsPath t : b) {
                String ts=pathOf(t);
                NutsPath o = x.get(ts);
                if (o == null || (!deepOf(o) && deepOf(t))) {
                    x.put(ts, t);
                }
            }
        }
        return new HashSet<>(x.values());
    }

    private static NutsPath commonRoot(NutsPath a, NutsPath b, NutsSession session) {
        boolean a_deep;
        String a_path;
        boolean b_deep;
        String b_path;
        if(a.getName().equals("*")){
            a_deep=true;
            a_path=a.getParent()==null?"":a.getParent().toString();
        }else{
            a_deep=false;
            a_path=a.toString();
        }
        if(b.getName().equals("*")){
            b_deep=true;
            b_path=b.getParent()==null?"":b.getParent().toString();
        }else{
            b_deep=false;
            b_path=a.toString();
        }
        String[] aa = a_path.split("[.]");
        String[] bb = b_path.split("[.]");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(aa.length, bb.length); i++) {
            if (aa[i].equals(bb[i])) {
                if (sb.length() > 0) {
                    sb.append(".");
                }
                sb.append(aa[i]);
            }
        }
        if(a_deep || b_deep){
            return NutsPath.of(sb.toString(),session).resolve("*");
        }
        return NutsPath.of(sb.toString(),session);
    }

    private static Set<NutsPath> resolveRootId(String groupId, String artifactId, String version,NutsSession session) {
        String g = groupId;
        if (g == null) {
            g = "";
        }
        g = g.trim();
        if (g.isEmpty() || g.equals("*")) {
            return new HashSet<>(Collections.singletonList(NutsPath.of("*", session)));
        }
        int i = g.indexOf("*");
        if (i >= 0) {
            g = g.substring(0, i);
            int j = g.indexOf(".");
            if (j >= 0) {
                g = g.substring(0, j);
            }
            if(g.isEmpty()){
                g="*";
            }else{
                g=g.replace('.', '/');
                if(!g.endsWith("/")){
                    g+="/";
                }
                g+="*";
            }
            return new HashSet<>(Collections.singletonList(NutsPath.of(g, session)));
        }
        if (artifactId.length() > 0) {
            if (!artifactId.contains("*")) {
                if (version.length() > 0 && !version.contains("*") && !version.contains("[") && !version.contains("]")) {
                    return new HashSet<>(Collections.singletonList(NutsPath.of(g.replace('.', '/') + "/" + artifactId + "/" + version, session)));
                } else {
                    return new HashSet<>(Collections.singletonList(NutsPath.of(g.replace('.', '/') + "/" + artifactId, session)));
                }
            }
        }
        return new HashSet<>(Collections.singletonList(NutsPath.of(g.replace('.', '/'), session)));
    }

    public static List<NutsPath> resolveRootPaths(NutsIdFilter filter, NutsSession session) {
        return new ArrayList<>(CommonRootsHelper.resolveRootIds(filter, session));
    }

    public static Set<NutsPath> resolveRootIds(NutsIdFilter filter, NutsSession session) {
        Set<NutsPath> v = resolveRootId0(filter, session);
        if (v == null) {
            HashSet<NutsPath> s = new HashSet<>();
            s.add(NutsPath.of("*",session));
            return s;
        }
        return v;
    }

    public static Set<NutsPath> resolveRootId0(NutsIdFilter filter, NutsSession session) {
        if (filter == null) {
            return null;
        }
        if (filter instanceof NutsIdFilterAnd) {
            NutsIdFilterAnd f = ((NutsIdFilterAnd) filter);
            Set<NutsPath> xx = null;
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
            Set<NutsPath> xx = resolveRootId0(y[0], session);
            for (int i = 1; i < y.length; i++) {
                xx = resolveRootIdOr(xx, resolveRootId0(y[i], session));
            }
            return xx;
        }
        if (filter instanceof NutsPatternIdFilter) {
            NutsPatternIdFilter f = ((NutsPatternIdFilter) filter);
            return resolveRootId(f.getId().getGroupId(), f.getId().getArtifactId(), f.getId().getVersion().toString(),session);
        }
        return null;
    }
}
