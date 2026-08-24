package net.thevpc.nuts.cmdline;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Stream;

/**
 * NArgValueComplete interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NArgValueComplete {
    /**
     * Creates a new instance of of flags.
     *
     * @param flags flags
     * @return of flags result
     */
    static NArgValueComplete ofFlags(NArgCompleteFlag... flags) {
        return ctx -> NArgCompleteResult.ofFlags(flags);
    }

    /**
     * Creates a new instance of of flags.
     *
     * @param flags flags
     * @return of flags result
     */
    static NArgValueComplete ofFlags(Collection<NArgCompleteFlag> flags) {
        return ctx -> NArgCompleteResult.ofFlags(flags);
    }

    /**
     * Creates a new instance of of simple candidates list supplier.
     *
     * @param candidates candidates
     * @return of simple candidates list supplier result
     */
    static NArgValueComplete ofSimpleCandidatesListSupplier(Supplier<? extends Collection<String>> candidates) {
      /**
       * Return.
       *
       * @param candidates.get() candidates.get()
       */
        return (ctx) -> ctx.filterValues(candidates == null ? null : candidates.get());
    }

    /**
     * Creates a new instance of of simple candidates stream supplier.
     *
     * @param candidates candidates
     * @return of simple candidates stream supplier result
     */
    static NArgValueComplete ofSimpleCandidatesStreamSupplier(Supplier<Stream<String>> candidates) {
      /**
       * Return.
       *
       * @param candidates.get() candidates.get()
       */
        return (ctx) -> ctx.filterValues(candidates == null ? null : candidates.get());
    }

    /**
     * Creates a new instance of of simple candidates list.
     *
     * @param candidates candidates
     * @return of simple candidates list result
     */
    static NArgValueComplete ofSimpleCandidatesList(Collection<String> candidates) {
      /**
       * Return.
       *
       * @param ctx.filterValues(candidates ctx.filter values(candidates
       */
        return (ctx) -> ctx.filterValues(candidates);
    }
    /**
     * Creates a new instance of of simple candidates list.
     *
     * @param candidates candidates
     * @return of simple candidates list result
     */
    static NArgValueComplete ofSimpleCandidatesList(String... candidates) {
      /**
       * Return.
       *
       * @param Arrays.asList(candidates) arrays.as list(candidates)
       */
        return (ctx) -> ctx.filterValues(candidates == null ? null : Arrays.asList(candidates));
    }

    /**
     * Creates a new instance of of candidates list.
     *
     * @param candidates candidates
     * @return of candidates list result
     */
    static NArgValueComplete ofCandidatesList(Collection<NArgCompleteCandidate> candidates) {
      /**
       * Return.
       *
       * @param ctx.filterCandidates(candidates ctx.filter candidates(candidates
       */
        return (ctx) -> ctx.filterCandidates(candidates);
    }

    /**
     * Creates a new instance of of candidates list.
     *
     * @param candidates candidates
     * @return of candidates list result
     */
    static NArgValueComplete ofCandidatesList(NArgCompleteCandidate... candidates) {
      /**
       * Return.
       *
       * @param Arrays.asList(candidates) arrays.as list(candidates)
       */
        return (ctx) -> ctx.filterCandidates(candidates == null ? null : Arrays.asList(candidates));
    }

    /**
     * Finds the search value.
     *
     * @param prefixcontext prefixcontext
     * @return search value result
     */
    NArgCompleteResult searchValue(Context prefixcontext);

    interface Context {
        /**
         * Prefix.
         *
         * @return prefix result
         */
        String prefix();

        /**
         * Suffix.
         *
         * @return suffix result
         */
        String suffix();

        /**
         * Matches.
         *
         * @param word word
         * @return matches result
         */
        boolean matches(String word);

        /**
         * Filter values.
         *
         * @param values values
         * @return filter values result
         */
        NArgCompleteResult filterValues(Stream<String> values);

        /**
         * Filter values.
         *
         * @param values values
         * @return filter values result
         */
        NArgCompleteResult filterValues(Collection<String> values);

        /**
         * Filter candidates.
         *
         * @param values values
         * @return filter candidates result
         */
        NArgCompleteResult filterCandidates(Stream<NArgCompleteCandidate> values);

        /**
         * Filter candidates.
         *
         * @param word word
         * @return filter candidates result
         */
        NArgCompleteResult filterCandidates(Collection<NArgCompleteCandidate> word);
    }
}
