package net.thevpc.nuts.runtime.standalone.id;

import net.thevpc.nuts.NId;

import java.util.Comparator;

public class DefaultNIdComparator implements Comparator<NId> {

    public static final DefaultNIdComparator INSTANCE = new DefaultNIdComparator();

    @Override
    public int compare(NId o1, NId o2) {
        int x = o1.getShortName().compareTo(o2.getShortName());
        if (x != 0) {
            return x;
        }
        //latest versions first
        x = o1.getVersion().compareTo(o2.getVersion());
        return -x;
    }
}
