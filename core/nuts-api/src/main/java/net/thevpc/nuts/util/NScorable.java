package net.thevpc.nuts.util;

import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.spi.NUtilSPI;

/**
 * Interface representing a "scorable" entity.
 * <p>
 * A scorable is any object whose instances can be assigned a score (or level of support)
 * within a given context. Scores are used by the framework to select the most appropriate
 * implementation or instance among several alternatives. Scorable objects are commonly used
 * for runtime selection of providers, callables, or actions.
 * </p>
 * <p>
 * Implementations of {@code NScorable} must define how the score is computed for a
 * given {@link NScorableContext}. Scores should follow these conventions:
 * </p>
 * <ul>
 *     <li>{@link #CUSTOM_SCORE} (1000): Minimum score for user-defined implementations.</li>
 *     <li>{@link #DEFAULT_SCORE} (10): Default score for runtime implementations (e.g., nuts runtime).</li>
 *     <li>{@link #UNSUPPORTED_SCORE} (-1): Indicates that the instance should be discarded or ignored.</li>
 * </ul>
 * <p>
 * The framework provides static utility methods to query and validate scorable instances.
 * Scorable objects are immutable with respect to score evaluation; the only input to
 * scoring is the {@link NScorableContext}.
 * </p>
 *
 * @author thevpc
 * @since 0.8.7
 */
public interface NScorable {
    /**
     * Minimum score for user-defined implementations.
     * Scores equal or greater than this are considered user-customized.
     */
    int CUSTOM_SCORE = 1000;

    /**
     * Default score for runtime-provided implementations.
     */
    int DEFAULT_SCORE = 10;

    /**
     * When getScore(...) == UNSUPPORTED_SCORE, the instance is considered invalid and
     * will be ignored by the selection framework.
     */
    int UNSUPPORTED_SCORE = -1;


    /**
     * Computes the score of this instance in the given context.
     * <p>
     * The score determines how preferable this instance is relative to other scorable
     * instances within the same context. Higher values indicate better suitability.
     * A score less than or equal to 0 indicates the instance is invalid in this context.
     * The constant {@link #UNSUPPORTED_SCORE} (-1) is a convenient label for an
     * invalid score, but any non-positive value is treated as invalid.
     * </p>
     *
     * @param context the scoring context containing criteria and environment information
     * @return an integer score representing support level
     */
    int getScore(NScorableContext context);

    /**
     * Checks whether a given scorable instance is valid in a given context.
     * <p>
     * A scorable is considered valid if its score is greater than zero.
     * Scores equal to {@link #UNSUPPORTED_SCORE} or any other non-positive value are invalid.
     * </p>
     *
     * @param scorable the scorable instance to test
     * @param context the scoring context
     * @return true if the score is positive, false otherwise
     * @throws NAssertException,NDetachedAssertException if {@code scorable} is null
     */
    static boolean isValidScore(NScorable scorable,NScorableContext context) {
        NAssert.requireNonNull(scorable,"scorable");
        return scorable.getScore(context) > 0;
    }

    /**
     * Creates a new {@link NScorableQuery} with a specific scoring context.
     * <p>
     * Queries are used to filter and select the best scorable instance(s) among
     * a collection of candidates.
     * </p>
     *
     * @param context the scoring context
     * @param <T> type of the scorable
     * @return a new scorable query initialized with the provided context
     */
    static <T extends NScorable> NScorableQuery<T> query(NScorableContext context) {
        return NExtensions.of().createComponent(NUtilSPI.class).get().<T>ofScorableQuery().withContext(context==null?NScorableContext.of():context);
    }

    /**
     * Creates a new {@link NScorableQuery} without an explicit context.
     * <p>
     * A default context will be used if needed. This is convenient when context is
     * optional or not required for scoring.
     * </p>
     *
     * @param <T> type of the scorable
     * @return a new scorable query with a default context
     */
    static <T extends NScorable> NScorableQuery<T> query() {
        return NExtensions.of().createComponent(NUtilSPI.class).get().ofScorableQuery();
    }

}
