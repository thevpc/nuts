package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;

import java.util.Comparator;

public class NutsPlatformLocationSelectComparator implements Comparator<NutsPlatformLocation> {
    private NutsSession ws;
    public NutsPlatformLocationSelectComparator(NutsSession ws) {
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
        NutsVersion v1 = NutsVersion.of(o1.getVersion(),ws);
        NutsVersion v2 = NutsVersion.of(o2.getVersion(),ws);
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
