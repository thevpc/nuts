package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NStringUtils;

import java.util.Comparator;

public class NSdkLocationComparator implements Comparator<NPlatformLocation> {
    public NSdkLocationComparator() {
    }

    @Override
    public int compare(NPlatformLocation o1, NPlatformLocation o2) {
        NVersion v1 = NVersion.get(o1.getVersion()).get();
        NVersion v2 = NVersion.get(o2.getVersion()).get();
        int x = (v1 == null || v2 == null) ? 0 : (v1 != null && v2 != null) ? v1.compareTo(v2) : v2 == null ? 1 : -1;
        if (x != 0) {
            return x;
        }
        String s1 = NStringUtils.trim(o1.getName());
        String s2 = NStringUtils.trim(o2.getName());
        x = s1.compareTo(s2);
        if (x != 0) {
            return x;
        }
        s1 = NStringUtils.trim(o1.getPackaging());
        s2 = NStringUtils.trim(o2.getPackaging());
        x = s1.compareTo(s2);
        if (x != 0) {
            return x;
        }
        s1 = NStringUtils.trim(o1.getProduct());
        s2 = NStringUtils.trim(o2.getProduct());
        x = s1.compareTo(s2);
        if (x != 0) {
            return x;
        }
        s1 = NStringUtils.trim(o1.getPath());
        s2 = NStringUtils.trim(o2.getPath());
        x = s1.compareTo(s2);
        if (x != 0) {
            return x;
        }
        return 0;
    }
}
