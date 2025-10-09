package net.thevpc.nuts.util;

import net.thevpc.nuts.text.NMsg;

import java.util.Iterator;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * Represents a query builder for selecting {@link NScorable} instances based on their score.
 * <p>
 * This interface provides a fluent API to specify sources of scorable instances, optional
 * context, names, and messages. After specifying the sources and context, the query can
 * evaluate the scores and return the best instance or all candidates.
 * </p>
 *
 * <p><b>Usage example:</b></p>
 * <pre>{@code
 * NScorableContext context = NScorableContext.of(myCriteria);
 * NOptional<MyScorable> best = NScorable.<MyScorable>query(context)
 *                                      .fromStream(myStreamOfScorables)
 *                                      .withName(NMsg.ofC("Finding best scorer"))
 *                                      .getBest();
 * if(best.isPresent()) {
 *     MyScorable selected = best.get();
 *     // use selected instance
 * }
 * }</pre>
 *
 * <p>
 * Multiple calls to any {@code from*} method will <b>add</b> candidates to the query
 * rather than replacing previous ones. When {@link #getBest()} or {@link #getBestResult()}
 * is called, all accumulated candidates are evaluated according to their score.
 * </p>
 *
 * @param <T> the type of scorable instances
 */
public interface NScorableQuery<T extends NScorable> {
    /**
     * Sets the source of scorable instances as a {@link Stream} of {@link Supplier}s.
     *
     * @param source the source stream of suppliers
     * @return this query instance
     */
    NScorableQuery<T> fromStreamOfSuppliers(Stream<Supplier<T>> source);

    /**
     * Sets a single scorable instance as the source.
     *
     * @param source the scorable instance
     * @return this query instance
     */
    NScorableQuery<T> fromSingleton(T source);

    /**
     * Sets a single scorable instance supplier as the source.
     *
     * @param source the supplier of the scorable instance
     * @return this query instance
     */
    NScorableQuery<T> fromSingletonSupplier(Supplier<T> source);


    /**
     * Sets the source of scorable instances as a {@link Stream}.
     *
     * @param source the stream of scorable instances
     * @return this query instance
     */
    NScorableQuery<T> fromStream(Stream<T> source);

    /**
     * Sets the source as an {@link NStream} of {@link Supplier}s.
     *
     * @param source the NStream of suppliers
     * @return this query instance
     */
    NScorableQuery<T> fromStreamOfSuppliers(NStream<Supplier<T>> source);

    /**
     * Sets the source as an {@link NStream}.
     *
     * @param source the NStream of scorable instances
     * @return this query instance
     */
    NScorableQuery<T> fromStream(NStream<T> source);

    /**
     * Sets the source of scorable instances as an {@link Iterable}.
     *
     * @param source the iterable of scorable instances
     * @return this query instance
     */
    NScorableQuery<T> fromIterable(Iterable<T> source);


    /**
     * Sets the source as an {@link Iterator}.
     *
     * @param source the iterator of scorable instances
     * @return this query instance
     */
    NScorableQuery<T> fromIterator(Iterator<T> source);

    /**
     * Sets the source of scorable instances as an {@link Iterable} of {@link Supplier}s.
     *
     * @param source the iterable of suppliers
     * @return this query instance
     */
    NScorableQuery<T> fromIterableOfSuppliers(Iterable<Supplier<T>> source);

    /**
     * Sets the source of scorable instances as an {@link Iterator} of {@link Supplier}s.
     *
     * @param source the iterator of suppliers
     * @return this query instance
     */
    NScorableQuery<T> fromIteratorOfSuppliers(Iterator<Supplier<T>> source);

    /**
     * Sets an optional descriptive name for the query, used in error messages or logging.
     *
     * @param source the descriptive message
     * @return this query instance
     */
    NScorableQuery<T> withName(NMsg source);

    /**
     * Sets an optional descriptive name supplier for the query.
     *
     * @param source the supplier of the descriptive message
     * @return this query instance
     */
    NScorableQuery<T> withName(Supplier<NMsg> source);

    /**
     * Sets the {@link NScorableContext} to use when evaluating scores.
     *
     * @param context the scorable context
     * @return this query instance
     */
    NScorableQuery<T> withContext(NScorableContext context);


    /**
     * Sets a supplier of an empty message to use if no valid scorable instance is found.
     *
     * @param source the supplier of the empty message
     * @return this query instance
     */
    NScorableQuery<T> withEmptyMessage(Supplier<NMsg> source);

    /**
     * Returns all accumulated scorable instances that have a valid score.
     * <p>
     * An instance is considered valid if {@code getScore(context) > 0}.
     * </p>
     *
     * @return list of valid scorable instances
     */
    List<T> getAll();

    /**
     * Returns the best scorable instance according to its score and the provided context.
     *
     * @return an {@link NOptional} wrapping the best instance, or empty if none are valid
     */
    NOptional<T> getBest();

    /**
     * Returns the best scorable instance along with its score and the context used to evaluate it.
     *
     * @return an {@link NOptional} wrapping the {@link NScorableResult} for the best instance, or empty if none are valid
     */
    NOptional<NScorableResult<T>> getBestResult();
}
