package net.thevpc.nuts.util;

/**
 * FloatPredicate interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
@NJdkExtension("Missing from java.util.function — JDK only provides IntPredicate, LongPredicate, DoublePredicate")
public interface FloatPredicate {
    /**
     * Test.
     *
     * @param c c
     * @return test result
     */
    boolean test(float c);
}
