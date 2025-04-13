package net.thevpc.nuts.runtime.standalone.definition;

import net.thevpc.nuts.*;
import net.thevpc.nuts.runtime.standalone.definition.filter.*;
import net.thevpc.nuts.util.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class NDefinitionFilterUtils {
    public static NOptional<NDefinitionFilter[]> toAndChildren(NDefinitionFilter id) {
        if (id instanceof NDefinitionFilterAnd) {
            return NOptional.of(((NDefinitionFilterAnd) id).getChildren());
        }
        return NOptional.ofEmpty();
    }

    public static NOptional<NDefinitionFilter[]> toOrChildren(NDefinitionFilter id) {
        if (id instanceof NDefinitionFilterOr) {
            return NOptional.of(((NDefinitionFilterOr) id).getChildren());
        }
        return NOptional.ofEmpty();
    }

    public static NOptional<NId> toPatternId(NDefinitionFilter id) {
        if (id instanceof NPatternDefinitionFilter) {
            return NOptional.of(((NPatternDefinitionFilter) id).getId());
        }
        return NOptional.ofEmpty();
    }

    public static NDefinitionFilterToNIdPredicate2 toIdPredicate(NDefinitionFilter filter) {
        return new NDefinitionFilterToNIdPredicate2(filter);
    }

    public static NPatternDefinitionFilter[] asPatternDefinitionFilterOrList(NDefinitionFilter defFilter0) {
        if (defFilter0 == null) {
            return new NPatternDefinitionFilter[0];
        }
        List<NPatternDefinitionFilter> orResult = new ArrayList<>();
        if (defFilter0 instanceof NDefinitionFilterOr) {
            for (NDefinitionFilter child : ((NDefinitionFilterOr) defFilter0).getChildren()) {
                if (child instanceof NPatternDefinitionFilter) {
                    orResult.add((NPatternDefinitionFilter) child);
                }
            }
        } else if (defFilter0 instanceof NDefinitionFilterAnd) {
            for (NDefinitionFilter child : ((NDefinitionFilterAnd) defFilter0).getChildren()) {
                NPatternDefinitionFilter[] found = asPatternDefinitionFilterOrList(child);
                if (found.length > 0) {
                    if (orResult.isEmpty()) {
                        orResult.addAll(Arrays.asList(found));
                    } else {
                        // Too complex
                        return new NPatternDefinitionFilter[0];
                    }
                }
            }
        } else if (defFilter0 instanceof NPatternDefinitionFilter) {
            orResult.add((NPatternDefinitionFilter) defFilter0);
        }
        return orResult.toArray(new NPatternDefinitionFilter[0]);
    }

    public static boolean isAlways(NDefinitionFilter any) {
        return any == null || any.getFilterOp() == NFilterOp.TRUE;
    }

    public static boolean isNever(NDefinitionFilter any) {
        return any != null && any.getFilterOp() == NFilterOp.FALSE;
    }

    public static boolean isInstallStatusFilter(NDefinitionFilter filter) {
        if (filter instanceof NInstallStatusDefinitionFilter2) {
            return true;
        }
        if (filter.getFilterOp() == NFilterOp.AND) {
            return filter.getSubFilters().stream().allMatch(x -> isInstallStatusFilter((NDefinitionFilter) x));
        }
        if (filter.getFilterOp() == NFilterOp.OR) {
            return filter.getSubFilters().stream().allMatch(x -> isInstallStatusFilter((NDefinitionFilter) x));
        }
        if (filter.getFilterOp() == NFilterOp.NOT) {
            return isInstallStatusFilter((NDefinitionFilter) filter.getSubFilters().get(0));
        }
        return false;
    }

    public static NRepositoryFilter toRepositoryFilter(NDefinitionFilter filter) {
        if (filter instanceof NRepositoryFilter) {
            return (NRepositoryFilter) filter;
        }
        if (filter instanceof NInstallStatusDefinitionFilter2) {
            NInstallStatusDefinitionFilter2 d = (NInstallStatusDefinitionFilter2) filter;
            switch (d.getMode()) {
                case REQUIRED:
                case DEPLOYED:
                case INSTALLED:
                case INSTALLED_OR_REQUIRED:
                case DEFAULT_VERSION:
                    return NRepositoryFilters.of().installedRepo();
                case OBSOLETE:
                    return NRepositoryFilters.of().always();
                case NON_DEPLOYED:
                    return NRepositoryFilters.of().installedRepo().neg();
            }
            return null;
        }
        if (filter.getFilterOp() == NFilterOp.NOT) {
            return toRepositoryFilter((NDefinitionFilter) filter.getSubFilters().get(0)).neg();
        }
        if (filter.getFilterOp() == NFilterOp.AND) {
            NRepositoryFilter result = null;
            for (NFilter subFilter : filter.getSubFilters()) {
                NRepositoryFilter n = toRepositoryFilter((NDefinitionFilter) subFilter);
                if (result == null) {
                    result = n;
                } else {
                    result = result.and(n);
                }
            }
            if (result == null) {
                result = NRepositoryFilters.of().always();
            }
            return result;
        }
        if (filter.getFilterOp() == NFilterOp.OR) {
            NRepositoryFilter result = null;
            for (NFilter subFilter : filter.getSubFilters()) {
                NRepositoryFilter n = toRepositoryFilter((NDefinitionFilter) subFilter);
                if (result == null) {
                    result = n;
                } else {
                    result = result.or(n);
                }
            }
            if (result == null) {
                result = NRepositoryFilters.of().always();
            }
            return result;
        }
        return NRepositoryFilters.of().always();
    }


    public static NDefinitionFilter[] flattenAnd(NDefinitionFilter any) {
        if (any == null) {
            return new NDefinitionFilter[]{NDefinitionFilters.of().always()};
        }
        any = (NDefinitionFilter) any.simplify();
        if (any == null) {
            return new NDefinitionFilter[]{NDefinitionFilters.of().always()};
        }
        if (any instanceof NDefinitionFilterAnd) {
            return ((NDefinitionFilterAnd) any).getChildren();
        }
        return new NDefinitionFilter[]{any};
    }


    public static NDefinitionFilter replaceFilter(NDefinitionFilter parent, Function<NDefinitionFilter, NDefinitionFilter> replacer) {
        if (parent == null) {
            return null;
        }
        NDefinitionFilter n = replacer.apply(parent);
        if (n == null) {
            return null;
        }
        if (n != parent) {
            return n;
        }
        if (parent.getFilterOp() == NFilterOp.AND) {
            List<NDefinitionFilter> newList = new ArrayList<>();
            boolean someChanges = false;
            for (NFilter subFilter : parent.getSubFilters()) {
                n = replacer.apply((NDefinitionFilter) subFilter);
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
                return NDefinitionFilters.of().all(newList.toArray(new NDefinitionFilter[0]));
            }
            return parent;
        }
        return parent;
    }

    public static NDefinitionFilter addLockedIds(NDefinitionFilter parent, NId... ids) {
        if (parent == null) {
            return NDefinitionFilters.of().byLockedIds(ids);
        }
        if (ids == null) {
            return parent;
        }
        NId[] validIds = Arrays.stream(ids).filter(x -> NBlankable.isBlank(x)).collect(Collectors.toSet()).toArray(new NId[0]);
        if (ids.length == 0) {
            return parent;
        }
        NRef<Boolean> found = new NRef<>(false);
        NDefinitionFilter np = replaceFilter(parent, new NFunction<NDefinitionFilter, NDefinitionFilter>() {
            @Override
            public NDefinitionFilter apply(NDefinitionFilter old) {
                if (old instanceof NLockedIdExtensionDefinitionFilter) {
                    found.set(true);
                    return ((NLockedIdExtensionDefinitionFilter) old).addAll(ids);
                }
                return old;
            }
        });
        if (!found.get()) {
            np = np.and(NDefinitionFilters.of().byLockedIds(validIds));
        }
        return np;
    }
}
