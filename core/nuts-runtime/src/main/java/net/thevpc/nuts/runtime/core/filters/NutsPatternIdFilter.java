/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.core.filters;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;

/**
 *
 * @author thevpc
 */
public class NutsPatternIdFilter extends AbstractNutsFilter implements NutsIdFilter {

    private NutsId id;
    private Pattern g;
    private Pattern n;
    private boolean wildcard;
    private NutsVersionFilter v;
    private Map<String, String> qm;
    private List<Predicate<Map<String, String>>> q = new ArrayList<>();

    public NutsPatternIdFilter(NutsWorkspace ws, NutsId id) {
        super(ws, NutsFilterOp.CUSTOM);
        this.id = id;
        this.wildcard = containsWildcad(id.toString());
        g = CoreStringUtils.toPattern(id.getGroupId());
        n = CoreStringUtils.toPattern(id.getArtifactId());
        v = id.getVersion().filter();
        qm = id.getProperties();
        for (Map.Entry<String, String> entry : id.getProperties().entrySet()) {
            String key = entry.getKey();
            String val = entry.getValue();
            if (!key.contains("*")) {
                q.add(new PredicateStaticKey(key, val));
            } else {
                if (!val.contains("*")) {
                }
                q.add(new PredicateWildKey(key, val));
            }
        }
    }

    public boolean isWildcard() {
        return wildcard;
    }

    public NutsId getId() {
        return id;
    }

    @Override
    public boolean acceptId(NutsId other, NutsSession session) {
        if (!g.matcher(other.getGroupId()).matches()) {
            return false;
        }
        if (!n.matcher(other.getArtifactId()).matches()) {
            return false;
        }
        if (!v.acceptVersion(other.getVersion(), session)) {
            return false;
        }
        Map<String, String> oqm = null;
        for (Predicate<Map<String, String>> pp : q) {
            if (oqm == null) {
                oqm = other.getProperties();
            }
            if (!pp.test(oqm)) {
                return false;
            }
        }
        return true;
    }

    private static class PredicateStaticKey implements Predicate<Map<String, String>> {

        private final String key;
        private final String val;
        private Pattern valPattern;

        public PredicateStaticKey(String key, String val) {
            this.key = key;
            this.val = val;
            this.valPattern = CoreStringUtils.toPattern(val);
        }

        @Override
        public boolean test(Map<String, String> x) {
            String sv = CoreStringUtils.trim(x.get(key));
            return valPattern.matcher(sv).matches();
        }
    }

    private static class PredicateWildKey implements Predicate<Map<String, String>> {

        private Pattern keyPattern;
        private Pattern valPattern;

        public PredicateWildKey(String key, String val) {
            this.keyPattern = CoreStringUtils.toPattern(key);
            this.valPattern = CoreStringUtils.toPattern(val);
        }

        @Override
        public boolean test(Map<String, String> x) {
            for (Map.Entry<String, String> entry : x.entrySet()) {
                if (keyPattern.matcher(entry.getKey()).matches()) {
                    String sv = CoreStringUtils.trim(entry.getValue());
                    return valPattern.matcher(sv).matches();
                }
            }
            return false;
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + Objects.hashCode(this.id);
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
        final NutsPatternIdFilter other = (NutsPatternIdFilter) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return id.toString();
    }

    public static boolean containsWildcad(String id) {
        return id.indexOf('*') >= 0 // ||id.indexOf('|')>=0
                ;
    }

    @Override
    public NutsIdFilter simplify() {
        return this;
    }
}
