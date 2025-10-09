package net.thevpc.nuts.util;

/**
 * Represents the result of evaluating a {@link NScorable} instance.
 * <p>
 * Each result couples a scorable instance with its computed score and the
 * context in which the score was calculated.
 * </p>
 *
 * @param <T> the type of the scorable instance
 */
public interface NScorableResult<T extends NScorable> {
    /**
     * Returns the scorable instance that was evaluated.
     *
     * @return the scorable instance
     */
    T value();

    /**
     * Returns the score computed for the scorable instance.
     * <p>
     * A valid score is greater than zero. A score less than or equal to zero
     * indicates that the instance should not be considered.
     * </p>
     *
     * @return the score of the scorable instance
     */
    int score();

    /**
     * Returns the context used when computing the score.
     *
     * @return the {@link NScorableContext} used for scoring
     */
    NScorableContext context();
}
