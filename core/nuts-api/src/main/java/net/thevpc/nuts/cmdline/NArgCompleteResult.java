package net.thevpc.nuts.cmdline;

import net.thevpc.nuts.util.NBlankable;

import java.util.*;
import java.util.stream.Collectors;

public interface NArgCompleteResult extends NBlankable {
    static NArgCompleteResult of(String txt) {
        return DefaultNArgCompleteResult.parse(txt);
    }

    static NArgCompleteResult ofFlags(NArgCompleteFlag... flags) {
        return of(null, flags == null ? null : Arrays.asList(flags));
    }

    static NArgCompleteResult ofFlags(Collection<NArgCompleteFlag> flags) {
        return of(null, flags);
    }

    static NArgCompleteResult ofCandidates(NArgCompleteCandidate... candidates) {
        return of(candidates == null ? null : Arrays.asList(candidates), null);
    }

    static NArgCompleteResult ofSimpleCandidates(String... candidates) {
        return of(candidates == null ? null : Arrays.stream(candidates).filter(Objects::nonNull).map(NArgCompleteCandidate::of).collect(Collectors.toList()), null);
    }

    static NArgCompleteResult ofSimpleCandidates(Collection<String> candidates) {
        return of(candidates == null ? null : candidates.stream().filter(Objects::nonNull).map(NArgCompleteCandidate::of).collect(Collectors.toList()), null);
    }

    static NArgCompleteResult ofBlank() {
        return DefaultNArgCompleteResult.BLANK;
    }

    static NArgCompleteResult ofCandidates(Collection<NArgCompleteCandidate> candidates) {
        return of(candidates, null);
    }

    List<NArgCompleteCandidate> candidates();

    Set<NArgCompleteFlag> flags();

    static NArgCompleteResult of(Collection<NArgCompleteCandidate> candidates, Collection<NArgCompleteFlag> flags) {
        return new DefaultNArgCompleteResult(candidates, flags);
    }

    String format();

}
