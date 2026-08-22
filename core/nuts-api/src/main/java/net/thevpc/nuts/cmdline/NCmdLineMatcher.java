package net.thevpc.nuts.cmdline;

import java.util.function.Predicate;

public interface NCmdLineMatcher {
    NCmdLineMatcher with(NCmdLineProcessor processor);

    NCmdLineMatcherCondition whenAny();

    NCmdLineMatcherCondition when(String... names);

    NCmdLineMatcherCondition whenArg(Predicate<NArg> condition);

    NCmdLineMatcherCondition whenRaw(Predicate<NCmdLine> condition);

    NCmdLineMatcherCondition whenNonOption();

    NCmdLineMatcherCondition whenOption();

    NCmdLineMatcher withDefaults();

    boolean anyMatch();

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
