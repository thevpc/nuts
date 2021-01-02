package net.thevpc.nuts.runtime.depracated;

import net.thevpc.nuts.*;

import java.util.Comparator;

public class SimpleNutsDescriptorComparator implements Comparator<NutsDescriptor> {
    private NutsWorkspace ws;

    public SimpleNutsDescriptorComparator(NutsWorkspace ws) {
        this.ws = ws;
    }

    @Override
    public int compare(NutsDescriptor o1, NutsDescriptor o2) {
        //most specific first
        return Integer.compare(weight(o2), weight(o1));
    }

    private int weight(NutsDescriptor desc) {
        int x = 1;
        x *= weight(desc.getArch());
        x *= weight(desc.getOs());
        x *= weight(desc.getOsdist());
        x *= weight(desc.getPlatform());
        return x;
    }

    private int weight(String[] desc) {
        int x = 1;
        for (String s : desc) {
            x += weight(ws.dependency().parser().parseDependency(s));
        }
        return x;
    }

    private int weight(NutsDependency desc) {
        return weight(desc.getVersion());
    }

    private int weight(NutsVersion desc) {
        int x = 1;
        for (NutsVersionInterval s : desc.intervals()) {
            x *= weight(s);
        }
        return x;
    }

    private int weight(NutsVersionInterval desc) {
        return desc.isFixedValue() ? 2 : 3;
    }
}
