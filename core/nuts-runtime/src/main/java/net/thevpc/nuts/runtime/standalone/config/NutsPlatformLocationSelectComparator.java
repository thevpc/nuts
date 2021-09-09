package net.thevpc.nuts.runtime.standalone.config;

import net.thevpc.nuts.NutsPlatformLocation;
import net.thevpc.nuts.NutsUtilStrings;
import net.thevpc.nuts.NutsVersion;
import net.thevpc.nuts.NutsWorkspace;

import java.util.Comparator;

public class NutsPlatformLocationSelectComparator implements Comparator<NutsPlatformLocation> {
    private NutsWorkspace ws;
    public NutsPlatformLocationSelectComparator(NutsWorkspace ws) {
        this.ws=ws;
    }

    @Override
    public int compare(NutsPlatformLocation o1, NutsPlatformLocation o2) {
        int x=o1.getId().compareTo(o2.getId());
        if(x!=0){
            return x;
        }
        x=Integer.compare(o2.getPriority(),o1.getPriority());
        if(x!=0){
            return x;
        }
        NutsVersion v1 = ws.version().parser().parse(o1.getVersion());
        NutsVersion v2 = ws.version().parser().parse(o2.getVersion());
        x = (v1 == null || v2 == null) ? 0 : (v1 != null && v2 != null) ? v2.compareTo(v1) : v2 == null ? -1 : 1;
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
