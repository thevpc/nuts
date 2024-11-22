package net.thevpc.nuts.util;

import java.util.Collection;

/**
 * Created by vpc on 8/15/14.
 */
public interface NCollectionFilter<A> {

    boolean accept(A a, int baseIndex, Collection<A> list);
}
