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

class NewLanguagesCodeHighlighterTest {

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

    private static void assertRoundTrip(NCodeHighlighter h, String input) {
        NText r = h.stringToText(input);
        assertNotNull(r);
        assertEquals(input, r.filteredText(), "round trip for highlighter " + h.id());
    }

    private static NTextStyle styleOf(NCodeHighlighter h, String input, String token) {
        for (Seg s : segments(h.stringToText(input))) {
            if (token.equals(s.text)) {
                return s.style;
            }
        }
        return null;
    }

    @Test
    void testAllNewHighlightersAreRegistered() {
        for (String id : new String[]{"kotlin", "go", "ruby", "php", "swift", "toml", "ini",
                "dockerfile", "makefile", "hcl", "css"}) {
            assertNotNull(NCodeHighlighter.of(id), id);
        }
        for (String mime : new String[]{"text/x-kotlin", "text/x-go", "text/x-ruby", "text/x-php",
                "text/x-swift", "text/x-toml", "text/x-ini", "text/x-dockerfile",
                "text/x-makefile", "text/x-hcl", "text/css", "text/x-scss"}) {
            assertNotNull(NCodeHighlighter.of(mime), mime);
        }
    }

    @Test
    void testKotlin() {
        KotlinCodeHighlighter h = new KotlinCodeHighlighter();
        String src = "// c\nfun main() { val x: Int = 1; val s = \"hi $name\"; val t = \"\"\"raw\"\"\" }\n";
        assertRoundTrip(h, src);
        assertEquals(NTextStyle.comments(), styleOf(h, src, "// c\n"));
        assertEquals(NTextStyle.keyword(), styleOf(h, src, "fun"));
        assertEquals(NTextStyle.option(), styleOf(h, src, "Int"));
        assertEquals(NTextStyle.variable(), styleOf(h, src, "$name"));
        assertEquals(NTextStyle.string(), styleOf(h, src, "\"\"\"raw\"\"\""));
    }

    @Test
    void testGo() {
        GoCodeHighlighter h = new GoCodeHighlighter();
        String src = "// c\npackage main\nfunc main() { x := `raw`; s := \"hi\"; r := 'x'; var y int }\n";
        assertRoundTrip(h, src);
        assertEquals(NTextStyle.comments(), styleOf(h, src, "// c\n"));
        assertEquals(NTextStyle.keyword(), styleOf(h, src, "func"));
        assertEquals(NTextStyle.option(), styleOf(h, src, "int"));
        assertEquals(NTextStyle.string(), styleOf(h, src, "`raw`"));
        assertEquals(NTextStyle.string(), styleOf(h, src, "'x'"));
    }

    @Test
    void testRuby() {
        RubyCodeHighlighter h = new RubyCodeHighlighter();
        String src = "# c\nclass Foo\n  def bar(x)\n    @y = :sym\n    puts \"#{x}\"\n  end\nend\n";
        assertRoundTrip(h, src);
        assertEquals(NTextStyle.comments(), styleOf(h, src, "# c\n"));
        assertEquals(NTextStyle.keyword(), styleOf(h, src, "class"));
        assertEquals(NTextStyle.annotation(), styleOf(h, src, ":sym"));
        assertEquals(NTextStyle.variable(), styleOf(h, src, "@y"));
        assertEquals(NTextStyle.variable(), styleOf(h, src, "#{x}"));
    }

    @Test
    void testPhp() {
        PhpCodeHighlighter h = new PhpCodeHighlighter();
        String src = "<?php\n// c\nfunction f($x) { return \"hi $x\"; }\n";
        assertRoundTrip(h, src);
        assertEquals(NTextStyle.annotation(), styleOf(h, src, "<?php"));
        assertEquals(NTextStyle.comments(), styleOf(h, src, "// c\n"));
        assertEquals(NTextStyle.keyword(), styleOf(h, src, "function"));
        assertEquals(NTextStyle.variable(), styleOf(h, src, "$x"));
    }

    @Test
    void testSwift() {
        SwiftCodeHighlighter h = new SwiftCodeHighlighter();
        String src = "// c\nstruct A { let x: Int = 1; let s = \"hi \\(name)\" }\n";
        assertRoundTrip(h, src);
        assertEquals(NTextStyle.comments(), styleOf(h, src, "// c\n"));
        assertEquals(NTextStyle.keyword(), styleOf(h, src, "struct"));
        assertEquals(NTextStyle.option(), styleOf(h, src, "Int"));
        assertEquals(NTextStyle.variable(), styleOf(h, src, "\\(name)"));
    }

    @Test
    void testToml() {
        TomlCodeHighlighter h = new TomlCodeHighlighter();
        String src = "# c\n[table]\nkey = \"value\"\nflag = true\nwhen = 1979-05-27\n";
        assertRoundTrip(h, src);
        assertEquals(NTextStyle.comments(), styleOf(h, src, "# c\n"));
        assertEquals(NTextStyle.annotation(), styleOf(h, src, "[table]"));
        assertEquals(NTextStyle.string(), styleOf(h, src, "\"value\""));
        assertEquals(NTextStyle.bool(), styleOf(h, src, "true"));
        assertEquals(NTextStyle.number(), styleOf(h, src, "1979-05-27"));
    }

    @Test
    void testIni() {
        IniCodeHighlighter h = new IniCodeHighlighter();
        String src = "; c\n[section]\nkey = \"value\"\nflag=true\n";
        assertRoundTrip(h, src);
        assertEquals(NTextStyle.comments(), styleOf(h, src, "; c"));
        assertEquals(NTextStyle.annotation(), styleOf(h, src, "[section]"));
        assertEquals(NTextStyle.option(), styleOf(h, src, "key"));
        assertEquals(NTextStyle.string(), styleOf(h, src, "\"value\""));
        assertEquals(NTextStyle.bool(), styleOf(h, src, "true"));
    }

    @Test
    void testDockerfile() {
        DockerfileCodeHighlighter h = new DockerfileCodeHighlighter();
        String src = "# c\nFROM ubuntu:22.04\nRUN echo \"hi $NAME\"\n";
        assertRoundTrip(h, src);
        assertEquals(NTextStyle.comments(), styleOf(h, src, "# c"));
        assertEquals(NTextStyle.keyword(), styleOf(h, src, "FROM"));
        assertEquals(NTextStyle.keyword(), styleOf(h, src, "RUN"));
        assertEquals(NTextStyle.variable(), styleOf(h, src, "$NAME"));
    }

    @Test
    void testMakefile() {
        MakefileCodeHighlighter h = new MakefileCodeHighlighter();
        String src = "# c\nNAME = app\nall: build\n\t@echo \"hi $(NAME)\"\n";
        assertRoundTrip(h, src);
        assertEquals(NTextStyle.comments(), styleOf(h, src, "# c"));
        assertEquals(NTextStyle.option(), styleOf(h, src, "NAME"));
        assertEquals(NTextStyle.keyword(), styleOf(h, src, "all"));
        assertEquals(NTextStyle.variable(), styleOf(h, src, "$(NAME)"));
    }

    @Test
    void testCss() {
        CssCodeHighlighter h = new CssCodeHighlighter();
        String src = "/* c */\n@media screen { .a { color: #ff0000; margin: 10px; } }\n";
        assertRoundTrip(h, src);
        assertEquals(NTextStyle.comments(), styleOf(h, src, "/* c */"));
        assertEquals(NTextStyle.annotation(), styleOf(h, src, "@media"));
        assertEquals(NTextStyle.option(), styleOf(h, src, "color"));
        assertEquals(NTextStyle.option(), styleOf(h, src, "margin"));
    }

    @Test
    void testHcl() {
        HclCodeHighlighter h = new HclCodeHighlighter();
        String src = "# c\nresource \"x\" \"y\" {\n  name = \"hi ${var.name}\"\n  enabled = true\n}\n";
        assertRoundTrip(h, src);
        assertEquals(NTextStyle.comments(), styleOf(h, src, "# c\n"));
        assertEquals(NTextStyle.keyword(), styleOf(h, src, "resource"));
        assertEquals(NTextStyle.variable(), styleOf(h, src, "${var.name}"));
        assertEquals(NTextStyle.bool(), styleOf(h, src, "true"));
    }
}
