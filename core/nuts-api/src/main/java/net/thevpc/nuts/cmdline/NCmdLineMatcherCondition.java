package net.thevpc.nuts.cmdline;

import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * NCmdLineMatcherCondition interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NCmdLineMatcherCondition {
    /**
     * consume next argument with boolean value and run {@code consumer}
     *
     * @return true if active
     */
    NCmdLineMatcher asFlag(Consumer<NArg> consumer);

    /**
     * And.
     *
     * @param condition condition
     * @return and result
     */
    NCmdLineMatcherCondition and(Predicate<NCmdLine> condition);

    /**
     * Display.
     *
     * @param display display
     * @return display result
     */
    NCmdLineMatcherCondition display(String display);

    /**
     * Value complete.
     *
     * @param finder finder
     * @return value complete result
     */
    NCmdLineMatcherCondition valueComplete(NArgValueComplete finder);

    /**
     * consume next argument with string value and run {@code consumer}
     *
     * @return true if active
     */
    NCmdLineMatcher asEntry(Consumer<NArg> consumer);

    /**
     * As attached entry.
     *
     * @param consumer consumer
     * @return as attached entry result
     */
    NCmdLineMatcher asAttachedEntry(Consumer<NArg> consumer);

    /**
     * As required entry.
     *
     * @param consumer consumer
     * @return as required entry result
     */
    NCmdLineMatcher asRequiredEntry(Consumer<NArg> consumer);

    /**
     * As arg.
     *
     * @param consumer consumer
     * @return as arg result
     */
    NCmdLineMatcher asArg(Consumer<NArg> consumer);

    /**
     * Skip.
     *
     * @return skip result
     */
    NCmdLineMatcher skip();

    /**
     * As raw.
     *
     * @param consumer consumer
     * @return as raw result
     */
    NCmdLineMatcher asRaw(Consumer<NCmdLine> consumer);

    /**
     * As true flag.
     *
     * @param consumer consumer
     * @return as true flag result
     */
    NCmdLineMatcher asTrueFlag(Consumer<NArg> consumer);
}
