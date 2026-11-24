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

/**
 * Regression tests for the lexer fixes made to the pre-existing highlighters.
 */
class HighlighterFixesTest {

    @BeforeAll
    static void setUp() {
        TestUtils.openNewMinTestWorkspace();
        net.thevpc.nuts.Nuts.require();
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

    private static NTextStyle styleOf(NCodeHighlighter h, String input, String token) {
        for (Seg s : segments(h.stringToText(input))) {
            if (token.equals(s.text)) {
                return s.style;
            }
        }
        return null;
    }

    private static void assertRoundTrip(NCodeHighlighter h, String input) {
        NText r = h.stringToText(input);
        assertNotNull(r);
        assertEquals(input, r.filteredText());
    }

    @Test
    void testJavaTextBlockAndAnnotation() {
        JavaCodeHighlighter h = new JavaCodeHighlighter();
        String input = "@Override\npublic String f() {\n  return \"\"\"\n    hello\n    \"\"\";\n}\n";
        assertRoundTrip(h, input);
        assertEquals(NTextStyle.annotation(), styleOf(h, input, "@Override"));
        assertEquals(NTextStyle.string(), styleOf(h, input, "\"\"\"\n    hello\n    \"\"\""));
    }

    @Test
    void testCSharpVerbatimAndInterpolatedStrings() {
        CSharpCodeHighlighter h = new CSharpCodeHighlighter();
        assertRoundTrip(h, "var s = @\"a\"\"b\";");
        assertRoundTrip(h, "var s = $\"x {name}\";");
        assertRoundTrip(h, "var s = \"\"\"raw\n\"\"\";");
        assertEquals(NTextStyle.annotation(), styleOf(h, "var s = @\"a\"\"b\";", "@"));
        assertEquals(NTextStyle.separator(), styleOf(h, "var s = $\"x {name}\";", "{name}"));
    }

    @Test
    void testCppKeywordListAndPreprocessor() {
        CppCodeHighlighter h = new CppCodeHighlighter();
        String input = "#include <vector>\nnamespace demo { class A {}; }\n";
        assertRoundTrip(h, input);
        assertEquals(NTextStyle.annotation(), styleOf(h, input, "#include"));
        assertEquals(NTextStyle.keyword(), styleOf(h, input, "namespace"));
        assertEquals(NTextStyle.keyword(), styleOf(h, input, "class"));
    }

    @Test
    void testSqlCommentsAndTypes() {
        SqlCodeHighlighter h = new SqlCodeHighlighter();
        String input = "-- hello\nSELECT id, name FROM users WHERE id = 1;\n";
        assertRoundTrip(h, input);
        assertEquals(NTextStyle.comments(), styleOf(h, input, "-- hello\n"));
        assertEquals(NTextStyle.keyword(), styleOf(h, input, "SELECT"));
        assertEquals(NTextStyle.option(), styleOf(h, "CREATE TABLE t (id bigint)", "bigint"));
    }

    @Test
    void testXmlClosingTag() {
        XmlCodeHighlighter h = new XmlCodeHighlighter();
        String input = "<project><name>x</name></project>\n";
        assertRoundTrip(h, input);
        assertEquals(NTextStyle.keyword(), styleOf(h, input, "project"));
        // first occurrence of "name" is the opening tag
        assertEquals(NTextStyle.keyword(), styleOf(h, input, "name"));
        // the closing tag name must be highlighted too
        List<Seg> segs = segments(h.stringToText(input));
        int closingName = -1;
        for (int i = 0; i < segs.size(); i++) {
            if ("</".equals(segs.get(i).text) && i + 1 < segs.size()) {
                closingName = i + 1;
                break;
            }
        }
        assertTrue(closingName >= 0);
        assertEquals("name", segs.get(closingName).text);
        assertEquals(NTextStyle.keyword(), segs.get(closingName).style);
    }
}
