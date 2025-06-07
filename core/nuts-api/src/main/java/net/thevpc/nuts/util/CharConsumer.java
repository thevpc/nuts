package net.thevpc.nuts.util;

import java.util.Objects;

public interface CharConsumer {

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    void accept(char t);

    /**
     * Returns a composed {@code Consumer} that performs, in sequence, this
     * operation followed by the {@code after} operation. If performing either
     * operation throws an exception, it is relayed to the caller of the
     * composed operation.  If performing this operation throws an exception,
     * the {@code after} operation will not be performed.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code Consumer} that performs in sequence this
     * operation followed by the {@code after} operation
     * @throws NullPointerException if {@code after} is null
     */
    default CharConsumer andThen(CharConsumer after) {
        Objects.requireNonNull(after);
        return (char t) -> {
            accept(t);
            after.accept(t);
        };
    }
}
