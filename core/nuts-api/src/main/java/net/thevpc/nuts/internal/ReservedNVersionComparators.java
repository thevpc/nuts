package net.thevpc.nuts.internal;

import net.thevpc.nuts.concurrent.NScopedValue;
import net.thevpc.nuts.artifact.NVersionComparator;

public class ReservedNVersionComparators {
    public static NScopedValue<NVersionComparator> SCOPED_VERSION_COMPARATOR = new NScopedValue<>(NVersionComparator::ofMaven);
}
