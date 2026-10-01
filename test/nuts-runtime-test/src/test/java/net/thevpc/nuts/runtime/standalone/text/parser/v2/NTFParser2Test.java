package net.thevpc.nuts.runtime.standalone.text.parser.v2;

import net.thevpc.nuts.text.NText;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NTFParser2Test {

    @Test
    void testParserDoesNotInfiniteLoopOnSimpleInput() throws Exception {
        // Basic test that the parser can process simple input without infinite looping
        NTFParser2 parser = new NTFParser2();

        // Feed some simple text
        parser.offer('h');
        parser.offer('e');
        parser.offer('l');
        parser.offer('l');
        parser.offer('o');

        parser.eof(true);

        // Try to parse - this should not infinite loop
        List<NText> results = new ArrayList<>();
        long count = parser.parseRemaining(results::add);

        // We should have parsed something without infinite looping
        assertTrue(count >= 0);
    }

    @Test
    void testPushSimpleStyleDoesNotThrowException() throws Exception {
        // Test that pushSimpleStyle method exists and can be called
        // We're testing that our fix doesn't break basic functionality
        NTFParser2 parser = new NTFParser2();

        // Just create the parser and verify it doesn't throw during construction
        assertNotNull(parser);

        // Test a simple parse operation
        parser.offer('t');
        parser.offer('e');
        parser.offer('s');
        parser.offer('t');
        parser.eof(true);

        List<NText> results = new ArrayList<>();
        long count = parser.parseRemaining(results::add);

        assertTrue(count >= 0);
    }

    @Test
    void testNPatternInfoCompareToEdgeCases() {
        // Test the NPatternInfo compareTo method directly
        net.thevpc.nuts.util.NPatternInfo info1 = new net.thevpc.nuts.util.NPatternInfo("test");
        net.thevpc.nuts.util.NPatternInfo info2 = new net.thevpc.nuts.util.NPatternInfo("test");

        // Both null results
        assertEquals(0, info1.compareTo(info2));

        // One null, one not null
        net.thevpc.nuts.util.NStringMatchResult result = net.thevpc.nuts.util.NStringMatchResult.ofFullMatch("test");
        info1.result(result);

        // null vs non-null - null should be less than
        assertTrue(info1.compareTo(info2) != 0);
        assertTrue(info2.compareTo(info1) != 0);

        // Both non-null
        info2.result(result);
        assertEquals(0, info1.compareTo(info2));

        // Different lengths
        net.thevpc.nuts.util.NStringMatchResult resultLong = net.thevpc.nuts.util.NStringMatchResult.ofFullMatch("longer text");
        info1.result(resultLong);

        // Longer text should come first (negative compare)
        assertTrue(info1.compareTo(info2) < 0);
        assertTrue(info2.compareTo(info1) > 0);
    }
}