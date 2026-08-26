package net.thevpc.nuts.core.test;

import net.thevpc.nuts.boot.NBootArguments;
import net.thevpc.nuts.boot.NBootCompleteResult;
import net.thevpc.nuts.boot.NBootWorkspace;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.*;
import java.util.stream.Collectors;

public class AutoCompleteTest {

    private static NBootCompleteResult complete(String modeArg, String... words) {
        List<String> full = new ArrayList<>();
        full.add(modeArg);
        full.addAll(Arrays.asList(words));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream pout = new PrintStream(out);
        NBootWorkspace.of(
                NBootArguments.ofFullArgs(full.toArray(new String[0]))
                        .out(pout)
                        .err(pout)
                        .in(new ByteArrayInputStream(new byte[0]))
        ).runWorkspace();
        pout.flush();
        return NBootCompleteResult.parse(out.toString());
    }

    private static Set<String> values(NBootCompleteResult r) {
        return r.candidates().stream().map(NBootCompleteResult.Candidate::value).collect(Collectors.toSet());
    }

    @Test
    public void testEmptyWord() {
        NBootCompleteResult r = complete("--nuts-exec-mode=complete");
        Set<String> v = values(r);
        Assertions.assertTrue(v.contains("--version"));
        Assertions.assertTrue(v.contains("--help"));
    }

    @Test
    public void testPrefixFiltering() {
        // "--v" should only match option NAMES starting with "--v" — not "-version"/"-v" (short forms).
        NBootCompleteResult r = complete("--nuts-exec-mode=complete,0,3", "--v");
        Set<String> v = values(r);
        Assertions.assertTrue(v.contains("--version"));
        Assertions.assertTrue(v.contains("--verbose"));
        Assertions.assertFalse(v.contains("--workspace")); // wrong prefix, sanity check filtering isn't a no-op
        for (String s : v) {
            Assertions.assertTrue(s.startsWith("--v"), "unexpected candidate: " + s);
        }
    }

    @Test
    public void testMidWordOffsetIgnoresTrailingChars() {
        // Full token is "--vXXXXX" but cursor sits right after "--v" (offset=3).
        // Only the prefix up to the cursor should be used for matching — trailing
        // garbage after the cursor must NOT suppress or alter the match set.
        NBootCompleteResult r = complete("--nuts-exec-mode=complete,0,3", "--vXXXXX");
        Set<String> v = values(r);
        Assertions.assertTrue(v.contains("--version"));
        Assertions.assertTrue(v.contains("--verbose"));
    }

    @Test
    public void testEnumValueAttachedWithEquals() {
        // "--color=" with nothing after '=' -> all enum values, composed back with the key.
        NBootCompleteResult r = complete("--nuts-exec-mode=complete,0,9", "--color=");
        Set<String> v = values(r);
        Assertions.assertTrue(v.contains("--color=default"));
        Assertions.assertTrue(v.contains("--color=ansi"));
        Assertions.assertTrue(v.contains("--color=formatted"));
        Assertions.assertTrue(v.contains("--color=filtered"));
        Assertions.assertTrue(v.contains("--color=inherited"));
    }

    @Test
    public void testEnumValueAttachedWithPartialPrefix() {
        // "--fetch=onl" -> only "online" survives the prefix filter.
        NBootCompleteResult r = complete("--nuts-exec-mode=complete,0,11", "--fetch=onl");
        Set<String> v = values(r);
        Assertions.assertEquals(Collections.singleton("--fetch=online"), v);
    }

    @Test
    public void testEnumValueAsSeparateToken() {
        // "nuts --fetch <TAB>" — value is a SEPARATE word, not attached via '='.
        // Candidates should be the bare enum values, not prefixed with "--fetch=".
        NBootCompleteResult r = complete("--nuts-exec-mode=complete,1,0", "--fetch", "");
        Set<String> v = values(r);
        Assertions.assertEquals(
                new HashSet<>(Arrays.asList("offline", "online", "anywhere", "remote")),
                v
        );
    }

    @Test
    public void testEnumValueAsSeparateTokenWithPrefix() {
        // "nuts --fetch onl<TAB>"
        NBootCompleteResult r = complete("--nuts-exec-mode=complete,1,3", "--fetch", "onl");
        Set<String> v = values(r);
        Assertions.assertEquals(Collections.singleton("online"), v);
    }

    @Test
    public void testFileValueFlagProducesNoEnumeratedCandidatesButFilenamesDirective() {
        // --java-home takes a path; nuts shouldn't try to enumerate filesystem
        // paths itself — it should defer to the shell via the FILENAMES directive.
        NBootCompleteResult r = complete("--nuts-exec-mode=complete,1,0", "--java-home", "");
        Assertions.assertTrue(r.candidates().isEmpty());
        Assertions.assertTrue(r.flags().contains(NBootCompleteResult.Flag.FILENAMES));
    }

    @Test
    public void testFlagOptionHasNoValueCompletion() {
        // --verbose is FLAG arity: completing the word AFTER it should behave as a
        // fresh key position, not attempt to complete a "value" for --verbose.
        NBootCompleteResult r = complete("--nuts-exec-mode=complete,1,1", "--verbose", "-");
        Set<String> v = values(r);
        // "-" alone is itself a registered TERMINAL option name, so it should appear verbatim.
        Assertions.assertTrue(v.contains("-"));
    }

    @Test
    public void testTerminalOptionStopsCompletionScope() {
        // Once "-e" (a TERMINAL option, i.e. exec-passthrough) has been seen earlier
        // in the word list, everything after it belongs to the delegated command —
        // nuts' own completion must decline to guess, returning no candidates and no flags.
        NBootCompleteResult r = complete("--nuts-exec-mode=complete,2,5", "-e", "--something", "other");
        Assertions.assertTrue(r.candidates().isEmpty());
        Assertions.assertTrue(r.flags().isEmpty());
    }

    @Test
    public void testUnknownPrefixMatchesNothing() {
        NBootCompleteResult r = complete("--nuts-exec-mode=complete,0,17", "--nonexistentzzzz");
        Assertions.assertTrue(r.candidates().isEmpty());
    }

    @Test
    public void testAliasesResolveToSameSpec() {
        // -f and --fetch are aliases of the same option; both should trigger identical
        // value-completion behavior for the following word.
        NBootCompleteResult rShort = complete("--nuts-exec-mode=complete,1,0", "-f", "");
        NBootCompleteResult rLong = complete("--nuts-exec-mode=complete,1,0", "--fetch", "");
        Assertions.assertEquals(values(rShort), values(rLong));
    }

    @Test
    public void testOptionalValueFlagWithoutEqualsBehavesAsFreshKeyPosition() {
        // --progress is OPTIONAL_VALUE: value only attaches via '='. A bare following
        // word (no '=') must NOT be swallowed as --progress's value; it's a fresh
        // key/flag completion instead.
        NBootCompleteResult r = complete("--nuts-exec-mode=complete,1,3", "--progress", "--v");
        Set<String> v = values(r);
        Assertions.assertTrue(v.contains("--version"));
        Assertions.assertTrue(v.contains("--verbose"));
    }
}