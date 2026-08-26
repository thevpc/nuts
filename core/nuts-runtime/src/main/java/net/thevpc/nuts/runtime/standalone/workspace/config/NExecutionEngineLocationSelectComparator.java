package net.thevpc.nuts.runtime.standalone.workspace.config;

import net.thevpc.nuts.artifact.NVersion;
import net.thevpc.nuts.platform.NExecutionEngineLocation;
import net.thevpc.nuts.util.NStringUtils;

import java.util.Comparator;

public class NExecutionEngineLocationSelectComparator implements Comparator<NExecutionEngineLocation> {
    public NExecutionEngineLocationSelectComparator() {
    }

    @Override
    public int compare(NExecutionEngineLocation o1, NExecutionEngineLocation o2) {
        int x=o1.id().compareTo(o2.id());
        if(x!=0){
            return x;
        }
        x=Integer.compare(o2.priority(),o1.priority());
        if(x!=0){
            return x;
        }
        NVersion v1 = NVersion.get(o1.version()).get();
        NVersion v2 = NVersion.get(o2.version()).get();
        x = (v1 == null || v2 == null) ? 0 : (v1 != null && v2 != null) ? v2.compareTo(v1) : v2 == null ? -1 : 1;
        if (x != 0) {
            return x;
        }
        String s1 = NStringUtils.strip(o1.name());
        String s2 = NStringUtils.strip(o2.name());
        x = s1.compareTo(s2);
        if (x != 0) {
            return x;
        }
        s1 = NStringUtils.strip(o1.vendor());
        s2 = NStringUtils.strip(o2.vendor());
        x = s1.compareTo(s2);
        if (x != 0) {
            return x;
        }
        s1 = NStringUtils.strip(o1.product());
        s2 = NStringUtils.strip(o2.product());
        x = s1.compareTo(s2);
        if (x != 0) {
            return x;
        }
        s1 = NStringUtils.strip(o1.variant());
        s2 = NStringUtils.strip(o2.variant());
        x = s1.compareTo(s2);
        if (x != 0) {
            return x;
        }
        s1 = NStringUtils.strip(o1.path());
        s2 = NStringUtils.strip(o2.path());
        x = s1.compareTo(s2);
        if (x != 0) {
            return x;
        }
        return 0;
    }
}
