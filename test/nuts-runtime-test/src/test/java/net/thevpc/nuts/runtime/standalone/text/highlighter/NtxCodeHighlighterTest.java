package net.thevpc.nuts.runtime.standalone.text.highlighter;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.spi.NCodeHighlighter;
import net.thevpc.nuts.text.NText;
import net.thevpc.nuts.text.NTextList;
import net.thevpc.nuts.text.NTextPlain;
import net.thevpc.nuts.text.NTextStyle;
import net.thevpc.nuts.text.NTextStyled;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class NtxCodeHighlighterTest {

    @BeforeAll
    static void setUp() {
        TestUtils.openNewMinTestWorkspace();
        net.thevpc.nuts.Nuts.require();
    }

    private static NCodeHighlighter highlighter() {
        return NCodeHighlighter.of("ntx");
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

    private static NTextStyle styleOf(String input, String token) {
        for (Seg s : segments(highlighter().stringToText(input))) {
            if (token.equals(s.text)) {
                return s.style;
            }
        }
        return null;
    }

    @Test
    void testHighlighterIsRegistered() {
        NCodeHighlighter h = highlighter();
        assertNotNull(h);
        assertEquals("ntx", h.id());
    }

    @Test
    void testHighlighterIsReachableByMimeType() {
        assertNotNull(NCodeHighlighter.of("text/ntx"));
        assertNotNull(NCodeHighlighter.of("application/ntx"));
    }

    @Test
    void testLegacyNtexupIdStillWorks() {
        NCodeHighlighter h = NCodeHighlighter.of("ntexup");
        assertNotNull(h);
        assertEquals("ntexup", h.id());
    }

    @Test
    void testRoundTrip() {
        String source = ""
                + "// a slide\n"
                + "documentTitle = \"Demo\"\n"
                + "@define badge(label, accent: blue){\n"
                + "    group{ rectangle(background: accent, round-corner: 4) text(\"${label}\", at: center) }\n"
                + "}\n"
                + "content-slide(title: \"Intro\"){\n"
                + "    body{\n"
                + "        ¶ plain one-line text\n"
                + "        ul{ \"one\" \"two\" }\n"
                + "        source(scala, at: center\n"
                + "        ```\n"
                + "        object Main extends App { println(s\"hi $name\") }\n"
                + "        ```)\n"
                + "    }\n"
                + "}\n";
        NText result = highlighter().stringToText(source);
        assertNotNull(result);
        assertEquals(source, result.filteredText());
    }

    @Test
    void testStyles() {
        assertEquals(NTextStyle.primary5(), styleOf("content-slide(title: \"x\")", "content-slide"));
        assertEquals(NTextStyle.primary1(), styleOf("rectangle(x: 1)", "rectangle"));
        assertEquals(NTextStyle.primary6(), styleOf("my-custom-node(x: 1)", "my-custom-node"));
        assertEquals(NTextStyle.primary3(), styleOf("content-slide(title: \"x\")", "title"));
        assertEquals(NTextStyle.primary4(), styleOf("content-slide(title: \"x\", at: center)", "center"));
        assertEquals(NTextStyle.annotation(), styleOf("@define f(){}", "@define"));
        assertEquals(NTextStyle.comments(), styleOf("// hello\nx = 1", "// hello\n"));
        assertEquals(NTextStyle.string(), styleOf("¶ plain one-line text", " plain one-line text"));
        assertEquals(NTextStyle.bool(), styleOf("x = true", "true"));
        assertEquals(NTextStyle.keyword(), styleOf("x = null", "null"));
    }

    @Test
    void testEmptyAndNull() {
        assertEquals("", highlighter().stringToText("").filteredText());
        assertEquals("", highlighter().stringToText(null).filteredText());
    }

    @Test
    void testUnterminatedConstructsDoNotCrash() {
        for (String s : new String[]{"\"abc", "```abc", "/* abc", "content-slide(title: \"x\"){"}) {
            NText r = highlighter().stringToText(s);
            assertNotNull(r);
            assertEquals(s, r.filteredText());
        }
    }
}
