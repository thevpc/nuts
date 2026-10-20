package net.thevpc.nuts.core.test;

import net.thevpc.nuts.boot.internal.util.NBootUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

public class NBootUtilsSimilarityTest {

    @Test
    public void exactTokenSetsHaveMaximumSimilarity() {
        String[] tokens = {"boot", "runtime"};
        Assertions.assertEquals(1.0, NBootUtils.fuzzyJaccardSimilarity(tokens, tokens));
        Assertions.assertEquals(1.0, NBootUtils.computeOptionSimilarity("--boot-runtime", "--runtime-boot"));
    }

    @Test
    public void damerauLevenshteinSupportsEditsAndTranspositions() {
        Assertions.assertEquals(3, NBootUtils.damerauLevenshtein("kitten", "sitting"));
        Assertions.assertEquals(1, NBootUtils.damerauLevenshtein("boot", "boto"));
        Assertions.assertEquals(1, NBootUtils.damerauLevenshtein("boto", "boot"));
    }

    @Test
    public void longInvalidOptionSkipsTypoMatching() {
        char[] chars = new char[10_000];
        Arrays.fill(chars, 'x');
        Assertions.assertNull(NBootUtils.damerauLevenshteinClosest(
                0.5, new String(chars), new String[]{"--version", "--workspace"}));
    }

    @Test
    public void shortInvalidOptionStillGetsTypoSuggestion() {
        Assertions.assertEquals("--version", NBootUtils.damerauLevenshteinClosest(
                0.5, "--versoin", new String[]{"--version", "--workspace"}));
    }
}
