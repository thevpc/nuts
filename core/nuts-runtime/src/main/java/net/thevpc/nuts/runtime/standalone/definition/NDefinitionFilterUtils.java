package net.thevpc.nuts.runtime.standalone.definition;

import net.thevpc.nuts.NDefinitionFilter;
import net.thevpc.nuts.NDefinitionFilters;
import net.thevpc.nuts.NId;
import net.thevpc.nuts.runtime.standalone.definition.filter.NDefinitionFilterAnd;
import net.thevpc.nuts.runtime.standalone.definition.filter.NDefinitionFilterOr;
import net.thevpc.nuts.runtime.standalone.definition.filter.NInstallStatusDefinitionFilter2;
import net.thevpc.nuts.runtime.standalone.definition.filter.NPatternDefinitionFilter;
import net.thevpc.nuts.util.NFilterOp;
import net.thevpc.nuts.util.NOptional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    public static NOptional<Boolean> resolveInstalled(NDefinitionFilter any) {
        for (NDefinitionFilter d : flattenAnd(any)) {
            if (d instanceof NInstallStatusDefinitionFilter2) {
                NInstallStatusDefinitionFilter2 v = (NInstallStatusDefinitionFilter2) d;
                switch (v.getName()) {
                    case "installed": {
                        return NOptional.of(v.isValue());
                    }
                }
            }
        }
        return NOptional.ofNamedEmpty("installed");
    }

    public static NOptional<Boolean> resolveRequired(NDefinitionFilter any) {
        for (NDefinitionFilter d : flattenAnd(any)) {
            if (d instanceof NInstallStatusDefinitionFilter2) {
                NInstallStatusDefinitionFilter2 v = (NInstallStatusDefinitionFilter2) d;
                switch (v.getName()) {
                    case "required": {
                        return NOptional.of(v.isValue());
                    }
                }
            }
        }
        return NOptional.ofNamedEmpty("required");
    }

    public static NOptional<Boolean> resolveDeployed(NDefinitionFilter any) {
        for (NDefinitionFilter d : flattenAnd(any)) {
            if (d instanceof NInstallStatusDefinitionFilter2) {
                NInstallStatusDefinitionFilter2 v = (NInstallStatusDefinitionFilter2) d;
                switch (v.getName()) {
                    case "deployed": {
                        return NOptional.of(v.isValue());
                    }
                }
            }
        }
        return NOptional.ofNamedEmpty("deployed");
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




}
