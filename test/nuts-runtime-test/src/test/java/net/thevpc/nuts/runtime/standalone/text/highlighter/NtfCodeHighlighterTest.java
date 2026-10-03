package net.thevpc.nuts.runtime.standalone.text.highlighter;
import net.thevpc.nuts.core.test.utils.TestUtils;

import net.thevpc.nuts.spi.NCodeHighlighter;
import net.thevpc.nuts.text.NText;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeAll;

class NtfCodeHighlighterTest {
    @BeforeAll
    static void setUp() {
        TestUtils.openNewMinTestWorkspace();
        net.thevpc.nuts.Nuts.require();
    }

    @Test
    void testHighlighterExists() {
        // Test that we can get the highlighter by id
        NCodeHighlighter highlighter = NCodeHighlighter.of("ntf");
        assertNotNull(highlighter);
        assertEquals("ntf", highlighter.id());
    }

    @Test
    void testPlainText() {
        NCodeHighlighter highlighter = NCodeHighlighter.of("ntf");
        NText result = highlighter.stringToText("Hello world");
        assertNotNull(result);
        assertEquals("Hello world", result.filteredText());
    }

    @Test
    void testItalicText() {
        NCodeHighlighter highlighter = NCodeHighlighter.of("ntf");
        NText result = highlighter.stringToText("This is *italic* text");
        assertNotNull(result);
        assertEquals("This is italic text", result.filteredText());
    }

    @Test
    void testBoldText() {
        NCodeHighlighter highlighter = NCodeHighlighter.of("ntf");
        NText result = highlighter.stringToText("This is **bold** text");
        assertNotNull(result);
        assertEquals("This is bold text", result.filteredText());
    }

    @Test
    void testInlineCode() {
        NCodeHighlighter highlighter = NCodeHighlighter.of("ntf");
        NText result = highlighter.stringToText("This is `code` text");
        assertNotNull(result);
        assertEquals("This is code text", result.filteredText());
    }

    @Test
    void testMultipleFormats() {
        NCodeHighlighter highlighter = NCodeHighlighter.of("ntf");
        NText result = highlighter.stringToText("This is *italic*, **bold**, and `code`");
        assertNotNull(result);
        assertEquals("This is italic, bold, and code", result.filteredText());
    }

    @Test
    void testResetBetweenUses() {
        NCodeHighlighter highlighter = NCodeHighlighter.of("ntf");
        
        // First use
        NText result1 = highlighter.stringToText("First *italic*");
        assertNotNull(result1);
        assertEquals("First italic", result1.filteredText());
        
        // Second use - should not be affected by first
        NText result2 = highlighter.stringToText("Second **bold**");
        assertNotNull(result2);
        assertEquals("Second bold", result2.filteredText());
    }
}
