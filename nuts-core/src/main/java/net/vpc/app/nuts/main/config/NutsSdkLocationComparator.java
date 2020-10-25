package net.vpc.app.nuts.main.config;

import net.vpc.app.nuts.NutsSdkLocation;
import net.vpc.app.nuts.NutsVersion;
import net.vpc.app.nuts.NutsWorkspace;
import net.vpc.app.nuts.runtime.util.common.CoreStringUtils;

import java.util.Comparator;

public class NutsSdkLocationComparator implements Comparator<NutsSdkLocation> {
    private NutsWorkspace ws;
    public NutsSdkLocationComparator(NutsWorkspace ws) {
        this.ws=ws;
    }

    @Override
    public int compare(NutsSdkLocation o1, NutsSdkLocation o2) {
        NutsVersion v1 = ws.version().parser().parse(o1.getVersion());
        NutsVersion v2 = ws.version().parser().parse(o2.getVersion());
        int x = (v1 == null || v2 == null) ? 0 : (v1 != null && v2 != null) ? v1.compareTo(v2) : v2 == null ? 1 : -1;
        if (x != 0) {
            return x;
        }
        String s1 = CoreStringUtils.trim(o1.getName());
        String s2 = CoreStringUtils.trim(o2.getName());
        x = s1.compareTo(s2);
        if (x != 0) {
            return x;
        }
        s1 = CoreStringUtils.trim(o1.getPackaging());
        s2 = CoreStringUtils.trim(o2.getPackaging());
        x = s1.compareTo(s2);
        if (x != 0) {
            return x;
        }
        s1 = CoreStringUtils.trim(o1.getProduct());
        s2 = CoreStringUtils.trim(o2.getProduct());
        x = s1.compareTo(s2);
        if (x != 0) {
            return x;
        }
        s1 = CoreStringUtils.trim(o1.getPath());
        s2 = CoreStringUtils.trim(o2.getPath());
        x = s1.compareTo(s2);
        if (x != 0) {
            return x;
        }
        return 0;
    }
}
