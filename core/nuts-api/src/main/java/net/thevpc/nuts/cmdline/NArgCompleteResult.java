package net.thevpc.nuts.cmdline;

import java.util.*;

public interface NArgCompleteResult {
    static NArgCompleteResult ofFlags(NArgCompleteFlag... flags) {
        return of(null, flags == null ? null : Arrays.asList(flags));
    }

    static NArgCompleteResult ofFlags(Collection<NArgCompleteFlag>  flags) {
        return of(null, flags);
    }

    static NArgCompleteResult ofCandidates(NArgCompleteCandidate... candidates) {
        return of(candidates == null ? null : Arrays.asList(candidates),null);
    }
    static NArgCompleteResult ofCandidates(Collection<NArgCompleteCandidate> candidates) {
        return of(candidates,null);
    }

    List<NArgCompleteCandidate> candidates();

    Set<NArgCompleteFlag> flags();

    static NArgCompleteResult of(Collection<NArgCompleteCandidate> candidates, Collection<NArgCompleteFlag> flags) {
        return new DefaultNArgCompleteResult(candidates, flags);
    }

}
