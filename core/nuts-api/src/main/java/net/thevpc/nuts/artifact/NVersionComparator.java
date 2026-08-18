package net.thevpc.nuts.artifact;

import net.thevpc.nuts.internal.NReservedMavenVersionComparator;
import net.thevpc.nuts.internal.NReservedNVersionComparators;
import net.thevpc.nuts.concurrent.NCallable;

import java.util.Comparator;

public interface NVersionComparator extends Comparator<NVersion> {
    static NVersionComparator of() {
        return NReservedNVersionComparators.SCOPED_VERSION_COMPARATOR.get();
    }

    static NVersionComparator ofMaven() {
        return NReservedMavenVersionComparator.INSTANCE;
    }
    static void runWith(NVersionComparator value, Runnable r) {
        NReservedNVersionComparators.SCOPED_VERSION_COMPARATOR.runWith(value,r);
    }

    static <V> V callWith(NVersionComparator value, NCallable<V> r) {
        return NReservedNVersionComparators.SCOPED_VERSION_COMPARATOR.callWith(value,r);
    }
}
