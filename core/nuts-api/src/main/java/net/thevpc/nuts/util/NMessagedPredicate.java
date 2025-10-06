package net.thevpc.nuts.util;

import java.util.function.Predicate;
import java.util.function.Supplier;

public interface NMessagedPredicate<T> {
    Predicate<T> filter();

    Supplier<NMsg> message();
}
