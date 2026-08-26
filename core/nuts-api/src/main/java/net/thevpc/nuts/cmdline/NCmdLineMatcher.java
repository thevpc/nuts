package net.thevpc.nuts.cmdline;

import java.util.function.Predicate;

/**
 * NCmdLineMatcher interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NCmdLineMatcher {
    /**
     * With.
     *
     * @param processor processor
     * @return with result
     */
    NCmdLineMatcher with(NCmdLineProcessor processor);

    /**
     * When any.
     *
     * @return when any result
     */
    NCmdLineMatcherCondition whenAny();

    /**
     * When.
     *
     * @param names names
     * @return when result
     */
    NCmdLineMatcherCondition when(String... names);

    /**
     * When arg.
     *
     * @param condition condition
     * @return when arg result
     */
    NCmdLineMatcherCondition whenArg(Predicate<NArg> condition);

    /**
     * When raw.
     *
     * @param condition condition
     * @return when raw result
     */
    NCmdLineMatcherCondition whenRaw(Predicate<NCmdLine> condition);

    /**
     * When non option.
     *
     * @return when non option result
     */
    NCmdLineMatcherCondition whenNonOption();

    /**
     * When option.
     *
     * @return when option result
     */
    NCmdLineMatcherCondition whenOption();

    /**
     * With defaults.
     *
     * @return with defaults result
     */
    NCmdLineMatcher withDefaults();

    /**
     * Any match.
     *
     * @return any match result
     */
    boolean anyMatch();

    /**
     * No match.
     *
     * @return no match result
     */
    boolean noMatch();

    /**
     * Throws an error if no processor matched the current argument.
     * Does not apply session defaults.
     */
    void require();

    /**
     * equivalent to {@code while(cmdline.hasNext()){require()} }
     */
    void requireAll();

}
