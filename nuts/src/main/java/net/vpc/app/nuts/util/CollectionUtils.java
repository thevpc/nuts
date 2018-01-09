package net.vpc.app.nuts.util;

import java.util.LinkedHashSet;
import java.util.Set;

public class CollectionUtils {
    public static Set<String> toSet(String[] values0) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        if (values0 != null) {
            for (String a : values0) {
                a = StringUtils.trim(a);
                if (!StringUtils.isEmpty(a) && !set.contains(a)) {
                    set.add(a);
                }
            }
        }
        return set;
    }

    public static String[] toArraySet(String[] values0, String[]... values) {
        Set<String> set = toSet(values0);
        if (values != null) {
            for (String[] value : values) {
                set.addAll(toSet(value));
            }
        }
        return set.toArray(new String[set.size()]);
    }
}
