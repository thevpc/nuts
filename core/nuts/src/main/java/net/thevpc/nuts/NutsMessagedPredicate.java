package net.thevpc.nuts;

import java.util.function.Function;
import java.util.function.Predicate;

public interface NutsMessagedPredicate<T> {
    Predicate<T> filter();

    Function<NutsSession, NutsMessage> message();
}
