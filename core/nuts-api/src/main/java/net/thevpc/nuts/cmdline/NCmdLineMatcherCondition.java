package net.thevpc.nuts.cmdline;

import java.util.function.Consumer;
import java.util.function.Predicate;

public interface NCmdLineMatcherCondition {
    /**
     * consume next argument with boolean value and run {@code consumer}
     *
     * @return true if active
     */
    NCmdLineMatcher asFlag(Consumer<NArg> consumer);

    NCmdLineMatcherCondition and(Predicate<NCmdLine> condition);

    NCmdLineMatcherCondition display(String display);

    NCmdLineMatcherCondition valueComplete(NArgValueComplete finder);

    /**
     * consume next argument with string value and run {@code consumer}
     *
     * @return true if active
     */
    NCmdLineMatcher asEntry(Consumer<NArg> consumer);

    NCmdLineMatcher asAttachedEntry(Consumer<NArg> consumer);

    NCmdLineMatcher asRequiredEntry(Consumer<NArg> consumer);

    NCmdLineMatcher asArg(Consumer<NArg> consumer);

    NCmdLineMatcher skip();

    NCmdLineMatcher asRaw(Consumer<NCmdLine> consumer);

    NCmdLineMatcher asTrueFlag(Consumer<NArg> consumer);
}
