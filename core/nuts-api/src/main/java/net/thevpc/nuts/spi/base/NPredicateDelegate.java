package net.thevpc.nuts.spi.base;

import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.reserved.util.NPredicateWithDescription;
import net.thevpc.nuts.util.NPredicate;

import java.util.function.Supplier;

public abstract class NPredicateDelegate<T> extends AbstractNPredicate<T> {
    public abstract NPredicate<T> basePredicate();

    @Override
    public boolean test(T t) {
        return basePredicate().test(t);
    }

    @Override
    public NElement describe() {
        return basePredicate().describe();
    }

    @Override
    public NPredicate<T> redescribe(Supplier<NElement> description) {
        if(description==null){
            return this;
        }
        return new NPredicateWithDescription<>(this,description);
    }
}
