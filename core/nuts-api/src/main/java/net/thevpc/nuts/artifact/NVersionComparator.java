package net.thevpc.nuts.artifact;

import net.thevpc.nuts.internal.MavenNVersionComparator;
import net.thevpc.nuts.internal.ReservedNVersionComparators;
import net.thevpc.nuts.concurrent.NCallable;

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
