/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.repository.impl.util;

import java.util.*;

import net.thevpc.nuts.NDefinitionFilter;
import net.thevpc.nuts.NId;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.definition.NDefinitionHelper;

/**
 * @author thevpc
 */
public class CommonRootsByPathHelper {

    private static Set<NPath> resolveRootIdAnd(Set<NPath> a, Set<NPath> b) {
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
        NPath[] aa = a.toArray(new NPath[0]);
        NPath[] bb = b.toArray(new NPath[0]);
        HashSet<NPath> h = new HashSet<>();
        for (NPath path : aa) {
            for (NPath nutsPath : bb) {
                h.add(commonRoot(path, nutsPath));
            }
        }
        //TODO
        return compact(h);
    }

    private static Set<NPath> compact(Set<NPath> a) {
        Map<String, NPath> x = new HashMap<>();
        if (a != null) {
            for (NPath t : a) {
                String ts=pathOf(t);
                NPath o = x.get(ts);
                if (o == null || (!deepOf(o) && deepOf(t))) {
                    x.put(ts, t);
                }
            }
        }
        return new HashSet<>(x.values());
    }

    static private boolean deepOf(NPath p){
        return p.getName().equals("*");
    }
    static private String pathOf(NPath p){
        if(p.getName().equals("*")){
            p=p.getParent();
        }
        if(p==null){
            return "";
        }
        return p.toString();
    }

    private static Set<NPath> resolveRootIdOr(Set<NPath> a, Set<NPath> b) {
        Map<String, NPath> x = new HashMap<>();
        if (a != null) {
            for (NPath t : a) {
                String ts=pathOf(t);
                NPath o = x.get(ts);
                if (o == null || (!deepOf(o) && deepOf(t))) {
                    x.put(ts, t);
                }
            }
        }
        if (b != null) {
            for (NPath t : b) {
                String ts=pathOf(t);
                NPath o = x.get(ts);
                if (o == null || (!deepOf(o) && deepOf(t))) {
                    x.put(ts, t);
                }
            }
        }
        return new HashSet<>(x.values());
    }

    private static NPath commonRoot(NPath a, NPath b) {
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
            return NPath.of(sb.toString()).resolve("*");
        }
        return NPath.of(sb.toString());
    }

    private static Set<NPath> resolveRootId(String groupId, String artifactId, String version) {
        String g = groupId;
        if (g == null) {
            g = "";
        }
        g = g.trim();
        if (g.isEmpty() || g.equals("*")) {
            return new HashSet<>(Collections.singletonList(NPath.of("*")));
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
            return new HashSet<>(Collections.singletonList(NPath.of(g)));
        }
        if (artifactId.length() > 0) {
            if (!artifactId.contains("*")) {
                if (version.length() > 0 && !version.contains("*") && !version.contains("[") && !version.contains("]")) {
                    return new HashSet<>(Collections.singletonList(NPath.of(g.replace('.', '/') + "/" + artifactId + "/" + version)));
                } else {
                    return new HashSet<>(Collections.singletonList(NPath.of(g.replace('.', '/') + "/" + artifactId)));
                }
            }
        }
        return new HashSet<>(Collections.singletonList(NPath.of(g.replace('.', '/'))));
    }

    public static List<NPath> resolveRootPaths(NDefinitionFilter filter) {
        return new ArrayList<>(CommonRootsByPathHelper.resolveRootIds(filter));
    }

    public static Set<NPath> resolveRootIds(NDefinitionFilter filter) {
        Set<NPath> v = resolveRootId0(filter);
        if (v == null) {
            HashSet<NPath> s = new HashSet<>();
            s.add(NPath.of("*"));
            return s;
        }
        return v;
    }

    public static Set<NPath> resolveRootId0(NDefinitionFilter filter) {
        if (filter == null) {
            return null;
        }
        NDefinitionFilter[] aa= NDefinitionHelper.toAndChildren(filter).orNull();
        if (aa!=null) {
            Set<NPath> xx = null;
            for (NDefinitionFilter g : aa) {
                xx = resolveRootIdAnd(xx, resolveRootId0(g));
            }
            return xx;
        }
        aa= NDefinitionHelper.toOrChildren(filter).orNull();
        if (aa!=null) {
            if (aa.length == 0) {
                return null;
            }
            Set<NPath> xx = resolveRootId0(aa[0]);
            for (int i = 1; i < aa.length; i++) {
                xx = resolveRootIdOr(xx, resolveRootId0(aa[i]));
            }
            return xx;
        }
        NId pid=NDefinitionHelper.toPatternId(filter).orNull();
        if ( pid!=null) {
            return resolveRootId(pid.getGroupId(), pid.getArtifactId(),pid.getVersion().toString());
        }
        return null;
    }
}
