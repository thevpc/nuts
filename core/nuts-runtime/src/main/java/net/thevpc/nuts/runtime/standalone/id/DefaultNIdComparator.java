package net.thevpc.nuts.runtime.standalone.id;

import net.thevpc.nuts.artifact.NId;

import java.util.Comparator;

public class DefaultNIdComparator implements Comparator<NId> {

    public static final DefaultNIdComparator INSTANCE = new DefaultNIdComparator();

    @Override
    public int compare(NId o1, NId o2) {
        int x = o1.shortName().compareTo(o2.shortName());
        if (x != 0) {
            return x;
        }
        //latest versions first
        x = o1.version().compareTo(o2.version());
        return -x;
    }
}
