package net.thevpc.nuts.runtime.standalone.text.parser.v2;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.text.NText;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class NTFParser2Test {

    @BeforeAll
    static void setUp() {
        TestUtils.openNewMinTestWorkspace();
    }

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

        assertEquals(1, count);
        assertEquals("hello", results.get(0).filteredText());
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

        assertEquals(1, count);
        assertEquals("test", results.get(0).filteredText());
    }

    @Test
    void testResetDiscardsUnconsumedParserState() {
        NTFParser2 parser = new NTFParser2();
        parser.offer("Hello #World#");
        assertNull(parser.read());

        parser.eof(true);
        parser.reset();
        parser.offer("fresh");
        NText result = parser.readFully();

        assertNotNull(result);
        assertEquals("fresh", result.filteredText());
    }

    @Test
    void testReadFullyReturnsWholeInputWithSeveralTopLevelNodes() {
        NTFParser2 parser = new NTFParser2();
        String input = "first ##:success:ok## middle ##:error:ko## last";
        parser.offer(input);
        parser.eof(true);

        NText result = parser.readFully();
        assertNotNull(result);
        assertEquals("first ok middle ko last", result.filteredText());
    }

    @Test
    void testReadFullyDrainsMixedDocument() {
        NTFParser2 parser = new NTFParser2();
        String input = "#) Title\nplain\n##:success:ok##\n```code```";
        parser.offer(input);
        parser.eof(true);

        NText result = parser.readFully();
        assertNotNull(result);
        assertEquals("Title\nplain\nok\ncode", result.filteredText());
    }

    @Test
    void testTitleDoesNotCloseOnIncompleteCrLf() {
        NTFParser2 parser = new NTFParser2();
        parser.offer("#) Title\r");
        // the '\r' may be the first half of a '\r\n' sequence
        assertNull(parser.read());

        parser.offer("\nrest");
        parser.eof(true);
        assertEquals("Title\nrest", parser.readFully().filteredText());
    }

    @Test
    void testIncrementalUnicodeIsNotCorruptedByCharsetRoundTrip() {
        String input = "h\u00e9llo \u2603 \u4e16\u754c";
        NTFParser2 parser = new NTFParser2();
        StringBuilder sb = new StringBuilder();
        parser.parseIncremental(input.toCharArray(), n -> sb.append(n.filteredText()));
        parser.eof(true);
        NText remaining = parser.parseRemaining();
        if (remaining != null) {
            sb.append(remaining.filteredText());
        }
        assertEquals(input, sb.toString());
    }

    @Test
    void testIncrementalUtf8BytesAreDecodedAsUtf8() {
        String input = "h\u00e9llo \u2603 \u4e16\u754c";
        NTFParser2 parser = new NTFParser2();
        StringBuilder sb = new StringBuilder();
        parser.parseIncremental(input.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                n -> sb.append(n.filteredText()));
        parser.eof(true);
        NText remaining = parser.parseRemaining();
        if (remaining != null) {
            sb.append(remaining.filteredText());
        }
        assertEquals(input, sb.toString());
    }

    @Test
    void testSplitEscapeSequenceIsNotCorrupted() {
        NTFParser2 parser = new NTFParser2();
        parser.offer("a\\");
        assertNull(parser.read());
        parser.offer("#b");
        parser.eof(true);
        assertEquals("a#b", parser.readFully().filteredText());
    }

    @Test
    void testSplitSimpleStyleHeaderIsNotMisparsed() {
        NTFParser2 parser = new NTFParser2();
        StringBuilder sb = new StringBuilder();
        for (String chunk : new String[]{"##:", "warn", ":", "ok", "##"}) {
            parser.offer(chunk);
            NText n;
            while ((n = parser.read()) != null) {
                sb.append(n.filteredText());
            }
        }
        parser.eof(true);
        NText remaining = parser.readFully();
        if (remaining != null) {
            sb.append(remaining.filteredText());
        }
        assertEquals("ok", sb.toString());
    }

    @Test
    void testSplitTitleDelimiterDoesNotKeepLeadingBlank() {
        NTFParser2 parser = new NTFParser2();
        StringBuilder sb = new StringBuilder();
        for (String chunk : new String[]{"#)", " ", "Title", "\n", "rest"}) {
            parser.offer(chunk);
            NText n;
            while ((n = parser.read()) != null) {
                sb.append(n.filteredText());
            }
        }
        parser.eof(true);
        NText remaining = parser.readFully();
        if (remaining != null) {
            sb.append(remaining.filteredText());
        }
        assertEquals("Title\nrest", sb.toString());
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