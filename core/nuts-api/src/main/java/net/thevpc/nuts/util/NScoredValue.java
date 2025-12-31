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
public interface NScoredValue<T> {
    /**
     * Returns the scorable instance that was evaluated.
     * when this is an invalid scored instance, this method should not be called as it might throw an exception.
     *
     * @return the scorable instance
     */
    T value();

    /**
     * when unsupported, may return null
     *
     * @return api interface
     */
    Class<T> getApiType();

    /**
     * when unsupported, may return null
     *
     * @return implementation class
     */
    Class<? extends T> getImplType();

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
     *
     * @return when this value is valid aka its score is positive non null
     */
    default boolean isValid() {
        return score() > 0;
    }
}
