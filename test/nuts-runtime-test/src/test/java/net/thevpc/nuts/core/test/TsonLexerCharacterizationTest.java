package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.standalone.format.tson.parser.NElementTokenImpl;
import net.thevpc.nuts.runtime.standalone.format.tson.parser.custom.TsonCustomLexer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * ARCH-08 characterization lock for TsonCustomLexer. Before the lexer is split,
 * this records the exact token stream (type, variant, level, image, position,
 * value, fence length and error message) for a broad corpus. The refactor must
 * not change a single byte of it.
 */
public class TsonLexerCharacterizationTest {

    private static final String[] CORPUS = {
            "",
            " \t\r\n  ",
            "{a:1,b:[2,3],c:(d)}",
            "# ## ### . .. ... :: ;; ; , : = == === < <= <== <- <-- << <<<",
            "// line comment\n  // second line\n/* block\n comment */",
            "\"quoted\" '''triple''' ```fenced``` ~~~~long fence~~~~",
            "¶single line\n¶¶multi\nline¶¶",
            "• bullet ● bullet2 ■ ordered ▪ ordered2",
            "ω=π+f*g",
            "identifier_123 identifier-two identifier.two $var",
            "0 1 -1 +2 1.5 -1E-5 1e+10 0x1F 1_000_000 2__1_._3__5___GHz",
            "123s8 456u16 789s32 1011u64 1213n 1f 2d 3L 4B",
            "1+2i 3.5-4.2i 5i",
            "2023-10-27 10:30:00 2023-10-27T10:30:00Z",
            "NaN Infinity -Infinity true false null",
            "a???b [1,,2] {unclosed: \"unterminated",
            "😀 unicode 💡 emoji",
            "@ $ % ^ & | ! ? ~ ` \\ /",
    };

    @BeforeAll
    static void initWorkspace() {
        // number token creation needs a workspace in the current context
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void tokenStreamIsStable() throws Exception {
        List<String> lines = new ArrayList<>();
        for (int i = 0; i < CORPUS.length; i++) {
            lines.add(i + ":" + fingerprint(lex(CORPUS[i])));
        }
        String actual = String.join("\n", lines) + "\n";
        Path baseline = Paths.get("src/test/resources/tson-lexer-characterization.txt")
                .toAbsolutePath().normalize();
        if (System.getProperty("nuts.lexer.generate") != null) {
            Files.createDirectories(baseline.getParent());
            Files.write(baseline, actual.getBytes(StandardCharsets.UTF_8));
            return;
        }
        Assertions.assertTrue(Files.exists(baseline), "missing characterization baseline: " + baseline);
        String expected = new String(Files.readAllBytes(baseline), StandardCharsets.UTF_8);
        Assertions.assertEquals(expected, actual, "TsonCustomLexer token stream changed");
    }

    private static String lex(String source) {
        StringBuilder sb = new StringBuilder();
        try {
            List<NElementTokenImpl> tokens = new TsonCustomLexer(new StringReader(source)).all();
            for (NElementTokenImpl t : tokens) {
                sb.append(t.type() == null ? "<null>" : t.type().id())
                        .append('|').append(quote(t.variant()))
                        .append('|').append(t.level())
                        .append('|').append(quote(t.image()))
                        .append('|').append(t.line())
                        .append('|').append(t.col())
                        .append('|').append(t.pos())
                        .append('|').append(valueString(t.value()))
                        .append('|').append(t.fenceLength())
                        .append('|').append(t.errorMessage() == null ? "<none>" : quote(String.valueOf(t.errorMessage())))
                        .append('\n');
            }
        } catch (Throwable ex) {
            sb.append("EXCEPTION ").append(ex.getClass().getName()).append(' ')
                    .append(quote(String.valueOf(ex.getMessage()))).append('\n');
        }
        return sb.toString();
    }

    private static String valueString(Object value) {
        if (value == null) {
            return "<null>";
        }
        if (value instanceof TsonCustomLexer.LinesAndContent) {
            TsonCustomLexer.LinesAndContent lc = (TsonCustomLexer.LinesAndContent) value;
            StringBuilder sb = new StringBuilder("LinesAndContent{content=").append(quote(lc.content)).append(",lines=[");
            if (lc.lines != null) {
                for (int i = 0; i < lc.lines.size(); i++) {
                    if (i > 0) {
                        sb.append(';');
                    }
                    sb.append(quote(String.valueOf(lc.lines.get(i))));
                }
            }
            return sb.append("]}").toString();
        }
        return value.getClass().getName() + ":" + quote(String.valueOf(value));
    }

    private static String fingerprint(String canonical) throws Exception {
        byte[] bytes = MessageDigest.getInstance("SHA-256")
                .digest(canonical.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xff));
        }
        return sb.toString();
    }

    private static String quote(String s) {
        if (s == null) {
            return "<null>";
        }
        return s.replace("\\", "\\\\")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t")
                .replace("|", "\\|");
    }
}
