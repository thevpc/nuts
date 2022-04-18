package net.thevpc.nuts.boot;

import net.thevpc.nuts.*;

import java.util.List;
import java.util.TreeSet;

public class PrivateNutsUtilDescriptors {
    public static String toExclusionListString(List<NutsId> exclusions) {
        TreeSet<String> ex = new TreeSet<>();
        for (NutsId exclusion : exclusions) {
            ex.add(exclusion.getShortName());
        }
        return String.join(",", ex);
    }

    public static boolean isDefaultScope(String s1) {
        return NutsDependencyScope.parse(s1).orElse(NutsDependencyScope.API) == NutsDependencyScope.API;
    }

    public static boolean accept(NutsVersionInterval one, NutsVersion other) {
        NutsVersion a = NutsVersion.of(one.getLowerBound()).get();
        if (a.isBlank()) {
            //ok
        } else {
            int c = a.compareTo(other);
            if (one.isIncludeLowerBound()) {
                if (c > 0) {
                    return false;
                }
            } else {
                if (c >= 0) {
                    return false;
                }
            }
        }
        a = NutsVersion.of(one.getUpperBound()).get();
        if (a.isBlank()) {
            //ok
        } else {
            int c = a.compareTo(other);
            if (one.isIncludeUpperBound()) {
                return c >= 0;
            } else {
                return c > 0;
            }
        }
        return true;
    }

    public static boolean accept(NutsVersion one, NutsVersion other) {
        if (!other.isSingleValue()) {
            throw new NutsBootException(NutsMessage.cstyle("expected single value version: %s", other));
        }
        List<NutsVersionInterval> ii = one.intervals().get();
        if (ii.isEmpty()) {
            return true;
        }
        for (NutsVersionInterval i : ii) {
            if (accept(i, other)) {
                return true;
            }
        }
        return false;
    }
}
