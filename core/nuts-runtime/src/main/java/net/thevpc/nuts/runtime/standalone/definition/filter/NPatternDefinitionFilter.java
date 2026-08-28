/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.runtime.standalone.definition.filter;

import net.thevpc.nuts.artifact.*;
import net.thevpc.nuts.command.NFetch;
import net.thevpc.nuts.internal.rpi.NDefinitionFilterRPI;
import net.thevpc.nuts.runtime.standalone.util.filters.CoreFilterUtils;
import net.thevpc.nuts.runtime.standalone.xtra.glob.GlobUtils;
import net.thevpc.nuts.spi.base.NPredicateBase;
import net.thevpc.nuts.util.NBlankable;
import net.thevpc.nuts.util.NFilterOp;
import net.thevpc.nuts.util.NStringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.regex.Pattern;

/**
 * @author thevpc
 */
public class NPatternDefinitionFilter extends DefinitionFilterBase implements NDefinitionFilter {

    private NId id;
    private Pattern g;
    private Pattern n;
    private boolean wildcard;
    private NVersionFilter v;
    private Map<String, String> qm;
    private List<Predicate<Map<String, String>>> q = new ArrayList<>();
    private boolean any;

    public NPatternDefinitionFilter(NId id) {
        super(NFilterOp.CUSTOM);
        this.id = id;
        String sid = id.toString();
        if (NBlankable.isBlank(id)) {
            any = true;
        } else {
            switch (sid) {
                case "*":
                case "*.*":
                case "*#*":
                case "*.*#*": {
                    this.wildcard = true;
                    any = true;
                    break;
                }
                default: {
                    this.wildcard = containsWildcad(sid);
                    g = GlobUtils.ofExact(id.groupId());
                    n = GlobUtils.ofExact(id.artifactId());
                    v = id.version().toFilter();
                    qm = id.properties();
                    for (Map.Entry<String, String> entry : id.properties().entrySet()) {
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
    public boolean acceptDefinition(NDefinition def) {
        if (any) {
            return true;
        }
        NId other = def.id();
        if (!g.matcher(other.groupId()).matches()) {
            return false;
        }
        if (!n.matcher(other.artifactId()).matches()) {
            return false;
        }
        if (!v.acceptVersion(other.version())) {
            return false;
        }
        Map<String, String> oqm = null;
        for (Predicate<Map<String, String>> pp : q) {
            if (oqm == null) {
                oqm = other.properties();
            }
            if (!pp.test(oqm)) {
                return false;
            }
        }
        NEnvCondition condition = this.id.condition();
        if (condition != null && !condition.isBlank()) {
            NEnvCondition otherCondition = null;
            try {
                otherCondition = NFetch.of(other)
                        .dependencyFilter(NDependencyFilter.ofRunnable())
                        .getResultDescriptor().condition();
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

    private static class PredicateStaticKey extends NPredicateBase<Map<String, String>> {

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
            String sv = NStringUtils.strip(x.get(key));
            return valPattern.matcher(sv).matches();
        }

        @Override
        public String toString() {
            return "EntryMatches[key='" + key + "',val='" + (NBlankable.isBlank(val) ? "*" : val) + "']";
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            PredicateStaticKey that = (PredicateStaticKey) o;
            return Objects.equals(key, that.key) && Objects.equals(val, that.val) && Objects.equals(valPattern, that.valPattern);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, val, valPattern);
        }
    }

    private static class PredicateWildKey extends NPredicateBase<Map<String, String>> {

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
                    String sv = NStringUtils.strip(entry.getValue());
                    return valPattern.matcher(sv).matches();
                }
            }
            return false;
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;
            PredicateWildKey that = (PredicateWildKey) o;
            return Objects.equals(keyPattern, that.keyPattern) && Objects.equals(valPattern, that.valPattern) && Objects.equals(skey, that.skey) && Objects.equals(sval, that.sval);
        }

        @Override
        public int hashCode() {
            return Objects.hash(keyPattern, valPattern, skey, sval);
        }

        @Override
        public String toString() {
            return "EntryMatches[key='" + skey + "',val='" + sval + "']";
        }
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;
        NPatternDefinitionFilter that = (NPatternDefinitionFilter) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), id);
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
    public NDefinitionFilter simplify() {
        if (any) {
            return NDefinitionFilterRPI.of().always();
        }
        return this;
    }
}
