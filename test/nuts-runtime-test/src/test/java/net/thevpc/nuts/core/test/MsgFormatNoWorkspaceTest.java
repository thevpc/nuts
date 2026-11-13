package net.thevpc.nuts.core.test;

import net.thevpc.nuts.boot.core.NAnyExceptionBase;
import net.thevpc.nuts.text.NMsg;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Message rendering must work without a workspace: {@link NMsg#toString()} is used by
 * logging and error paths that run before (or instead of) a workspace being created,
 * and by applications that only have nuts-api/nuts-boot on their class path.
 * <p>
 * A workspace cannot be "un-created" inside the running test JVM, so this test forks a
 * child JVM whose class path contains only nuts-api and nuts-boot (plus the compiled
 * test classes for the probe itself) and asserts what that child renders.
 */
public class MsgFormatNoWorkspaceTest {

    @Test
    public void testRenderingWithoutWorkspace() throws Exception {
        Map<String, String> out = runProbeInChildJvm();

        // --- printf formatting works "as java does" without a workspace ---------
        assertValue(out, "c.string", "a=x");
        assertValue(out, "c.int", "a=42");
        assertValue(out, "c.long", "a=9000000000");
        assertValue(out, "c.zeroPadded", "00042");
        assertValue(out, "c.leftJustified", "[a    ]");
        assertValue(out, "c.rightJustified", "[    a]");
        assertValue(out, "c.floatPrecision", "3.14");
        assertValue(out, "c.indexed", "z-z");
        assertValue(out, "c.percent", "100%");
        assertValue(out, "c.hex", "ff-FF-10");
        assertValue(out, "c.boolean", "true-false");
        assertValue(out, "c.char", "q");
        assertValue(out, "c.newline", "a\\nb");
        assertValue(out, "c.nullArg", "[null]");
        assertValue(out, "c.nestedMsg", "[inner]");
        assertValue(out, "c.noSpecifier", "plain text");

        // --- broken templates degrade instead of throwing ----------------------
        Assertions.assertTrue(out.get("c.badConversion").startsWith("[ERROR] Invalid CFORMAT message"),
                out.get("c.badConversion"));
        Assertions.assertTrue(out.get("c.missingArg").startsWith("[ERROR] Invalid CFORMAT message"),
                out.get("c.missingArg"));
        Assertions.assertTrue(out.get("c.trailingPercent").startsWith("[ERROR] Invalid CFORMAT message"),
                out.get("c.trailingPercent"));

        // --- MessageFormat, dollar, plain, code, styled ------------------------
        assertValue(out, "j.indexed", "a-b");
        assertValue(out, "j.anonymous", "a-b");
        assertValue(out, "j.numberPattern", "00042");
        assertValue(out, "v.map", "x=A");
        assertValue(out, "v.function", "x=A");
        assertValue(out, "v.unknownStaysLiteral", "x=${zz}");
        assertValue(out, "plain", "plain %s kept");
        assertValue(out, "code", "int x;");
        assertValue(out, "styled", "bold");

        // --- formats that need a runtime formatter degrade to the raw template
        //     rather than failing; documented as current behaviour, not as desired
        assertValue(out, "s.needsWorkspace", "select * from t where a=?");
        assertValue(out, "m.needsWorkspace", "{{a}}");
        assertValue(out, "custom.needsWorkspace", "hello %s");

        // --- the styled text model itself does require a workspace -------------
        Assertions.assertEquals("missing-workspace", out.get("text.requiresWorkspace"),
                out.get("text.requiresWorkspace"));
    }

    private static void assertValue(Map<String, String> out, String key, String expected) {
        Assertions.assertEquals(expected, out.get(key), key);
    }

    private static Map<String, String> runProbeInChildJvm() throws Exception {
        String javaBin = System.getProperty("java.home") + File.separator + "bin" + File.separator + "java";
        String classPath = String.join(File.pathSeparator,
                codeSourceOf(NMsg.class),          // nuts-api
                codeSourceOf(NAnyExceptionBase.class), // nuts-boot
                codeSourceOf(MsgFormatNoWorkspaceTest.class) // compiled test classes
        );
        ProcessBuilder pb = new ProcessBuilder(javaBin, "-cp", classPath, Probe.class.getName());
        pb.redirectErrorStream(true);
        Process p = pb.start();
        Map<String, String> values = new LinkedHashMap<>();
        List<String> raw = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream(), StandardCharsets.UTF_8))) {
            String line;
            while ((line = r.readLine()) != null) {
                raw.add(line);
                int eq = line.indexOf('=');
                if (eq > 0) {
                    values.put(line.substring(0, eq), line.substring(eq + 1));
                }
            }
        }
        int exit = p.waitFor();
        Assertions.assertEquals(0, exit, "probe JVM failed: " + raw);
        return values;
    }

    private static String codeSourceOf(Class<?> type) throws URISyntaxException {
        return new File(type.getProtectionDomain().getCodeSource().getLocation().toURI()).getAbsolutePath();
    }

    /**
     * Runs in the child JVM. Must only reference types present in nuts-api/nuts-boot,
     * and must never create a workspace.
     */
    public static class Probe {
        public static void main(String[] args) {
            print("c.string", NMsg.ofC("a=%s", "x").toString());
            print("c.int", NMsg.ofC("a=%d", 42).toString());
            print("c.long", NMsg.ofC("a=%d", 9000000000L).toString());
            print("c.zeroPadded", NMsg.ofC("%05d", 42).toString());
            print("c.leftJustified", NMsg.ofC("[%-5s]", "a").toString());
            print("c.rightJustified", NMsg.ofC("[%5s]", "a").toString());
            print("c.floatPrecision", NMsg.ofC("%.2f", 3.14159).toString());
            print("c.indexed", NMsg.ofC("%1$s-%1$s", "z").toString());
            print("c.percent", NMsg.ofC("100%%").toString());
            print("c.hex", NMsg.ofC("%x-%X-%o", 255, 255, 8).toString());
            print("c.boolean", NMsg.ofC("%b-%b", true, false).toString());
            print("c.char", NMsg.ofC("%c", 'q').toString());
            print("c.newline", NMsg.ofC("a%nb").toString());
            print("c.nullArg", NMsg.ofC("[%s]", (Object) null).toString());
            print("c.nestedMsg", NMsg.ofC("[%s]", NMsg.ofP("inner")).toString());
            print("c.noSpecifier", NMsg.ofC("plain text").toString());
            print("c.badConversion", NMsg.ofC("a=%d", "notanumber").toString());
            print("c.missingArg", NMsg.ofC("a=%s b=%s", "only").toString());
            print("c.trailingPercent", NMsg.ofC("a %").toString());
            print("j.indexed", NMsg.ofJ("{0}-{1}", "a", "b").toString());
            print("j.anonymous", NMsg.ofJ("{}-{}", "a", "b").toString());
            print("j.numberPattern", NMsg.ofJ("{0,number,00000}", 42).toString());
            print("v.map", NMsg.ofV("x=${a}", java.util.Collections.singletonMap("a", (Object) "A")).toString());
            print("v.function", NMsg.ofV("x=$a", s -> "a".equals(s) ? "A" : null).toString());
            print("v.unknownStaysLiteral", NMsg.ofV("x=${zz}", java.util.Collections.singletonMap("a", (Object) "A")).toString());
            print("plain", NMsg.ofP("plain %s kept").toString());
            print("code", NMsg.ofCode("java", "int x;").toString());
            print("styled", NMsg.ofStyled("bold", net.thevpc.nuts.text.NTextStyle.bold()).toString());
            print("s.needsWorkspace", NMsg.ofS("select * from t where a=?", 7).toString());
            print("m.needsWorkspace", NMsg.ofM("{{a}}", java.util.Collections.singletonMap("a", (Object) "A")).toString());
            print("custom.needsWorkspace", NMsg.ofCustom("myfmt", "hello %s", "w").toString());
            String textError;
            try {
                net.thevpc.nuts.text.NText.of(NMsg.ofC("a=%s", "x")).filteredText();
                textError = "unexpectedly-worked";
            } catch (Throwable t) {
                textError = t.getMessage() != null && t.getMessage().contains("missing workspace")
                        ? "missing-workspace" : t.getClass().getName() + ":" + t.getMessage();
            }
            print("text.requiresWorkspace", textError);
        }

        private static void print(String key, String value) {
            String v = value == null ? "null" : value;
            System.out.println(key + "=" + v.replace("\\", "\\\\").replace("\r", "").replace("\n", "\\n").replace("\u001E", "<RS>"));
        }
    }
}
