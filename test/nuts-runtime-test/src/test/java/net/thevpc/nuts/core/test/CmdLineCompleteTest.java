/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.cmdline.*;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.util.NAssert;
import net.thevpc.nuts.util.NOptional;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Tests for NCmdLine auto-complete (complete-mode) behaviour, covering all
 * NArgType variants and the new nextNonOption(String, NArgCompleteValueComplete)
 * overload.
 *
 * @author thevpc
 */
public class CmdLineCompleteTest {

    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    // ------------------------------------------------------------------ helpers

    /**
     * Builds a command line in complete mode with the given tokens and the
     * cursor sitting at {@code wordIndex} (within that word, offset 0).
     */
    private static NCmdLine completeAt(int wordIndex, String... tokens) {
        return NCmdLine.of(Arrays.asList(tokens))
                .complete(NArgCompletePos.of(wordIndex, 0));
    }

    private static Set<String> candidateStrings(NArgCompleteResult r) {
        if (r == null) return Collections.emptySet();
        return r.candidates().stream()
                .map(NArgCompleteCandidate::value)
                .collect(Collectors.toSet());
    }

    private static Set<NArgCompleteFlag> flagSet(NArgCompleteResult r) {
        if (r == null || r.flags() == null) return Collections.emptySet();
        return r.flags();
    }

    // ================================================================== FLAG

    @Test
    public void testFlag_partialPrefix() {
        // cursor at word 0, partial "--en" — should suggest "--enable"
        NCmdLine cmd = completeAt(0, "--en");
        while (cmd.hasNext()) {
            NOptional<NArg> a = cmd.next(NArgType.FLAG, "--enable");
            if (a.isPresent()) continue;
            if (cmd.isCompleteMode()) { cmd.skip(); continue; }
            cmd.throwUnexpectedArgument();
        }
        NArgCompleteResult result = cmd.completeResult();
        Assertions.assertNotNull(result);
        Assertions.assertTrue(candidateStrings(result).contains("--enable"),
                "Expected --enable in candidates: " + candidateStrings(result));
    }

    @Test
    public void testFlag_exactMatch() {
        // cursor at word 0, exact "--enable" — should still suggest "--enable"
        NCmdLine cmd = completeAt(0, "--enable");
        while (cmd.hasNext()) {
            NOptional<NArg> a = cmd.next(NArgType.FLAG, "--enable");
            if (a.isPresent()) continue;
            if (cmd.isCompleteMode()) { cmd.skip(); continue; }
            cmd.throwUnexpectedArgument();
        }
        NArgCompleteResult result = cmd.completeResult();
        Assertions.assertNotNull(result);
        Assertions.assertTrue(candidateStrings(result).contains("--enable"),
                "Expected --enable in candidates: " + candidateStrings(result));
    }

    @Test
    public void testFlag_noMatch() {
        // cursor at word 0, "--x" does not share a prefix with "--enable"
        NCmdLine cmd = completeAt(0, "--x");
        while (cmd.hasNext()) {
            NOptional<NArg> a = cmd.next(NArgType.FLAG, "--enable");
            if (a.isPresent()) continue;
            if (cmd.isCompleteMode()) { cmd.skip(); continue; }
            cmd.throwUnexpectedArgument();
        }
        NArgCompleteResult result = cmd.completeResult();
        // "--enable" should NOT appear (prefix mismatch)
        Set<String> cands = candidateStrings(result);
        Assertions.assertFalse(cands.contains("--enable"),
                "--enable should not appear for prefix '--x', got: " + cands);
    }

    // ================================================================== ENTRY

    @Test
    public void testEntry_partialOptionName() {
        // "--fil" at word 0 — should suggest "--file"
        NCmdLine cmd = completeAt(0, "--fil");
        while (cmd.hasNext()) {
            NOptional<NArg> n = cmd.next(NArgType.ENTRY, "project file",
                    (prefix, suffix) -> NArgCompleteResult.ofFlags(NArgCompleteFlag.FILENAMES),
                    "--file");
            if (n.isPresent()) continue;
            if (cmd.isCompleteMode()) { cmd.skip(); continue; }
            cmd.throwUnexpectedArgument();
        }
        NArgCompleteResult result = cmd.completeResult();
        Assertions.assertNotNull(result);
        Assertions.assertTrue(candidateStrings(result).contains("--file"),
                "Expected --file in: " + candidateStrings(result));
    }

    @Test
    public void testEntry_atValuePosition() {
        // "--file" fully typed (word 0), cursor at word 1 (value) — should return FILENAMES flag
        NCmdLine cmd = completeAt(1, "--file", "");
        while (cmd.hasNext()) {
            NOptional<NArg> n = cmd.next(NArgType.ENTRY, "project file",
                    (prefix, suffix) -> NArgCompleteResult.ofFlags(NArgCompleteFlag.FILENAMES),
                    "--file");
            if (n.isPresent()) continue;
            if (cmd.isCompleteMode()) { cmd.skip(); continue; }
            cmd.throwUnexpectedArgument();
        }
        NArgCompleteResult result = cmd.completeResult();
        Assertions.assertNotNull(result);
        Assertions.assertTrue(flagSet(result).contains(NArgCompleteFlag.FILENAMES),
                "Expected FILENAMES flag in: " + flagSet(result));
    }

    @Test
    public void testEntry_matcherApi() {
        // Same as testEntry_partialOptionName but using the matcher() fluent API
        NCmdLine cmd = completeAt(0, "--fil");
        cmd.matcher()
                .with("--file")
                .display("project file")
                .valueComplete((prefix, suffix) -> NArgCompleteResult.ofFlags(NArgCompleteFlag.FILENAMES))
                .matchEntry(a -> TestUtils.println("found file " + a.value()))
                /////
                .with("--folder")
                .display("project dir")
                .valueComplete((prefix, suffix) -> NArgCompleteResult.ofFlags(NArgCompleteFlag.DIRNAMES))
                .matchEntry(a -> TestUtils.println("found folder " + a.value()))
                /////
                .requireAll();
        NArgCompleteResult result = cmd.completeResult();
        NAssert.requireNamedEquals(
                NArgCompleteResult.ofCandidates(NArgCompleteCandidate.of("--file")),
                result,
                "entry-matcher"
        );
    }

    // ============================================================ REQUIRED_ENTRY

    @Test
    public void testRequiredEntry_atValue() {
        // "--out" typed at word 0, cursor at word 1 — value finder returns custom candidates
        NCmdLine cmd = completeAt(1, "--out", "");
        while (cmd.hasNext()) {
            NOptional<NArg> n = cmd.next(NArgType.REQUIRED_ENTRY, "output format",
                    (prefix, suffix) -> NArgCompleteResult.ofCandidates(
                            NArgCompleteCandidate.of("json"),
                            NArgCompleteCandidate.of("xml")),
                    "--out");
            if (n.isPresent()) continue;
            if (cmd.isCompleteMode()) { cmd.skip(); continue; }
            cmd.throwUnexpectedArgument();
        }
        NArgCompleteResult result = cmd.completeResult();
        Assertions.assertNotNull(result);
        Set<String> cands = candidateStrings(result);
        Assertions.assertTrue(cands.contains("json") && cands.contains("xml"),
                "Expected json+xml in: " + cands);
    }

    // ============================================================ ATTACHED_ENTRY

    @Test
    public void testAttachedEntry_noEquals() {
        // "--key" at word 0, no "=": ATTACHED_ENTRY should suggest "--key="
        NCmdLine cmd = completeAt(0, "--key");
        while (cmd.hasNext()) {
            NOptional<NArg> n = cmd.next(NArgType.ATTACHED_ENTRY, "--key");
            if (n.isPresent()) continue;
            if (cmd.isCompleteMode()) { cmd.skip(); continue; }
            cmd.throwUnexpectedArgument();
        }
        NArgCompleteResult result = cmd.completeResult();
        Assertions.assertNotNull(result);
        Set<String> cands = candidateStrings(result);
        Assertions.assertTrue(cands.stream().anyMatch(c -> c.startsWith("--key")),
                "Expected --key= candidate in: " + cands);
    }

    @Test
    public void testAttachedEntry_withEquals() {
        // "--key=val" already has value attached — in complete mode this should not throw
        NCmdLine cmd = completeAt(0, "--key=val");
        while (cmd.hasNext()) {
            NOptional<NArg> n = cmd.next(NArgType.ATTACHED_ENTRY, "--key");
            if (n.isPresent()) continue;
            if (cmd.isCompleteMode()) { cmd.skip(); continue; }
            cmd.throwUnexpectedArgument();
        }
        // In complete mode, completeResult() is always non-null (may have no candidates)
        Assertions.assertNotNull(cmd.completeResult());
    }

    // ================================================================= DEFAULT

    @Test
    public void testDefault_partialName() {
        // "--opt" at word 0 — DEFAULT type suggests "--option"
        NCmdLine cmd = completeAt(0, "--opt");
        while (cmd.hasNext()) {
            NOptional<NArg> n = cmd.next(NArgType.DEFAULT, "--option");
            if (n.isPresent()) continue;
            if (cmd.isCompleteMode()) { cmd.skip(); continue; }
            cmd.throwUnexpectedArgument();
        }
        NArgCompleteResult result = cmd.completeResult();
        Assertions.assertNotNull(result);
        Assertions.assertTrue(candidateStrings(result).contains("--option"),
                "Expected --option in: " + candidateStrings(result));
    }

    // ============================================================= NON-OPTION (new API)

    @Test
    public void testNonOption_newApi_withFinder() {
        // new nextNonOption(String, NArgCompleteValueComplete) — should return custom candidates
        NCmdLine cmd = completeAt(0, "fo");
        while (cmd.hasNext()) {
            NOptional<NArg> n = cmd.nextNonOption("format",
                    (prefix, suffix) -> NArgCompleteResult.ofCandidates(
                            NArgCompleteCandidate.of("format"),
                            NArgCompleteCandidate.of("file")));
            if (n.isPresent()) continue;
            if (cmd.isCompleteMode()) { cmd.skip(); continue; }
            cmd.throwUnexpectedArgument();
        }
        NArgCompleteResult result = cmd.completeResult();
        Assertions.assertNotNull(result);
        Set<String> cands = candidateStrings(result);
        Assertions.assertTrue(cands.contains("format") || cands.contains("file"),
                "Expected custom candidates, got: " + cands);
    }

    @Test
    public void testNonOption_newApi_displayOnly() {
        // nextNonOption(String display) with no finder — display hint becomes the candidate
        NCmdLine cmd = completeAt(0, "");
        while (cmd.hasNext()) {
            NOptional<NArg> n = cmd.nextNonOption("myValue");
            if (n.isPresent()) continue;
            if (cmd.isCompleteMode()) { cmd.skip(); continue; }
            cmd.throwUnexpectedArgument();
        }
        NArgCompleteResult result = cmd.completeResult();
        Assertions.assertNotNull(result);
        Assertions.assertTrue(candidateStrings(result).contains("myValue"),
                "Expected display hint as candidate, got: " + candidateStrings(result));
    }

    // ======================================================== MULTI-WORD

    @Test
    public void testMultiWord_firstPosition() {
        // ["sub", "--f"] — cursor at word 1 — should suggest "--flag"
        NCmdLine cmd = completeAt(1, "sub", "--f");
        while (cmd.hasNext()) {
            NOptional<NArg> sub = cmd.next("sub");
            if (sub.isPresent()) continue;
            NOptional<NArg> flag = cmd.next(NArgType.FLAG, "--flag");
            if (flag.isPresent()) continue;
            if (cmd.isCompleteMode()) { cmd.skip(); continue; }
            cmd.throwUnexpectedArgument();
        }
        NArgCompleteResult result = cmd.completeResult();
        Assertions.assertNotNull(result);
        Assertions.assertTrue(candidateStrings(result).contains("--flag"),
                "Expected --flag at word 1, got: " + candidateStrings(result));
    }

    @Test
    public void testMultiWord_secondPosition_valueComplete() {
        // ["sub", "--file", ""] — cursor at word 2 — should return FILENAMES
        NCmdLine cmd = completeAt(2, "sub", "--file", "");
        while (cmd.hasNext()) {
            NOptional<NArg> sub = cmd.next("sub");
            if (sub.isPresent()) continue;
            NOptional<NArg> entry = cmd.next(NArgType.ENTRY, "file path",
                    (prefix, suffix) -> NArgCompleteResult.ofFlags(NArgCompleteFlag.FILENAMES),
                    "--file");
            if (entry.isPresent()) continue;
            if (cmd.isCompleteMode()) { cmd.skip(); continue; }
            cmd.throwUnexpectedArgument();
        }
        NArgCompleteResult result = cmd.completeResult();
        Assertions.assertNotNull(result);
        Assertions.assertTrue(flagSet(result).contains(NArgCompleteFlag.FILENAMES),
                "Expected FILENAMES at word 2, got: " + flagSet(result));
    }

    // ============================================================= EXEC MODE

    @Test
    public void testExecMode_returnsNull() {
        // No complete() call → exec mode → completeResult() must be null
        NCmdLine cmd = NCmdLine.of(Arrays.asList("--file", "value"));
        while (cmd.hasNext()) {
            NOptional<NArg> n = cmd.next(NArgType.ENTRY, "--file");
            if (n.isPresent()) continue;
            cmd.throwUnexpectedArgument();
        }
        Assertions.assertNull(cmd.completeResult(),
                "completeResult() must be null in exec mode");
    }
}
