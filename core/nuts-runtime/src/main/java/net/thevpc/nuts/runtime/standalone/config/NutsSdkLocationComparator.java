package net.thevpc.nuts.runtime.standalone.config;

import net.thevpc.nuts.NutsSdkLocation;
import net.thevpc.nuts.NutsUtilStrings;
import net.thevpc.nuts.NutsVersion;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.runtime.core.util.CoreStringUtils;

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
        String s1 = NutsUtilStrings.trim(o1.getName());
        String s2 = NutsUtilStrings.trim(o2.getName());
        x = s1.compareTo(s2);
        if (x != 0) {
            return x;
        }
        s1 = NutsUtilStrings.trim(o1.getPackaging());
        s2 = NutsUtilStrings.trim(o2.getPackaging());
        x = s1.compareTo(s2);
        if (x != 0) {
            return x;
        }
        s1 = NutsUtilStrings.trim(o1.getProduct());
        s2 = NutsUtilStrings.trim(o2.getProduct());
        x = s1.compareTo(s2);
        if (x != 0) {
            return x;
        }
        s1 = NutsUtilStrings.trim(o1.getPath());
        s2 = NutsUtilStrings.trim(o2.getPath());
        x = s1.compareTo(s2);
        if (x != 0) {
            return x;
        }
        return 0;
    }
}
