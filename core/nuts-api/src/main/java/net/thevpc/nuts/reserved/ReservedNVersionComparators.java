package net.thevpc.nuts.reserved;

import net.thevpc.nuts.NScopedValue;
import net.thevpc.nuts.NVersionComparator;

public class ReservedNVersionComparators {
    public static NScopedValue<NVersionComparator> SCOPED_VERSION_COMPARATOR = new NScopedValue<>(NVersionComparator::ofMaven);
}
