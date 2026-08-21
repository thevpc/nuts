package net.thevpc.nuts.cmdline;

import java.util.Collection;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public interface NArgCompleteValueComplete {
    static NArgCompleteValueComplete ofFlags(NArgCompleteFlag... flags) {
        return (prefix, suffix) -> NArgCompleteResult.ofFlags(flags);
    }

    static NArgCompleteValueComplete ofFlags(Collection<NArgCompleteFlag> flags) {
        return (prefix, suffix) -> NArgCompleteResult.ofFlags(flags);
    }

    static NArgCompleteValueComplete ofSimpleCandidatesListSupplier(Supplier<? extends Collection<String>> candidates) {
        return (prefix, suffix) -> {
            if (candidates == null) {
                return NArgCompleteResult.ofSimpleCandidates();
            }
            Collection<String> c = candidates.get();
            if (c == null) {
                return NArgCompleteResult.ofSimpleCandidates();
            }
            String p = prefix == null ? "" : prefix;
            String s = suffix == null ? "" : suffix;
            return NArgCompleteResult.ofSimpleCandidates(c.stream().filter(x -> x != null && x.startsWith(p) && x.endsWith(s)).collect(Collectors.toList()));
        };
    }

    static NArgCompleteValueComplete ofSimpleCandidatesStreamSupplier(Supplier<Stream<String>> candidates) {
        return (prefix, suffix) -> {
            if (candidates == null) {
                return NArgCompleteResult.ofSimpleCandidates();
            }
            Stream<String> c = candidates.get();
            if (c == null) {
                return NArgCompleteResult.ofSimpleCandidates();
            }
            String p = prefix == null ? "" : prefix;
            String s = suffix == null ? "" : suffix;
            return NArgCompleteResult.ofSimpleCandidates(c.filter(x -> x != null && x.startsWith(p) && x.endsWith(s)).collect(Collectors.toList()));
        };
    }

    static NArgCompleteValueComplete ofSimpleCandidatesList(List<String> candidates) {
        return (prefix, suffix) -> {
            if (candidates == null) {
                return NArgCompleteResult.ofSimpleCandidates();
            }
            Stream<String> c = candidates.stream();
            if (c == null) {
                return NArgCompleteResult.ofSimpleCandidates();
            }
            String p = prefix == null ? "" : prefix;
            String s = suffix == null ? "" : suffix;
            return NArgCompleteResult.ofSimpleCandidates(c.filter(x -> x != null && x.startsWith(p) && x.endsWith(s)).collect(Collectors.toList()));
        };
    }

    NArgCompleteResult searchValue(String prefix, String suffix);
}
