package net.thevpc.nuts.boot.internal.cmdline;

import net.thevpc.nuts.boot.NBootCompleteResult;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class NBootOptionSpec {

    public enum Arity {
        /** Boolean toggle. Never consumes a separate following token. */
        FLAG,
        /** Requires a value: attached via '=' in the same token, or read from the next token if not attached. */
        VALUE,
        /** Value is optional and, if present, must be attached via '='. Never consumes a following token. */
        OPTIONAL_VALUE,
        /** Once matched, everything remaining belongs to the delegated command/app — out of nuts' own completion scope. */
        TERMINAL
    }

    private final List<String> names;
    private final Arity arity;
    private final Function<String, List<NBootCompleteResult.Candidate>> valueCandidates; // null = free-form, no enumeration
    private final List<NBootCompleteResult.Flag> resultFlags;
    private NBootOptionSpec(List<String> names, Arity arity,
                            Function<String, List<NBootCompleteResult.Candidate>> valueCandidates,
                            List<NBootCompleteResult.Flag> resultFlags) {
        this.names = names;
        this.arity = arity;
        this.valueCandidates = valueCandidates;
        this.resultFlags = resultFlags;
    }

    public List<String> names() { return names; }
    public Arity arity() { return arity; }
    public List<NBootCompleteResult.Flag> resultFlags() { return resultFlags; }
    public boolean hasValueCompletion() { return valueCandidates != null; }

    public List<NBootCompleteResult.Candidate> completeValue(String prefix) {
        return valueCandidates == null ? Collections.emptyList() : valueCandidates.apply(prefix);
    }

    // ---- builders: each option in the registry is one line using these ----

    public static NBootOptionSpec flag(String... names) {
        return new NBootOptionSpec(Arrays.asList(names), Arity.FLAG, null, Collections.emptyList());
    }

    public static NBootOptionSpec terminal(String... names) {
        return new NBootOptionSpec(Arrays.asList(names), Arity.TERMINAL, null, Collections.emptyList());
    }

    public static NBootOptionSpec freeValue(String... names) {
        return new NBootOptionSpec(Arrays.asList(names), Arity.VALUE, null, Collections.emptyList());
    }

    public static NBootOptionSpec optionalValue(String... names) {
        return new NBootOptionSpec(Arrays.asList(names), Arity.OPTIONAL_VALUE, null, Collections.emptyList());
    }

    /** VALUE arity whose completions are a fixed enum (mirrors a parseXxx() method already in the parser). */
    public static NBootOptionSpec enumValue(String[] names, String... values) {
        List<NBootCompleteResult.Candidate> all = Arrays.stream(values)
                .map(NBootCompleteResult.Candidate::new).collect(Collectors.toList());
        return new NBootOptionSpec(Arrays.asList(names), Arity.VALUE,
                prefix -> filterByPrefix(all, prefix), Collections.emptyList());
    }

    public static NBootOptionSpec optionalEnumValue(String[] names, String... values) {
        List<NBootCompleteResult.Candidate> all = Arrays.stream(values)
                .map(NBootCompleteResult.Candidate::new).collect(Collectors.toList());
        return new NBootOptionSpec(Arrays.asList(names), Arity.OPTIONAL_VALUE,
                prefix -> filterByPrefix(all, prefix), Collections.emptyList());
    }

    /** VALUE arity whose completion is delegated to the shell's own filename completion (no enumeration here). */
    public static NBootOptionSpec fileValue(String... names) {
        return new NBootOptionSpec(Arrays.asList(names), Arity.VALUE, null,
                Collections.singletonList(NBootCompleteResult.Flag.FILENAMES));
    }

    private static List<NBootCompleteResult.Candidate> filterByPrefix(List<NBootCompleteResult.Candidate> all, String prefix) {
        if (prefix == null || prefix.isEmpty()) return all;
        return all.stream().filter(c -> c.value().startsWith(prefix)).collect(Collectors.toList());
    }
}