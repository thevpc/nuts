package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NutsStringUtils;

import java.util.Comparator;

public class NutsSdkLocationComparator implements Comparator<NutsPlatformLocation> {
    private NutsSession session;
    public NutsSdkLocationComparator(NutsSession session) {
        this.session =session;
    }

    @Override
    public int compare(NutsPlatformLocation o1, NutsPlatformLocation o2) {
        NutsVersion v1 = NutsVersion.of(o1.getVersion()).get(session);
        NutsVersion v2 = NutsVersion.of(o2.getVersion()).get(session);
        int x = (v1 == null || v2 == null) ? 0 : (v1 != null && v2 != null) ? v1.compareTo(v2) : v2 == null ? 1 : -1;
        if (x != 0) {
            return x;
        }
        String s1 = NutsStringUtils.trim(o1.getName());
        String s2 = NutsStringUtils.trim(o2.getName());
        x = s1.compareTo(s2);
        if (x != 0) {
            return x;
        }
        s1 = NutsStringUtils.trim(o1.getPackaging());
        s2 = NutsStringUtils.trim(o2.getPackaging());
        x = s1.compareTo(s2);
        if (x != 0) {
            return x;
        }
        s1 = NutsStringUtils.trim(o1.getProduct());
        s2 = NutsStringUtils.trim(o2.getProduct());
        x = s1.compareTo(s2);
        if (x != 0) {
            return x;
        }
        s1 = NutsStringUtils.trim(o1.getPath());
        s2 = NutsStringUtils.trim(o2.getPath());
        x = s1.compareTo(s2);
        if (x != 0) {
            return x;
        }
        return 0;
    }
}
