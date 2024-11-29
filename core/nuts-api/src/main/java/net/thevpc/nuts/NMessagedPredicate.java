package net.thevpc.nuts;

import net.thevpc.nuts.util.NMsg;

import java.util.function.Predicate;
import java.util.function.Supplier;

public interface NMessagedPredicate<T> {
    Predicate<T> filter();

    Supplier<NMsg> message();
}
