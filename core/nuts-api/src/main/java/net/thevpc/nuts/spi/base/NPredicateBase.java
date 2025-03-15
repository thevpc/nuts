package net.thevpc.nuts.spi.base;

import net.thevpc.nuts.elem.NEDesc;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;

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
        return NEDesc.describeResolveOr(base, () -> NElements.of().ofObjectBuilder().build())
                .asObject().get();
    }
}
