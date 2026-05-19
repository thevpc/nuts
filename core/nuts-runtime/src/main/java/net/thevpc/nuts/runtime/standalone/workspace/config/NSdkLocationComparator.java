package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.platform.NExecutionEngineLocation;
import net.thevpc.nuts.util.NStringUtils;

import java.util.Comparator;

public class NSdkLocationComparator implements Comparator<NExecutionEngineLocation> {
    public NSdkLocationComparator() {
    }

    @Override
    public int compare(NExecutionEngineLocation o1, NExecutionEngineLocation o2) {
        NVersion v1 = NVersion.get(o1.version()).get();
        NVersion v2 = NVersion.get(o2.version()).get();
        int x = (v1 == null || v2 == null) ? 0 : (v1 != null && v2 != null) ? v1.compareTo(v2) : v2 == null ? 1 : -1;
        if (x != 0) {
            return x;
        }
        String s1 = NStringUtils.trim(o1.name());
        String s2 = NStringUtils.trim(o2.name());
        x = s1.compareTo(s2);
        if (x != 0) {
            return x;
        }
        s1 = NStringUtils.trim(o1.vendor());
        s2 = NStringUtils.trim(o2.vendor());
        x = s1.compareTo(s2);
        if (x != 0) {
            return x;
        }
        s1 = NStringUtils.trim(o1.product());
        s2 = NStringUtils.trim(o2.product());
        x = s1.compareTo(s2);
        if (x != 0) {
            return x;
        }
        s1 = NStringUtils.trim(o1.variant());
        s2 = NStringUtils.trim(o2.variant());
        x = s1.compareTo(s2);
        if (x != 0) {
            return x;
        }
        s1 = NStringUtils.trim(o1.path());
        s2 = NStringUtils.trim(o2.path());
        x = s1.compareTo(s2);
        if (x != 0) {
            return x;
        }
        return 0;
    }
}
