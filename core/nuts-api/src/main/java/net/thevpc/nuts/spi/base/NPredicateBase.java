package net.thevpc.nuts.spi.base;

import net.thevpc.nuts.elem.NDescribableElementSupplier;
import net.thevpc.nuts.elem.NElement;

import java.util.function.Predicate;

public class NPredicateBase<T> extends AbstractNPredicate<T> {
    private final Predicate<T> base;
    public NPredicateBase(Predicate<T> base) {
        this.base = base;
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
        return NDescribableElementSupplier.describeResolveOr(base, () -> NElement.ofObjectBuilder().build())
                .asObject().get();
    }
}
