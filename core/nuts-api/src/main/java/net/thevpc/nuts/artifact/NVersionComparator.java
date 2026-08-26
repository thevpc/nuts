package net.thevpc.nuts.artifact;

import net.thevpc.nuts.internal.NReservedMavenVersionComparator;
import net.thevpc.nuts.internal.NReservedNVersionComparators;
import net.thevpc.nuts.concurrent.NCallable;

import java.util.Comparator;

/**
 * NVersionComparator interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NVersionComparator extends Comparator<NVersion> {
    /**
     * Creates a new instance of of.
     *
     * @return of result
     */
    static NVersionComparator of() {
        return NReservedNVersionComparators.SCOPED_VERSION_COMPARATOR.get();
    }

    /**
     * Creates a new instance of of maven.
     *
     * @return of maven result
     */
    static NVersionComparator ofMaven() {
        return NReservedMavenVersionComparator.INSTANCE;
    }
    /**
     * Run with.
     *
     * @param value value
     * @param r r
     */
    static void runWith(NVersionComparator value, Runnable r) {
        NReservedNVersionComparators.SCOPED_VERSION_COMPARATOR.runWith(value,r);
    }

    /**
     * Call with.
     *
     * @param value value
     * @param r r
     * @return call with result
     */
    static <V> V callWith(NVersionComparator value, NCallable<V> r) {
        return NReservedNVersionComparators.SCOPED_VERSION_COMPARATOR.callWith(value,r);
    }
}
