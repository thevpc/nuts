package net.thevpc.nuts.collections;

import java.util.Collection;

/**
 * Created by vpc on 8/15/14.
 */
public interface NCollectionFilter<A> {

    /**
     * Accept.
     *
     * @param a a
     * @param baseIndex base index
     * @param list list
     * @return accept result
     */
    boolean accept(A a, int baseIndex, Collection<A> list);
}
