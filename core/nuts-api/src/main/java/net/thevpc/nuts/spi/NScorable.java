package net.thevpc.nuts.spi;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.text.NMsg;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NStream;

import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface NScorable {
    /**
     * minimum support level for user defined implementations.
     */
    public static final int CUSTOM_SCORE = 1000;
    /**
     * this is the default support level for runtime implementation (nuts-runtime).
     */
    public static final int DEFAULT_SCORE = 10;
    /**
     * when getScore(...)==NO_SUPPORT the package is discarded.
     */
    public static final int UNSUPPORTED_SCORE = -1;

    static boolean isValidScore(int score) {
        return score > 0;
    }

    int getScore(NScorableContext context);

    interface Result<T extends NScorable> {
        T value();

        int score();

        NScorableContext context();
    }

    interface Query<T extends NScorable> {
        Query<T> fromSupplierOfStream(Stream<Supplier<T>> source);

        Query<T> fromStream(Stream<T> source);

        Query<T> fromSupplierOfStream(NStream<Supplier<T>> source);

        Query<T> fromStream(NStream<T> source);

        Query<T> fromIterable(Iterable<T> source);

        Query<T> fromIterator(Iterator<T> source);

        Query<T> fromIterableOfSupplier(Iterable<Supplier<T>> source);

        Query<T> fromIteratorOrSupplier(Iterator<Supplier<T>> source);

        Query<T> withName(NMsg source);

        Query<T> withName(Supplier<NMsg> source);
        Query<T> withContext(NScorableContext context);
        Query<T> withEmptyMessage(Supplier<NMsg> source);

        List<T> getAll();

        NOptional<T> getBest();

        NOptional<Result<T>> getBestResult();
    }

    static <T extends NScorable> Query<T> query() {
        return NExtensions.of().createComponent(NUtilSPI.class).get().ofScorableQuery();
    }

}
