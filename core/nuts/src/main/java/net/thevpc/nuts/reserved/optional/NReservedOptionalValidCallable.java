package net.thevpc.nuts.reserved.optional;

import java.util.Objects;
import java.util.function.Function;
import net.thevpc.nuts.NSession;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NCallable;
import net.thevpc.nuts.util.NOptional;
import static net.thevpc.nuts.util.NOptionalType.EMPTY;
import static net.thevpc.nuts.util.NOptionalType.ERROR;
import static net.thevpc.nuts.util.NOptionalType.PRESENT;

public class NReservedOptionalValidCallable<T> extends NReservedOptionalValid<T> implements Cloneable {

    private final NCallable<T> value;
    private T result;
    private boolean evaluated;

    public NReservedOptionalValidCallable(NCallable<T> value) {
        NAssert.requireNonNull(value, "callable");
        this.value = value;
    }

    @Override
    public <V> NOptional<V> then(Function<T, V> mapper) {
        Objects.requireNonNull(mapper);
        try {
            T y = get();
            if (y != null) {
                try {
                    return NOptional.of(mapper.apply(y));
                } catch (Exception ex) {
                    return NOptional.ofError(getMessage(), ex);
                }
            } else {
                return NOptional.ofEmpty(getMessage());
            }
        } catch (Exception ex) {
            return NOptional.ofError(getMessage(), ex);
        }
    }

    @Override
    public T get(NSession session) {
        if(!evaluated){
            result=value.call(session);
            evaluated=true;
        }
        return result;
    }
}
