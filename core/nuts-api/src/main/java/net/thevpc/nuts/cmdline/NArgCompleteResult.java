package net.thevpc.nuts.cmdline;

import net.thevpc.nuts.util.NBlankable;

import java.util.*;
import java.util.stream.Collectors;

/**
 * NArgCompleteResult interface.
 *
 * @author thevpc
 * @since 0.8.0
 */
public interface NArgCompleteResult extends NBlankable {
    /**
     * Creates a new instance of of.
     *
     * @param txt txt
     * @return of result
     */
    static NArgCompleteResult of(String txt) {
        return DefaultNArgCompleteResult.parse(txt);
    }

    /**
     * Creates a new instance of of flags.
     *
     * @param flags flags
     * @return of flags result
     */
    static NArgCompleteResult ofFlags(NArgCompleteFlag... flags) {
        /**
         * Creates a new instance of of.
         *
         * @param null null
         * @param Arrays.asList(flags) arrays.as list(flags)
         * @return of result
         */
        return of(null, flags == null ? null : Arrays.asList(flags));
    }

    /**
     * Creates a new instance of of flags.
     *
     * @param flags flags
     * @return of flags result
     */
    static NArgCompleteResult ofFlags(Collection<NArgCompleteFlag> flags) {
        /**
         * Creates a new instance of of.
         *
         * @param null null
         * @param flags flags
         * @return of result
         */
        return of(null, flags);
    }

    /**
     * Creates a new instance of of candidates.
     *
     * @param candidates candidates
     * @return of candidates result
     */
    static NArgCompleteResult ofCandidates(NArgCompleteCandidate... candidates) {
        /**
         * Creates a new instance of of.
         *
         * @param Arrays.asList(candidates) arrays.as list(candidates)
         * @param null null
         * @return of result
         */
        return of(candidates == null ? null : Arrays.asList(candidates), null);
    }

    /**
     * Creates a new instance of of simple candidates.
     *
     * @param candidates candidates
     * @return of simple candidates result
     */
    static NArgCompleteResult ofSimpleCandidates(String... candidates) {
        /**
         * Creates a new instance of of.
         *
         * @param Arrays.stream(candidates).filter(Objects::nonNull).map(NArgCompleteCandidate::of).collect(Collectors.toList()) arrays.stream(candidates).filter( objects::non null).map(n arg complete candidate::of).collect( collectors.to list())
         * @param null null
         * @return of result
         */
        return of(candidates == null ? null : Arrays.stream(candidates).filter(Objects::nonNull).map(NArgCompleteCandidate::of).collect(Collectors.toList()), null);
    }

    /**
     * Creates a new instance of of simple candidates.
     *
     * @param candidates candidates
     * @return of simple candidates result
     */
    static NArgCompleteResult ofSimpleCandidates(Collection<String> candidates) {
        /**
         * Creates a new instance of of.
         *
         * @param candidates.stream().filter(Objects::nonNull).map(NArgCompleteCandidate::of).collect(Collectors.toList()) candidates.stream().filter( objects::non null).map(n arg complete candidate::of).collect( collectors.to list())
         * @param null null
         * @return of result
         */
        return of(candidates == null ? null : candidates.stream().filter(Objects::nonNull).map(NArgCompleteCandidate::of).collect(Collectors.toList()), null);
    }

    /**
     * Creates a new instance of of blank.
     *
     * @return of blank result
     */
    static NArgCompleteResult ofBlank() {
        return DefaultNArgCompleteResult.BLANK;
    }

    /**
     * Creates a new instance of of candidates.
     *
     * @param candidates candidates
     * @return of candidates result
     */
    static NArgCompleteResult ofCandidates(Collection<NArgCompleteCandidate> candidates) {
        /**
         * Creates a new instance of of.
         *
         * @param candidates candidates
         * @param null null
         * @return of result
         */
        return of(candidates, null);
    }

    /**
     * Checks if candidates.
     *
     * @return candidates result
     */
    List<NArgCompleteCandidate> candidates();

    /**
     * Flags.
     *
     * @return flags result
     */
    Set<NArgCompleteFlag> flags();

    /**
     * Creates a new instance of of.
     *
     * @param candidates candidates
     * @param flags flags
     * @return of result
     */
    static NArgCompleteResult of(Collection<NArgCompleteCandidate> candidates, Collection<NArgCompleteFlag> flags) {
        return new DefaultNArgCompleteResult(candidates, flags);
    }

    /**
     * Format.
     *
     * @return format result
     */
    String format();

}
