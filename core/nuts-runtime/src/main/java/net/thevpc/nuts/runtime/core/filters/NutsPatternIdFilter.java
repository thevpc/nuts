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
import net.thevpc.nuts.runtime.bundles.string.GlobUtils;
import net.thevpc.nuts.runtime.core.filters.id.AbstractIdFilter;

/**
 *
 * @author thevpc
 */
public class NutsPatternIdFilter extends AbstractIdFilter implements NutsIdFilter {

    private NutsId id;
    private Pattern g;
    private Pattern n;
    private boolean wildcard;
    private NutsVersionFilter v;
    private Map<String, String> qm;
    private List<Predicate<Map<String, String>>> q = new ArrayList<>();

    public NutsPatternIdFilter(NutsSession session, NutsId id) {
        super(session, NutsFilterOp.CUSTOM);
        this.id = id;
        this.wildcard = containsWildcad(id.toString());
        g = GlobUtils.ofExact(id.getGroupId());
        n = GlobUtils.ofExact(id.getArtifactId());
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

    private static class PredicateStaticKey extends NutsPredicates.BasePredicate<Map<String, String>> {

        private final String key;
        private final String val;
        private Pattern valPattern;

        public PredicateStaticKey(String key, String val) {
            this.key = key;
            this.val = val;
            this.valPattern = GlobUtils.ofExact(val);
        }

        @Override
        public boolean test(Map<String, String> x) {
            String sv = NutsUtilStrings.trim(x.get(key));
            return valPattern.matcher(sv).matches();
        }
        @Override
        public String toString() {
            return "EntryMatches[key='"+key+"',val='"+(NutsBlankable.isBlank(val)?"*":val)+"']";
        }
    }

    private static class PredicateWildKey extends NutsPredicates.BasePredicate<Map<String, String>> {

        private Pattern keyPattern;
        private Pattern valPattern;
        private String skey;
        private String sval;

        public PredicateWildKey(String key, String val) {
            this.keyPattern = GlobUtils.ofExact(key);
            this.valPattern = GlobUtils.ofExact(val);
            skey=NutsBlankable.isBlank(key)?"*":key;
            sval=NutsBlankable.isBlank(val)?"*":val;
        }

        @Override
        public boolean test(Map<String, String> x) {
            for (Map.Entry<String, String> entry : x.entrySet()) {
                if (keyPattern.matcher(entry.getKey()).matches()) {
                    String sv = NutsUtilStrings.trim(entry.getValue());
                    return valPattern.matcher(sv).matches();
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return "EntryMatches[key='"+skey+"',val='"+sval+"']";
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
