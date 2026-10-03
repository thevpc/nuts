package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.spi.NCodeHighlighter;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextCode;
import net.thevpc.nuts.text.NTextList;
import net.thevpc.nuts.text.NTextPlain;
import net.thevpc.nuts.text.NTextStyled;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTextTitle;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MtfCodeHighlighterTest {

    @BeforeAll
    static void setUp() {
        TestUtils.openNewMinTestWorkspace();
        net.thevpc.nuts.Nuts.require();
    }

    private static NCodeHighlighter highlighter() {
        return NCodeHighlighter.of("mtf");
    }

    private static void assertHighlights(String input, String expected) {
        NText result = highlighter().stringToText(input);
        assertNotNull(result);
        assertEquals(expected, result.filteredText(), "unexpected text for [" + input + "]");
    }

    @Test
    void testHighlighterIsRegistered() {
        NCodeHighlighter highlighter = NCodeHighlighter.of("mtf");
        assertNotNull(highlighter);
        assertEquals("mtf", highlighter.id());
    }

    @Test
    void testHighlighterIsReachableByMimeType() {
        assertNotNull(NCodeHighlighter.of("text/x-mtf"));
        assertNotNull(NCodeHighlighter.of("text/mtf"));
    }

    @Test
    void testPlainText() {
        assertHighlights("Hello world", "Hello world");
    }

    @Test
    void testEmptyText() {
        assertHighlights("", "");
    }

    @Test
    void testNullText() {
        NText result = highlighter().stringToText(null);
        assertNotNull(result);
        assertEquals("", result.filteredText());
    }

    @Test
    void testItalicText() {
        NText result = highlighter().stringToText("This is *italic* text");
        assertEquals("This is italic text", result.filteredText());
        assertTrue(result instanceof NTextList);
        NTextList list = (NTextList) result;
        assertEquals(3, list.size());
        assertTrue(list.get(1) instanceof NTextStyled);
        assertEquals(NTextStyle.italic(), ((NTextStyled) list.get(1)).styles().get(0));
    }

    @Test
    void testBoldText() {
        NText result = highlighter().stringToText("This is **bold** text");
        assertTrue(result instanceof NTextList);
        assertTrue(((NTextList) result).get(1) instanceof NTextStyled);
        assertEquals(NTextStyle.bold(),
                ((NTextStyled) ((NTextList) result).get(1)).styles().get(0));
    }

    @Test
    void testInlineCode() {
        NText result = highlighter().stringToText("a `b` c");
        assertTrue(((NTextList) result).get(1) instanceof NTextStyled);
        assertEquals(NTextStyle.string(),
                ((NTextStyled) ((NTextList) result).get(1)).styles().get(0));
    }

    @Test
    void testMultipleFormats() {
        assertHighlights("This is *italic*, **bold**, and `code`",
                "This is italic, bold, and code");
    }

    @Test
    void testNtfStyle() {
        NText result = highlighter().stringToText("##:success:ok##");
        assertTrue(result instanceof NTextStyled);
        assertEquals(NTextStyle.success(), ((NTextStyled) result).styles().get(0));
    }

    @Test
    void testTitle() {
        NText result = highlighter().stringToText("#) Title");
        assertTrue(result instanceof NTextTitle);
        assertEquals(1, ((NTextTitle) result).level());
    }

    @Test
    void testCodeBlock() {
        NText result = highlighter().stringToText("```java int x=1;```");
        assertTrue(result instanceof NTextCode);
        assertEquals("java", ((NTextCode) result).qualifier());
    }

    @Test
    void testEmbeddedSource() {
        NText result = highlighter().stringToText("see [[java:int x=1;]]");
        assertTrue(result instanceof NTextList);
        NTextList list = (NTextList) result;
        assertTrue(list.get(0) instanceof NTextPlain);
        assertTrue(list.get(1) instanceof NTextCode);
        assertEquals("java", ((NTextCode) list.get(1)).qualifier());
        assertEquals("int x=1;", ((NTextCode) list.get(1)).value());
    }

    @Test
    void testUnterminatedMarkupIsNotLost() {
        assertHighlights("*not closed and `not closed either", "*not closed and `not closed either");
    }

    @Test
    void testRepeatedUseIsIndependent() {
        NCodeHighlighter highlighter = highlighter();
        assertEquals("first", highlighter.stringToText("*first*").filteredText());
        assertEquals("second", highlighter.stringToText("**second**").filteredText());
        assertEquals("third", highlighter.stringToText("`third`").filteredText());
    }

    @Test
    void testTokenToText() {
        assertEquals("tok", highlighter().tokenToText("tok", "string").filteredText());
        assertEquals("", highlighter().tokenToText(null, "string").filteredText());
    }
}
