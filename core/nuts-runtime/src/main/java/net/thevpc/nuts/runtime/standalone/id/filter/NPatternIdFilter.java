/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.id.filter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

import net.thevpc.nuts.*;
import net.thevpc.nuts.NEnvCondition;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.spi.base.AbstractIdFilter;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.runtime.standalone.xtra.glob.GlobUtils;
import net.thevpc.nuts.spi.base.AbstractNPredicate;
import net.thevpc.nuts.util.*;

/**
 * @author thevpc
 */
public class NPatternIdFilter extends AbstractIdFilter implements NIdFilter {

    private NId id;
    private Pattern g;
    private Pattern n;
    private boolean wildcard;
    private NVersionFilter v;
    private Map<String, String> qm;
    private List<Predicate<Map<String, String>>> q = new ArrayList<>();

    public NPatternIdFilter(NId id) {
        super(NFilterOp.CUSTOM);
        this.id = id;
        this.wildcard = containsWildcard(id.toString());
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

    public NId getId() {
        return id;
    }

    @Override
    public boolean acceptId(NId other) {
        if (!g.matcher(other.getGroupId()).matches()) {
            return false;
        }
        if (!n.matcher(other.getArtifactId()).matches()) {
            return false;
        }
        if (!v.acceptVersion(other.getVersion())) {
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
        NEnvCondition condition = id.getCondition();
        if (condition != null && !condition.isBlank()) {
            NEnvCondition otherCondition = null;
            try {
                otherCondition = NFetchCmd.of(other)
                        .setDependencyFilter(NDependencyFilters.of().byRunnable())
                        .getResultDescriptor().getCondition();
            } catch (Exception ex) {
                //ignore any error
            }
            if (otherCondition != null && !otherCondition.isBlank()) {
                if (!CoreFilterUtils.acceptCondition(condition, otherCondition)) {
                    return false;
                }
            }
        }
        return true;
    }

    private static class PredicateStaticKey extends AbstractNPredicate<Map<String, String>> {

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
            String sv = NStringUtils.trim(x.get(key));
            return valPattern.matcher(sv).matches();
        }

        @Override
        public String toString() {
            return "EntryMatches[key='" + key + "',val='" + (NBlankable.isBlank(val) ? "*" : val) + "']";
        }
    }

    private static class PredicateWildKey extends AbstractNPredicate<Map<String, String>> {

        private Pattern keyPattern;
        private Pattern valPattern;
        private String skey;
        private String sval;

        public PredicateWildKey(String key, String val) {
            this.keyPattern = GlobUtils.ofExact(key);
            this.valPattern = GlobUtils.ofExact(val);
            skey = NBlankable.isBlank(key) ? "*" : key;
            sval = NBlankable.isBlank(val) ? "*" : val;
        }

        @Override
        public boolean test(Map<String, String> x) {
            for (Map.Entry<String, String> entry : x.entrySet()) {
                if (keyPattern.matcher(entry.getKey()).matches()) {
                    String sv = NStringUtils.trim(entry.getValue());
                    return valPattern.matcher(sv).matches();
                }
            }
            return false;
        }

        @Override
        public String toString() {
            return "EntryMatches[key='" + skey + "',val='" + sval + "']";
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
        final NPatternIdFilter other = (NPatternIdFilter) obj;
        if (!Objects.equals(this.id, other.id)) {
            return false;
        }
        return true;
    }

    @Override
    public String toString() {
        return id.toString();
    }

    public static boolean containsWildcard(NVersion version) {
        if(NBlankable.isBlank(version)) {
            return false;
        }
        return containsWildcardString(version.toString());
    }

    public static boolean containsWildcard(String id) {
        if(NBlankable.isBlank(id)) {
            return false;
        }
        NId nId = NId.of(id);
        if(containsWildcardString(nId.getArtifactId())){
            return true;
        }
        if(containsWildcardString(nId.getGroupId())){
            return true;
        }
        if(containsWildcard(nId.getVersion())){
            return true;
        }
        return id.indexOf('*') >= 0;
    }
    private static boolean containsWildcardString(String id) {
        return !NBlankable.isBlank(id) && id.indexOf('*') >= 0;
    }

    @Override
    public NIdFilter simplify() {
        return this;
    }
}
