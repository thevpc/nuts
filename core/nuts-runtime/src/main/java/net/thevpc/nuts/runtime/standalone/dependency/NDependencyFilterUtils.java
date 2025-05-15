package net.thevpc.nuts.runtime.standalone.dependency;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.dependency.filter.NDependencyScopeFilter;
import net.thevpc.nuts.util.NFilter;
import net.thevpc.nuts.util.NFilterOp;
import net.thevpc.nuts.util.NFunction;
import net.thevpc.nuts.util.NRef;

import java.util.*;
import java.util.function.Function;

public class NDependencyFilterUtils {
    public static Set<NDependencyScope> toScopeFilterPossibilities(NDependencyFilter filter) {
        if (filter instanceof NDependencyScopeFilter) {
            return new LinkedHashSet<>(((NDependencyScopeFilter) filter).getScopes());
        }
        if (filter.getFilterOp() == NFilterOp.AND) {
            Set<NDependencyScope> a = null;
            for (NFilter subFilter : filter.getSubFilters()) {
                Set<NDependencyScope> r = toScopeFilterPossibilities((NDependencyFilter) subFilter);
                if (r == null) {
                    return null;
                }
                if (a == null) {
                    a = new LinkedHashSet<>(r);
                } else {
                    a.retainAll(r);
                }
            }
            if (a == null) {
                return new LinkedHashSet<>();
            }
            return a;
        }
        if (filter.getFilterOp() == NFilterOp.OR) {
            Set<NDependencyScope> a = null;
            for (NFilter subFilter : filter.getSubFilters()) {
                Set<NDependencyScope> r = toScopeFilterPossibilities((NDependencyFilter) subFilter);
                if (r == null) {
                    return null;
                }
                if (a == null) {
                    a = new LinkedHashSet<>(r);
                } else {
                    a.addAll(r);
                }
            }
            if (a == null) {
                return new LinkedHashSet<>();
            }
            return a;
        }
        return null;
    }

    public static boolean isScopeFilter(NDependencyFilter filter) {
        if (filter instanceof NDependencyScopeFilter) {
            return true;
        }
        if (filter.getFilterOp() == NFilterOp.AND) {
            return filter.getSubFilters().stream().allMatch(x -> isScopeFilter((NDependencyFilter) x));
        }
        if (filter.getFilterOp() == NFilterOp.OR) {
            return filter.getSubFilters().stream().allMatch(x -> isScopeFilter((NDependencyFilter) x));
        }
        return false;
    }

    public static NDependencyFilter addScope(NDependencyFilter parent, NDependencyScopePattern scope) {
        if (parent == null) {
            return NDependencyFilters.of().byScope(scope);
        }
        if (scope == null) {
            return parent;
        }
        NRef<Boolean> found = NRef.of(false);
        NDependencyFilter np = replaceFilter(parent, new NFunction<NDependencyFilter, NDependencyFilter>() {
            @Override
            public NDependencyFilter apply(NDependencyFilter old) {
                Set<NDependencyScope> li = toScopeFilterPossibilities(old);
                if (li != null) {
                    found.set(true);
                    Set<NDependencyScope> li2 = new LinkedHashSet<>(li);
                    li2.addAll(scope.toScopes());
                    if (!li2.equals(li)) {
                        return NDependencyFilters.of().byScope(li2.toArray(new NDependencyScope[0]));
                    }
                }
                return old;
            }
        });
        if (!found.get()) {
            np = np.and(NDependencyFilters.of().byScope(scope));
        }
        return np;
    }

    public static NDependencyFilter replaceFilter(NDependencyFilter parent, Function<NDependencyFilter, NDependencyFilter> replacer) {
        if (parent == null) {
            return null;
        }
        NDependencyFilter n = replacer.apply(parent);
        if (n == null) {
            return null;
        }
        if (n != parent) {
            return n;
        }
        if (parent.getFilterOp() == NFilterOp.AND) {
            List<NDependencyFilter> newList = new ArrayList<>();
            boolean someChanges = false;
            for (NFilter subFilter : parent.getSubFilters()) {
                n = replacer.apply((NDependencyFilter) subFilter);
                if (n == null) {
                    someChanges = true;
                } else if (n == subFilter) {
                    newList.add(n);
                } else {
                    someChanges = true;
                    newList.add(n);
                }
            }
            if (someChanges) {
                return NDependencyFilters.of().all(newList.toArray(new NDependencyFilter[0]));
            }
            return parent;
        }
        return parent;
    }
}
