package net.vpc.app.nuts.util;

import java.util.LinkedHashSet;

public class CollectionUtils {
    public static String[] toArraySet(String[] values) {
        LinkedHashSet<String> set = new LinkedHashSet<>();
        if (values != null) {
            for (String a : values) {
                a = StringUtils.trim(a);
                if (!StringUtils.isEmpty(a) && !set.contains(a)) {
                    set.add(a);
                }
            }
        }
        return set.toArray(new String[set.size()]);
    }
}
