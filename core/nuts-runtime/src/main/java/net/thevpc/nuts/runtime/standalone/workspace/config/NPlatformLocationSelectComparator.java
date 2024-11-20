package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.*;
import net.thevpc.nuts.util.NStringUtils;

import java.util.Comparator;

public class NPlatformLocationSelectComparator implements Comparator<NPlatformLocation> {
    private NSession session;
    public NPlatformLocationSelectComparator(NSession session) {
        this.session = session;
    }

    @Override
    public int compare(NPlatformLocation o1, NPlatformLocation o2) {
        int x=o1.getId().compareTo(o2.getId());
        if(x!=0){
            return x;
        }
        x=Integer.compare(o2.getPriority(),o1.getPriority());
        if(x!=0){
            return x;
        }
        NVersion v1 = NVersion.of(o1.getVersion()).get();
        NVersion v2 = NVersion.of(o2.getVersion()).get();
        x = (v1 == null || v2 == null) ? 0 : (v1 != null && v2 != null) ? v2.compareTo(v1) : v2 == null ? -1 : 1;
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
