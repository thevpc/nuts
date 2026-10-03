package net.thevpc.nuts.runtime.standalone.text.parser.v2;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.text.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link MTFParser}.
 *
 * <p>MTF is a hybrid of the NTF format (see {@code ntf-help.ntf}) and a subset of markdown:
 * {@code *italic*}, {@code **bold**}, {@code ***bold italic***}, {@code `inline code`} and
 * {@code [[lang: source]]} embedded sources.
 */
class MTFParserTest {

    @BeforeAll
    static void setUp() {
        TestUtils.openNewMinTestWorkspace();
        net.thevpc.nuts.Nuts.require();
    }

    // ------------------------------------------------------------------ helpers

    private static MTFParser parser(String... chunks) {
        MTFParser parser = new MTFParser();
        parser.reset();
        for (String chunk : chunks) {
            parser.offer(chunk);
        }
        parser.eof(true);
        return parser;
    }

    private static NText parse(String input) {
        return parser(input).readFully();
    }

    /**
     * Parses the input in one shot and asserts the resulting text.
     */
    private static NText assertParses(String input, String expected) {
        NText result = parse(input);
        assertNotNull(result, "null result for [" + input + "]");
        assertEquals(expected, result.filteredText(), "unexpected text for [" + input + "]");
        return result;
    }

    /**
     * The parsed text must never lose a single character of the input.
     */
    private static void assertLossless(String input, String expected) {
        assertParses(input, expected);
    }

    private static List<NText> children(NText text) {
        if (text instanceof NTextList) {
            return ((NTextList) text).children();
        }
        List<NText> all = new ArrayList<>();
        all.add(text);
        return all;
    }

    // ------------------------------------------------------------------ plain

    @Test
    void testPlainText() {
        assertParses("Hello world", "Hello world");
    }

    @Test
    void testEmptyInput() {
        NText result = parse("");
        assertNotNull(result);
        assertEquals("", result.filteredText());
        assertTrue(result.isEmpty());
    }

    @Test
    void testBlankInput() {
        assertParses("   ", "   ");
        assertParses("\n", "\n");
        assertParses("\r\n", "\r\n");
    }

    @Test
    void testPlainTextIsPlain() {
        assertTrue(parse("Hello world") instanceof NTextPlain);
    }

    @Test
    void testParseIncrementalApi() {
        MTFParser parser = new MTFParser();
        StringBuilder sb = new StringBuilder();
        parser.parseIncremental("Hello ".toCharArray(), n -> sb.append(n.filteredText()));
        parser.parseIncremental("world".toCharArray(), n -> sb.append(n.filteredText()));
        assertEquals("Hello world", sb.toString());
    }

    @Test
    void testParseRemainingApi() {
        MTFParser parser = new MTFParser();
        parser.reset();
        parser.offer("##:success:ok##");
        List<NText> nodes = new ArrayList<>();
        parser.eof(true);
        assertEquals(1, parser.parseRemaining(nodes::add));
        assertEquals(1, nodes.size());
        assertEquals("ok", nodes.get(0).filteredText());
    }

    @Test
    void testResetDiscardsPreviousInput() {
        MTFParser parser = parser("##:success:ok##");
        assertEquals("ok", parser.readFully().filteredText());
        parser.reset();
        parser.offer("*it*");
        parser.eof(true);
        assertEquals("it", parser.readFully().filteredText());
    }

    // ------------------------------------------------------------------ markdown

    @Test
    void testItalicText() {
        NText result = assertParses("This is *italic* text", "This is italic text");
        List<NText> all = children(result);
        assertEquals(3, all.size());
        assertTrue(all.get(1) instanceof NTextStyled);
        assertEquals(NTextStyle.italic(),
                ((NTextStyled) all.get(1)).styles().get(0));
    }

    @Test
    void testBoldText() {
        NText result = assertParses("This is **bold** text", "This is bold text");
        List<NText> all = children(result);
        assertEquals(3, all.size());
        assertTrue(all.get(1) instanceof NTextStyled);
        assertEquals(NTextStyle.bold(),
                ((NTextStyled) all.get(1)).styles().get(0));
    }

    @Test
    void testBoldItalicText() {
        assertParses("This is ***both*** text", "This is both text");
    }

    @Test
    void testInlineCode() {
        NText result = assertParses("This is `code` text", "This is code text");
        List<NText> all = children(result);
        assertEquals(3, all.size());
        assertTrue(all.get(1) instanceof NTextStyled);
        assertEquals(NTextStyle.string(),
                ((NTextStyled) all.get(1)).styles().get(0));
    }

    @Test
    void testMultipleFormats() {
        assertParses("This is *italic*, **bold**, and `code`",
                "This is italic, bold, and code");
    }

    @Test
    void testNestedFormatting() {
        assertParses("This is *italic and **bold** inside*",
                "This is italic and bold inside");
    }

    @Test
    void testDeeplyNestedFormatting() {
        assertParses("**bold with *italic* inside**",
                "bold with italic inside");
    }

    @Test
    void testInlineCodeInsideEmphasis() {
        assertParses("*a `b` c*", "a b c");
    }

    @Test
    void testEmphasisInsideInlineCodeIsLiteral() {
        assertParses("`*not italic*`", "*not italic*");
    }

    @Test
    void testEmphasisNextToPunctuation() {
        assertParses("(*a*) and [*b*]", "(a) and [b]");
    }

    @Test
    void testAdjacentEmphasis() {
        assertLossless("*a**b*", "a**b");
    }

    @Test
    void testUnclosedItalicIsLiteral() {
        assertLossless("This is *unclosed italic", "This is *unclosed italic");
    }

    @Test
    void testUnclosedInlineCodeIsLiteral() {
        assertLossless("This is `unclosed code", "This is `unclosed code");
    }

    @Test
    void testOpeningDelimiterFollowedByBlankIsLiteral() {
        assertLossless("* not italic", "* not italic");
        assertLossless("** not bold", "** not bold");
        assertLossless("` not code", "` not code");
    }

    @Test
    void testEmphasisDoesNotSpanLines() {
        assertLossless("*not\nclosed*", "*not\nclosed*");
        assertLossless("`not\nclosed`", "`not\nclosed`");
    }

    @Test
    void testEmphasisInsideALine() {
        assertParses("multi\n*it*\nline", "multi\nit\nline");
    }

    @Test
    void testBulletMarkerIsPreserved() {
        assertLossless("* one\n* two", "* one\n* two");
    }

    @Test
    void testBulletMarkerIsNotEmphasised() {
        NText result = parse("* one");
        assertEquals("* one", result.filteredText());
        assertTrue(result instanceof NTextPlain);
    }

    @Test
    void testStarInsideWord() {
        assertLossless("2*3*4", "234");
    }

    @Test
    void testEmptyEmphasisIsLiteral() {
        assertLossless("**", "**");
        assertLossless("****", "****");
    }

    // ------------------------------------------------------------------ escapes

    @Test
    void testEscapedMarkdown() {
        assertLossless("a\\*b", "a*b");
        assertLossless("a\\`b", "a`b");
        assertLossless("a\\[b", "a[b");
        assertLossless("\\*not italic\\*", "*not italic*");
    }

    @Test
    void testEscapedNtf() {
        assertLossless("a\\#b", "a#b");
        assertLossless("a\\\\b", "a\\b");
        assertLossless("\\`\\`\\`", "```");
    }

    @Test
    void testBackSlashOfUnknownEscapeIsNotAnEscape() {
        assertLossless("\\a", "\\a");
    }

    @Test
    void testSilentSeparatorIsNotDisplayed() {
        assertLossless("##{s12:AA##:12:BB##\u001E##:6:CC##DD}##", "AABBCCDD");
    }

    @Test
    void testEscapedSilentSeparatorIsIgnored() {
        assertLossless("a\\\u001Eb", "ab");
    }

    // ------------------------------------------------------------------ ntf styles

    @Test
    void testSingleSharpIsPlainText() {
        // per ntf-help.ntf '#Text#' is plain text, '##Text##' is primary1
        assertLossless("#Bold#", "#Bold#");
    }

    @Test
    void testPrimaryStyles() {
        assertParses("##P2##", "P2");
        assertParses("###P3###", "P3");
        assertParses("########P8########", "P8");
    }

    @Test
    void testPrimaryStyleKeepsItsLevel() {
        // '###P3###' opens a 3 sharps style and closes it with the 2 sharps one
        NText result = parse("###P3###");
        assertTrue(result instanceof NTextStyled);
        assertEquals(NTextStyle.primary(2),
                ((NTextStyled) result).styles().get(0));
    }

    @Test
    void testSimpleStyle() {
        NText result = assertParses("##:success:ok##", "ok");
        assertTrue(result instanceof NTextStyled);
        assertEquals(NTextStyle.success(),
                ((NTextStyled) result).styles().get(0));
    }

    @Test
    void testSimpleStyleSeparatorMustBeAdjacentToText() {
        // the style separator is a single character, the blank belongs to the text
        assertParses("##:success: ok##", " ok");
    }

    @Test
    void testUnknownStyleIsKeptAsPlainText() {
        assertLossless("##:unknownstyle:ok", "##:unknownstyle:ok");
    }

    @Test
    void testNestedStyles() {
        assertParses("##:error:##:bold:deep## end##", "deep end");
    }

    @Test
    void testCompositeStyle() {
        assertParses("##{success:ok##:error:ko##}##", "okko");
    }

    @Test
    void testCompositeStyleClosingIsStripped() {
        NText result = parse("##{success:ok##:error:ko##}##");
        assertTrue(result instanceof NTextStyled);
        assertEquals(NTextStyle.success(),
                ((NTextStyled) result).styles().get(0));
    }

    @Test
    void testUnterminatedStyleIsFlushed() {
        assertLossless("##:success:ok", "ok");
    }

    @Test
    void testStyleMarkersAreNotDisplayed() {
        assertParses("a ##:warn:w## b", "a w b");
    }

    // ------------------------------------------------------------------ ntf titles

    @Test
    void testTitle() {
        NText result = parse("#) Title");
        assertTrue(result instanceof NTextTitle);
        NTextTitle title = (NTextTitle) result;
        assertEquals(1, title.level());
        assertEquals("Title", title.child().filteredText());
        assertEquals("Title\n", title.filteredText());
    }

    @Test
    void testSecondLevelTitle() {
        NText result = parse("##) Title");
        assertTrue(result instanceof NTextTitle);
        assertEquals(2, ((NTextTitle) result).level());
    }

    @Test
    void testTitleStopsAtNewLine() {
        NText result = assertParses("#) Title\nnext", "Title\nnext");
        assertTrue(result instanceof NTextList);
        assertTrue(((NTextList) result).get(0) instanceof NTextTitle);
        assertTrue(((NTextList) result).get(1) instanceof NTextPlain);
    }

    @Test
    void testTitleWithStyles() {
        assertParses("###) The *title*", "The title\n");
    }

    @Test
    void testTitleMarkerNotAtLineStartIsLiteral() {
        assertLossless("a #) not a title", "a #) not a title");
    }

    // ------------------------------------------------------------------ ntf code

    @Test
    void testVerbatimText() {
        assertParses("```*not italic*```", "*not italic*");
    }

    @Test
    void testFormattedCodeBlock() {
        NText result = parse("```java int x=1;```");
        assertTrue(result instanceof NTextCode);
        NTextCode code = (NTextCode) result;
        assertEquals("java", code.qualifier());
        assertEquals("int x=1;", code.value());
    }

    @Test
    void testCodeBlockKeepsNewLines() {
        NText result = parse("```java:int a=1;\nint b=2;```");
        assertTrue(result instanceof NTextCode);
        assertEquals("java", ((NTextCode) result).qualifier());
        assertEquals("int a=1;\nint b=2;", ((NTextCode) result).value());
    }

    @Test
    void testVerbatimTextUsesTheFirstTokenAsQualifier() {
        // per ntf-help.ntf '```lang code```' declares a formatted code block
        NText result = parse("```plain text```");
        assertTrue(result instanceof NTextCode);
        assertEquals("plain", ((NTextCode) result).qualifier());
        assertEquals("text", ((NTextCode) result).value());
    }

    @Test
    void testEmptyCodeBlock() {
        assertParses("``````", "");
    }

    @Test
    void testUnterminatedCodeBlockIsFlushed() {
        assertParses("```abc", "abc");
    }

    // ------------------------------------------------------------------ embedded sources

    @Test
    void testEmbeddedSource() {
        NText result = assertParses("This is [[test:id]] text", "This is id text");
        assertTrue(result instanceof NTextList);
        NTextList list = (NTextList) result;
        assertEquals(3, list.size());
        assertTrue(list.get(0) instanceof NTextPlain);
        assertEquals("This is ", list.get(0).filteredText());
        assertTrue(list.get(1) instanceof NTextCode);
        NTextCode code = (NTextCode) list.get(1);
        assertEquals("test", code.qualifier());
        assertEquals("id", code.value());
        assertEquals(":", code.separator());
        assertTrue(list.get(2) instanceof NTextPlain);
        assertEquals(" text", list.get(2).filteredText());
    }

    @Test
    void testEmbeddedSourceWithoutSpace() {
        NText result = parse("[[java:int x=1;]]");
        assertTrue(result instanceof NTextCode);
        assertEquals("java", ((NTextCode) result).qualifier());
        assertEquals("int x=1;", ((NTextCode) result).value());
    }

    @Test
    void testEmbeddedSourceWithSpace() {
        NText result = parse("[[java: int x=1;]]");
        assertEquals("java", ((NTextCode) result).qualifier());
        assertEquals("int x=1;", ((NTextCode) result).value());
    }

    @Test
    void testEmbeddedSourceSpansLines() {
        NText result = parse("[[java:int a=1;\nint b=2;]]");
        assertEquals("java", ((NTextCode) result).qualifier());
        assertEquals("int a=1;\nint b=2;", ((NTextCode) result).value());
    }

    @Test
    void testEmbeddedSourceEmptyContent() {
        NText result = parse("[[java:]]");
        assertEquals("", result.filteredText());
    }

    @Test
    void testEmbeddedSourceUnescapesClosingBracket() {
        NText result = parse("[[java:a\\]b]]");
        assertEquals("a]b", ((NTextCode) result).value());
    }

    @Test
    void testEmbeddedSourceWithoutLangIsLiteral() {
        assertLossless("[[:x]]", "[[:x]]");
    }

    @Test
    void testUnclosedEmbeddedSourceIsLiteral() {
        assertLossless("This is [[unclosed id ref", "This is [[unclosed id ref");
        assertLossless("[[java:x]", "[[java:x]");
    }

    @Test
    void testSingleBracketIsLiteral() {
        assertLossless("[a]", "[a]");
        assertLossless("a[b[c", "a[b[c");
    }

    @Test
    void testEmbeddedSourceWithOtherFormats() {
        assertParses("This is *italic*, [[ref:id]], and `code`",
                "This is italic, id, and code");
    }

    @Test
    void testEmbeddedSourceInsideEmphasis() {
        NText result = parse("*see [[java:x]]*");
        assertEquals("see x", result.filteredText());
    }

    @Test
    void testEmbeddedSourceDoesNotCloseEmphasis() {
        assertParses("[[java:a*b]]", "a*b");
    }

    // ------------------------------------------------------------------ mixed

    @Test
    void testMixedDocument() {
        assertParses("#) Title\n*a* and **b** and `c` and [[java:x]]",
                "Title\na and b and c and x");
    }

    @Test
    void testNtfStyleInsideMarkdown() {
        NText result = assertParses("*##:success:ok##*", "ok");
        assertTrue(result instanceof NTextStyled);
        assertEquals(NTextStyle.italic(),
                ((NTextStyled) result).styles().get(0));
        NText inner = ((NTextStyled) result).child();
        assertTrue(inner instanceof NTextStyled);
        assertEquals(NTextStyle.success(),
                ((NTextStyled) inner).styles().get(0));
    }

    @Test
    void testMarkdownInsideNtfStyle() {
        NText result = assertParses("##:success:*ok*##", "ok");
        assertTrue(result instanceof NTextStyled);
        assertEquals(NTextStyle.success(),
                ((NTextStyled) result).styles().get(0));
    }

    // ------------------------------------------------------------------ incremental

    /**
     * Feeding the parser one character at a time must produce the very same text as
     * feeding it the whole input at once.
     */
    private static void assertIncrementalEquals(String input) {
        NText whole = parse(input);

        MTFParser parser = new MTFParser();
        parser.reset();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < input.length(); i++) {
            parser.offer(input.charAt(i));
            NText node;
            while ((node = parser.read()) != null) {
                sb.append(node.filteredText());
            }
        }
        parser.eof(true);
        NText remaining = parser.parseRemaining();
        if (remaining != null) {
            sb.append(remaining.filteredText());
        }
        assertEquals(whole.filteredText(), sb.toString(),
                "incremental parsing differs for [" + input + "]");
        assertFalse(parser.isIncomplete(), "parser still incomplete for [" + input + "]");
    }

    @Test
    void testIncrementalPlainText() {
        assertIncrementalEquals("Hello world");
    }

    @Test
    void testIncrementalItalic() {
        assertIncrementalEquals("This is *italic* text");
    }

    @Test
    void testIncrementalBold() {
        assertIncrementalEquals("This is **bold** text");
    }

    @Test
    void testIncrementalUnclosedItalic() {
        assertIncrementalEquals("*unclosed");
    }

    @Test
    void testIncrementalInlineCode() {
        assertIncrementalEquals("a `b` c");
    }

    @Test
    void testIncrementalEmbeddedSource() {
        assertIncrementalEquals("[[java:int x=1;]]");
    }

    @Test
    void testIncrementalUnclosedEmbeddedSource() {
        assertIncrementalEquals("[[java:x");
    }

    @Test
    void testIncrementalTitle() {
        assertIncrementalEquals("#) Title\nrest");
    }

    @Test
    void testIncrementalStyle() {
        assertIncrementalEquals("##:success:ok##");
    }

    @Test
    void testIncrementalCompositeStyle() {
        assertIncrementalEquals("##{success:ok##:error:ko##}##");
    }

    @Test
    void testIncrementalCodeBlock() {
        assertIncrementalEquals("```java\nint x=1;\n```");
    }

    @Test
    void testIncrementalBullet() {
        assertIncrementalEquals("* one\n* two");
    }

    @Test
    void testIncrementalMixedDocument() {
        assertIncrementalEquals("#) T\n*a* and **b** and `c` and [[java:x]]\n##:success:ok##\n");
    }

    /**
     * The parser must also behave the same way when the input is split in two chunks.
     */
    @Test
    void testTwoChunksEqualsOneChunk() {
        String input = "#) Title\nsome *it* and `co` and [[java:x]]";
        NText whole = parse(input);
        for (int split = 0; split <= input.length(); split++) {
            MTFParser parser = new MTFParser();
            parser.reset();
            parser.offer(input.substring(0, split));
            parser.offer(input.substring(split));
            parser.eof(true);
            assertEquals(whole.filteredText(), parser.readFully().filteredText(),
                    "split at " + split);
        }
    }
}
