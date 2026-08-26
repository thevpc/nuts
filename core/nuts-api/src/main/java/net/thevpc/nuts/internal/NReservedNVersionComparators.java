package net.thevpc.nuts.internal;

import net.thevpc.nuts.concurrent.NScopedValue;
import net.thevpc.nuts.artifact.NVersionComparator;

/**
 * NReservedNVersionComparators class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NReservedNVersionComparators {
    public static NScopedValue<NVersionComparator> SCOPED_VERSION_COMPARATOR = NScopedValue.ofSupplier(NVersionComparator::ofMaven);
}
