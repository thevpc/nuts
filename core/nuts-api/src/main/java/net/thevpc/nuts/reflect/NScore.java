package net.thevpc.nuts.reflect;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Assigns a priority or relevance score to a component or factory method implementation.
 * <p>
 * Used during service and component discovery to resolve conflicts when multiple
 * implementations match the same contract. Implementations with higher scores
 * take precedence over those with lower scores.
 *
 * @author thevpc
 * @since 0.8.0
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface NScore {
    /**
     * Fixed integer score value. Higher values indicate higher priority.
     *
     * @return the fixed priority score
     */
    int fixed() default Integer.MIN_VALUE;

    /**
     * Custom dynamic scoring class evaluated at runtime.
     *
     * @return the custom scoring implementation class
     */
    Class<NScorable> custom() default NScorable.class;
}
