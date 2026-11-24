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
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScalaCodeHighlighterTest {

    @BeforeAll
    static void setUp() {
        TestUtils.openNewMinTestWorkspace();
        net.thevpc.nuts.Nuts.require();
    }

    private static NCodeHighlighter highlighter() {
        return NCodeHighlighter.of("scala");
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
        assertEquals("scala", h.id());
    }

    @Test
    void testHighlighterIsReachableByMimeType() {
        assertNotNull(NCodeHighlighter.of("text/x-scala"));
        assertNotNull(NCodeHighlighter.of("application/x-scala"));
    }

    @Test
    void testRoundTrip() {
        String source = ""
                + "package demo\n"
                + "import scala.collection.mutable\n"
                + "/* outer /* nested */ still comment */\n"
                + "object Main extends App {\n"
                + "  val name: String = s\"hello ${user.name}\"\n"
                + "  val raw = \"\"\"line1\nline2\"\"\"\n"
                + "  val c = 'x'\n"
                + "  val sym = 'symbol\n"
                + "  @deprecated(\"x\", \"y\") def f[A](x: Int): Option[A] = None\n"
                + "  println(f\"$name%s\")\n"
                + "  val n = 0xFF_FFL + 1.5e-3f\n"
                + "}\n";
        NText result = highlighter().stringToText(source);
        assertNotNull(result);
        assertEquals(source, result.filteredText());
    }

    @Test
    void testStyles() {
        assertEquals(NTextStyle.keyword(), styleOf("object Main", "object"));
        assertEquals(NTextStyle.option(), styleOf("val x: String = \"\"", "String"));
        assertEquals(NTextStyle.annotation(), styleOf("@deprecated def f() = 1", "@deprecated"));
        assertEquals(NTextStyle.comments(), styleOf("/* outer /* nested */ still */ x", "/* outer /* nested */ still */"));
        assertEquals(NTextStyle.string(), styleOf("val c = 'x'", "'x'"));
        assertEquals(NTextStyle.annotation(), styleOf("val sym = 'symbol", "'symbol"));
        assertEquals(NTextStyle.string(), styleOf("val s = \"\"\"a\nb\"\"\"", "\"\"\"a\nb\"\"\""));
        assertEquals(NTextStyle.number(), styleOf("val n = 0xFF_FFL", "0xFF_FFL"));
        assertEquals(NTextStyle.variable(), styleOf("val s = s\"hi $name\"", "$name"));
        assertEquals(NTextStyle.variable(), styleOf("val s = s\"hi ${user.name}\"", "${user.name}"));
        assertEquals(NTextStyle.operator(), styleOf("val x = a :: b", "::"));
    }

    @Test
    void testEmptyAndNull() {
        assertEquals("", highlighter().stringToText("").filteredText());
        assertEquals("", highlighter().stringToText(null).filteredText());
    }

    @Test
    void testUnterminatedConstructsDoNotCrash() {
        for (String s : new String[]{"\"abc", "\"\"\"abc", "/* abc", "'a", "`abc", "s\"${x"}) {
            NText r = highlighter().stringToText(s);
            assertNotNull(r);
            assertEquals(s, r.filteredText());
        }
        assertTrue(true);
    }
}
