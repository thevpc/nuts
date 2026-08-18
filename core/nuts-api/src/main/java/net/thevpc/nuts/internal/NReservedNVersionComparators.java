package net.thevpc.nuts.internal;

import net.thevpc.nuts.concurrent.NScopedValue;
import net.thevpc.nuts.artifact.NVersionComparator;

public class NReservedNVersionComparators {
    public static NScopedValue<NVersionComparator> SCOPED_VERSION_COMPARATOR = NScopedValue.ofSupplier(NVersionComparator::ofMaven);
}
