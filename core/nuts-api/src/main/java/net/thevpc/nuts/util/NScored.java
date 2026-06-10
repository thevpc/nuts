package net.thevpc.nuts.util;

import java.util.Objects;

/**
 * A simple holder for a value and its associated score.
 *
 * <p>Scores are used to rank alternatives, typically in extension loading
 * or component selection. Higher scores indicate higher priority or better match.
 *
 * @param <T> the type of the value
 */
public class NScored<T> {

    private T value;
    private int score;

    /**
     * Constructs a scored value.
     *
     * @param value the wrapped value (may be {@code null})
     * @param score the score (higher is better)
     */
    public NScored(T value, int score) {
        this.value = value;
        this.score = score;
    }

    /**
     * Returns the wrapped value.
     *
     * @return the value (may be {@code null})
     */
    public T value() {
        return value;
    }

    /**
     * Returns the score.
     *
     * @return the score
     */
    public int score() {
        return score;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        NScored nScored = (NScored) o;
        return score == nScored.score && Objects.equals(value, nScored.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, score);
    }

    @Override
    public String toString() {
        return "NScored{" +
                "value=" + value +
                ", score=" + score +
                '}';
    }
}
