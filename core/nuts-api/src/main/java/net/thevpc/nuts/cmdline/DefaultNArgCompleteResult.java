package net.thevpc.nuts.cmdline;

import net.thevpc.nuts.collections.NCollections;

import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public class DefaultNArgCompleteResult implements NArgCompleteResult {
    private List<NArgCompleteCandidate> candidates;
    private Set<NArgCompleteFlag> flags;

    public DefaultNArgCompleteResult(Collection<NArgCompleteCandidate> candidates, Collection<NArgCompleteFlag> flags) {
        this.candidates = NCollections.unmodifiableNonNullList(candidates);
        this.flags = NCollections.unmodifiableNonNullSet(flags);
    }

    @Override
    public List<NArgCompleteCandidate> candidates() {
        return candidates;
    }

    @Override
    public Set<NArgCompleteFlag> flags() {
        return flags;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        net.thevpc.nuts.cmdline.DefaultNArgCompleteResult aDefault = (net.thevpc.nuts.cmdline.DefaultNArgCompleteResult) o;
        return Objects.equals(candidates, aDefault.candidates) && Objects.equals(flags, aDefault.flags);
    }

    @Override
    public int hashCode() {
        return Objects.hash(candidates, flags);
    }
}
