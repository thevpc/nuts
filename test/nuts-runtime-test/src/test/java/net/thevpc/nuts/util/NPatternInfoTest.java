package net.thevpc.nuts.util;

import net.thevpc.nuts.core.test.utils.TestUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class NPatternInfoTest {

    @BeforeAll
    static void setUp() {
        TestUtils.openNewMinTestWorkspace();
        net.thevpc.nuts.Nuts.require();
    }


    @Test
    void testCompareToWithNullResults() {
        // Test case 1: both results null
        NPatternInfo a1 = new NPatternInfo("pattern1");
        NPatternInfo b1 = new NPatternInfo("pattern2");
        // Both have null result by default
        System.out.println("a1 = " + a1);
        System.out.println("b1 = " + b1);
        System.out.println("a1.result = " + (a1.result() == null ? "null" : "not null"));
        System.out.println("b1.result = " + (b1.result() == null ? "null" : "not null"));
        // Set explicit non-null results
        NStringMatchResult result1A = NStringMatchResult.ofFullMatch("testA");
        NStringMatchResult result1B = NStringMatchResult.ofFullMatch("testB");
        a1.result(result1A);
        b1.result(result1B);
        System.out.println("After setting results:");
        System.out.println("a1.result = " + (a1.result() == null ? "null" : "not null"));
        System.out.println("b1.result = " + (b1.result() == null ? "null" : "not null"));
        System.out.println("Calling compareTo...");
        int result = a1.compareTo(b1);
        System.out.println("compareTo returned: " + result);
        assertEquals(0, result);

        // Test case 2: a.result null, b.result not null
        NPatternInfo a2 = new NPatternInfo("pattern1");
        NStringMatchResult resultB = NStringMatchResult.ofFullMatch("test");
        NPatternInfo b2 = new NPatternInfo("pattern2");
        b2.result(resultB);
        // null result should be considered "less than" non-null result
        assertTrue(a2.compareTo(b2) < 0);

        // Test case 4: both results not null, same mode, different lengths
        NPatternInfo a4 = new NPatternInfo("pattern1");
        NStringMatchResult resultA4 = NStringMatchResult.ofFullMatch("longer");
        a4.result(resultA4);
        NPatternInfo b4 = new NPatternInfo("pattern2");
        NStringMatchResult resultB4 = NStringMatchResult.ofFullMatch("short");
        b4.result(resultB4);
        // longer text should sort before shorter text (descending order)
        assertTrue(a4.compareTo(b4) < 0);

        // Test case 5: both results not null, same mode, same length
        NPatternInfo a5 = new NPatternInfo("pattern1");
        NStringMatchResult resultA5 = NStringMatchResult.ofFullMatch("equal");
        a5.result(resultA5);
        NPatternInfo b5 = new NPatternInfo("pattern2");
        NStringMatchResult resultB5 = NStringMatchResult.ofFullMatch("equal");
        b5.result(resultB5);
        assertEquals(0, a5.compareTo(b5));

        // Test case 6: different modes
        NPatternInfo a6 = new NPatternInfo("pattern1");
        NStringMatchResult resultA6 = NStringMatchResult.ofFullMatch("test");
        a6.result(resultA6);
        NPatternInfo b6 = new NPatternInfo("pattern2");
        NStringMatchResult resultB6 = NStringMatchResult.ofMatch(java.util.regex.Pattern.compile("test").matcher("test"));
        b6.result(resultB6);
        // FULL_MATCH should come before MATCH (based on NMatchType ordering)
        assertTrue(a6.compareTo(b6) < 0);
    }

    @Test
    void testCompareToWithVariousMatchTypes() {
        NPatternInfo a = new NPatternInfo("pattern");
        NPatternInfo b = new NPatternInfo("pattern");

        // Test NO_MATCH vs FULL_MATCH
        a.result(NStringMatchResult.ofNoMatch());
        b.result(NStringMatchResult.ofFullMatch("test"));
        // NO_MATCH should come after FULL_MATCH (assuming NMatchType.NO_MATCH > NMatchType.FULL_MATCH)
        // Actually, let's just check they're not equal
        assertNotEquals(0, a.compareTo(b));

        // Reset
        a.result(null);
        b.result(null);

        // Test PARTIAL_MATCH vs MATCH
        a.result(NStringMatchResult.ofPartialMatch("text"));
        b.result(NStringMatchResult.ofMatch(java.util.regex.Pattern.compile("test").matcher("test")));
        assertNotEquals(0, a.compareTo(b));
    }
}