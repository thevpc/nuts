package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.standalone.text.highlighter.TsonCodeHighlighter;
import net.thevpc.nuts.text.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

/**
 * TSON v2.1 highlighter fixtures for Generalized N-Quote Fenced Strings.
 * <p>
 * The highlighter classifies fenced-string tokens as follows:
 * <ul>
 *   <li>opening/closing fences of any length N and literal content  → {@code string} style</li>
 *   <li>the discarded escape quote of a P &gt; N run                → {@code separator} style</li>
 * </ul>
 * A run of exactly 2 quotes (N=1 empty string) is a single {@code string} token.
 */
public class TsonHighlighterTest {

    @BeforeAll
    static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    private static final class Seg {
        NTextStyle style;
        String text;
    }

    private static List<Seg> segments(NText t) {
        List<Seg> all = new ArrayList<>();
        collect(t, all);
        return all;
    }

    private static void collect(NText t, List<Seg> all) {
        if (t instanceof NTextList) {
            for (NText c : ((NTextList) t).children()) {
                collect(c, all);
            }
        } else if (t instanceof NTextStyled) {
            NTextStyled s = (NTextStyled) t;
            Seg seg = new Seg();
            seg.style = s.styles().iterator().next();
            seg.text = flatten(s.child());
            all.add(seg);
        } else if (t instanceof NTextPlain) {
            Seg seg = new Seg();
            seg.style = null;
            seg.text = ((NTextPlain) t).value();
            all.add(seg);
        } else {
            all.addAll(segments(t));
        }
    }

    private static String flatten(NText t) {
        StringBuilder sb = new StringBuilder();
        flatten(t, sb);
        return sb.toString();
    }

    private static void flatten(NText t, StringBuilder sb) {
        if (t instanceof NTextList) {
            for (NText c : ((NTextList) t).children()) {
                flatten(c, sb);
            }
        } else if (t instanceof NTextStyled) {
            flatten(((NTextStyled) t).child(), sb);
        } else if (t instanceof NTextPlain) {
            sb.append(((NTextPlain) t).value());
        }
    }

    private static void check(String input, String[] texts, NTextStyle[] styles) {
        TsonCodeHighlighter h = new TsonCodeHighlighter();
        List<Seg> segs = segments(h.stringToText(input));
        StringBuilder flat = new StringBuilder();
        for (Seg s : segs) {
            flat.append(s.text);
        }
        Assertions.assertEquals(input, flat.toString(), "highlighted text must reproduce input: " + input);
        Assertions.assertEquals(texts.length, segs.size(), "segment count for: " + input);
        for (int i = 0; i < texts.length; i++) {
            Assertions.assertEquals(texts[i], segs.get(i).text, "segment[" + i + "] text for: " + input);
            Assertions.assertEquals(styles[i], segs.get(i).style, "segment[" + i + "] style for: " + input);
        }
    }

    @Test
    public void testV21_highlight_N1_doubled_quote() {
        // "a""b" : fence, content "a\"", discarded quote (separator), content, fence
        check("\"a\"\"b\"",
                new String[]{"\"", "a\"", "\"", "b", "\""},
                new NTextStyle[]{NTextStyle.string(), NTextStyle.string(), NTextStyle.separator(), NTextStyle.string(), NTextStyle.string()});
    }

    @Test
    public void testV21_highlight_N3_internal_run() {
        // """a""""b""" : fence(3), content a""" , discarded quote, content b, fence(3)
        check("\"\"\"a\"\"\"\"b\"\"\"",
                new String[]{"\"\"\"", "a\"\"\"", "\"", "b", "\"\"\""},
                new NTextStyle[]{NTextStyle.string(), NTextStyle.string(), NTextStyle.separator(), NTextStyle.string(), NTextStyle.string()});
    }

    @Test
    public void testV21_highlight_N4_internal_run() {
        // """"a"""""b"""" : fence(4), content a"""", discarded quote, content b, fence(4)
        check("\"\"\"\"a\"\"\"\"\"b\"\"\"\"",
                new String[]{"\"\"\"\"", "a\"\"\"\"", "\"", "b", "\"\"\"\""},
                new NTextStyle[]{NTextStyle.string(), NTextStyle.string(), NTextStyle.separator(), NTextStyle.string(), NTextStyle.string()});
    }

    @Test
    public void testV21_highlight_empty_string() {
        // ""  : exactly two quotes, single empty-string token
        check("\"\"",
                new String[]{"\"\""},
                new NTextStyle[]{NTextStyle.string()});
    }

    @Test
    public void testV21_highlight_simple_and_multiline() {
        check("\"hello\"",
                new String[]{"\"", "hello", "\""},
                new NTextStyle[]{NTextStyle.string(), NTextStyle.string(), NTextStyle.string()});
        check("\"\"\"line1\nline2\"\"\"",
                new String[]{"\"\"\"", "line1\nline2", "\"\"\""},
                new NTextStyle[]{NTextStyle.string(), NTextStyle.string(), NTextStyle.string()});
    }

    @Test
    public void testV21_highlight_unterminated() {
        // unterminated N=1 : fence + content, no closing fence, no crash
        check("\"abc",
                new String[]{"\"", "abc"},
                new NTextStyle[]{NTextStyle.string(), NTextStyle.string()});
        // unterminated N=4
        check("\"\"\"\"abc",
                new String[]{"\"\"\"\"", "abc"},
                new NTextStyle[]{NTextStyle.string(), NTextStyle.string()});
    }

    @Test
    public void testV21_highlight_all_quote_chars() {
        // backtick
        check("`a``b`",
                new String[]{"`", "a`", "`", "b", "`"},
                new NTextStyle[]{NTextStyle.string(), NTextStyle.string(), NTextStyle.separator(), NTextStyle.string(), NTextStyle.string()});
        // single quotes
        check("''''a''''''b''''",
                new String[]{"''''", "a'''''", "'", "b", "''''"},
                new NTextStyle[]{NTextStyle.string(), NTextStyle.string(), NTextStyle.separator(), NTextStyle.string(), NTextStyle.string()});
    }
}