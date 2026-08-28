package net.thevpc.nuts.spi.base;

import net.thevpc.nuts.elem.NDescribables;
import net.thevpc.nuts.elem.NElement;

import java.util.function.Predicate;

/**
 * NPredicateBase class.
 *
 * @author thevpc
 * @since 0.8.0
 */
public class NPredicateImpl<T> extends NPredicateBase<T> {
    private final Predicate<T> base;
    private final NElement description;
    /**
     * N predicate base.
     *
     * @param base base
     * @param description description
     * @return n predicate base result
     */
    public NPredicateImpl(Predicate<T> base, NElement description) {
        this.base = base;
        this.description = description;
    }

    @Override
    public boolean test(T t) {
        return base.test(t);
    }

    @Override
    public String toString() {
        return "NamedPredicate";
    }

    @Override
    public NElement describe() {
        return description==null? NDescribables.describeResolveOr(base, () -> NElement.ofObjectBuilder().build())
                .asObject().get():description;
    }
}
