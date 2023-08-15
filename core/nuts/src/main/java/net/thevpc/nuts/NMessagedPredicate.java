package net.thevpc.nuts;

import net.thevpc.nuts.util.NMsg;

import java.util.function.Function;
import java.util.function.Predicate;

public interface NMessagedPredicate<T> {
    Predicate<T> filter();

    Function<NSession, NMsg> message();
}
