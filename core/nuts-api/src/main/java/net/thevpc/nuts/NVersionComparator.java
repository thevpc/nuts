package net.thevpc.nuts;

import net.thevpc.nuts.reserved.MavenNVersionComparator;
import net.thevpc.nuts.reserved.ReservedNVersionComparators;
import net.thevpc.nuts.util.NCallable;

import java.util.Comparator;

public interface NVersionComparator extends Comparator<NVersion> {
    static NVersionComparator of() {
        return ReservedNVersionComparators.SCOPED_VERSION_COMPARATOR.get();
    }

    static NVersionComparator ofMaven() {
        return MavenNVersionComparator.INSTANCE;
    }
    static void runWith(NVersionComparator value, Runnable r) {
        ReservedNVersionComparators.SCOPED_VERSION_COMPARATOR.runWith(value,r);
    }

    static <V> V callWith(NVersionComparator value, NCallable<V> r) {
        return ReservedNVersionComparators.SCOPED_VERSION_COMPARATOR.callWith(value,r);
    }
}
