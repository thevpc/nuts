package net.vpc.app.nuts.core;

import net.vpc.app.nuts.NutsId;

import java.util.Comparator;

public class DefaultNutsIdComparator implements Comparator<NutsId> {

    public static final DefaultNutsIdComparator INSTANCE = new DefaultNutsIdComparator();

    @Override
    public int compare(NutsId o1, NutsId o2) {
        int x = o1.getSimpleName().compareTo(o2.getSimpleName());
        if (x != 0) {
            return x;
        }
        //latests versions first
        x = o1.getVersion().compareTo(o2.getVersion());
        return -x;
    }
}
