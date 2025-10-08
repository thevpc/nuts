package net.thevpc.nuts.util;

import net.thevpc.nuts.text.NMsg;

import java.util.function.Predicate;
import java.util.function.Supplier;

public interface NMessagedPredicate<T> {
    Predicate<T> filter();

    Supplier<NMsg> message();
}
