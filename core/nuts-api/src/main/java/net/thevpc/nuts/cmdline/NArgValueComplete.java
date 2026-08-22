package net.thevpc.nuts.cmdline;

import java.util.Arrays;
import java.util.Collection;
import java.util.function.Supplier;
import java.util.stream.Stream;

public interface NArgValueComplete {
    static NArgValueComplete ofFlags(NArgCompleteFlag... flags) {
        return ctx -> NArgCompleteResult.ofFlags(flags);
    }

    static NArgValueComplete ofFlags(Collection<NArgCompleteFlag> flags) {
        return ctx -> NArgCompleteResult.ofFlags(flags);
    }

    static NArgValueComplete ofSimpleCandidatesListSupplier(Supplier<? extends Collection<String>> candidates) {
        return (ctx) -> ctx.filterValues(candidates == null ? null : candidates.get());
    }

    static NArgValueComplete ofSimpleCandidatesStreamSupplier(Supplier<Stream<String>> candidates) {
        return (ctx) -> ctx.filterValues(candidates == null ? null : candidates.get());
    }

    static NArgValueComplete ofSimpleCandidatesList(Collection<String> candidates) {
        return (ctx) -> ctx.filterValues(candidates);
    }
    static NArgValueComplete ofSimpleCandidatesList(String... candidates) {
        return (ctx) -> ctx.filterValues(candidates == null ? null : Arrays.asList(candidates));
    }

    static NArgValueComplete ofCandidatesList(Collection<NArgCompleteCandidate> candidates) {
        return (ctx) -> ctx.filterCandidates(candidates);
    }

    static NArgValueComplete ofCandidatesList(NArgCompleteCandidate... candidates) {
        return (ctx) -> ctx.filterCandidates(candidates == null ? null : Arrays.asList(candidates));
    }

    NArgCompleteResult searchValue(Context prefixcontext);

    interface Context {
        String prefix();

        String suffix();

        boolean matches(String word);

        NArgCompleteResult filterValues(Stream<String> values);

        NArgCompleteResult filterValues(Collection<String> values);

        NArgCompleteResult filterCandidates(Stream<NArgCompleteCandidate> values);

        NArgCompleteResult filterCandidates(Collection<NArgCompleteCandidate> word);
    }
}
