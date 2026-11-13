package net.thevpc.nuts.core.test;

import net.thevpc.nuts.Nuts;
import net.thevpc.nuts.collections.NMaps;
import net.thevpc.nuts.text.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Checks that the printf-style ({@code NMsg.ofC}), MessageFormat-style ({@code NMsg.ofJ}),
 * dollar ({@code ofV}), sql ({@code ofS}) and moustache ({@code ofM}) message formats are
 * supported consistently by the two rendering paths available when a workspace exists:
 * <ul>
 *     <li>{@link NMsg#toString()} / {@link NMsg#toString(boolean)} - rendered with
 *     {@code java.util.Formatter} / {@code java.text.MessageFormat} directly from nuts-api,
 *     and therefore also available without a workspace;</li>
 *     <li>{@code NText.of(msg).filteredText()} - rendered by the runtime
 *     ({@code NMsgCFormatHelper} and friends), which is the path used for styled terminal
 *     output.</li>
 * </ul>
 * Every case below asserts that both paths render the message identically.
 */
public class MsgFormatSupportTest {

    @BeforeAll
    static void beforeAll() {
        Nuts.require("--color");
    }

    /**
     * The message rendered by nuts-api (java.util.Formatter / MessageFormat).
     */
    private static String byApi(NMsg m) {
        return m.toString();
    }

    /**
     * The message rendered by the runtime text model, as used for styled output.
     */
    private static String byText(NMsg m) {
        return NText.of(m).filteredText();
    }

    private static void assertBoth(String expected, NMsg m) {
        Assertions.assertEquals(expected, byApi(m), "toString()");
        Assertions.assertEquals(expected, m.toString(true), "toString(true)");
        Assertions.assertEquals(expected, byText(m), "NText.of(msg).filteredText()");
    }

    // =========================================================================
    // printf style (%s, %d, and more complex specifiers)
    // =========================================================================

    @Test
    public void testCFormatString() {
        assertBoth("a=x", NMsg.ofC("a=%s", "x"));
    }

    @Test
    public void testCFormatInt() {
        assertBoth("a=42", NMsg.ofC("a=%d", 42));
    }

    @Test
    public void testCFormatLong() {
        assertBoth("a=9000000000", NMsg.ofC("a=%d", 9000000000L));
    }

    @Test
    public void testCFormatZeroPadded() {
        assertBoth("00042", NMsg.ofC("%05d", 42));
    }

    @Test
    public void testCFormatLeftJustified() {
        assertBoth("[a    ]", NMsg.ofC("[%-5s]", "a"));
    }

    @Test
    public void testCFormatRightJustified() {
        assertBoth("[    a]", NMsg.ofC("[%5s]", "a"));
    }

    @Test
    public void testCFormatFloatPrecision() {
        assertBoth("3.14", NMsg.ofC("%.2f", 3.14159));
    }

    @Test
    public void testCFormatHexAndOctal() {
        assertBoth("ff-FF-10", NMsg.ofC("%x-%X-%o", 255, 255, 8));
    }

    @Test
    public void testCFormatBoolean() {
        assertBoth("true-false", NMsg.ofC("%b-%b", true, false));
    }

    @Test
    public void testCFormatPercentEscape() {
        assertBoth("100%", NMsg.ofC("100%%"));
    }

    @Test
    public void testCFormatNewLineEscape() {
        // %n is the platform line separator in both paths, like java.util.Formatter
        assertBoth("a" + System.lineSeparator() + "b", NMsg.ofC("a%nb"));
    }

    @Test
    public void testCFormatNoSpecifier() {
        assertBoth("plain text", NMsg.ofC("plain text"));
    }

    @Test
    public void testCFormatExtraUnusedArgs() {
        // printf ignores arguments that the template never references
        assertBoth("plain", NMsg.ofC("plain", "unused", 12));
    }

    @Test
    public void testCFormatPercentInArgument() {
        assertBoth("Discount 100%", NMsg.ofC("Discount %s", "100%"));
    }

    @Test
    public void testCFormatNestedMsg() {
        assertBoth("[inner]", NMsg.ofC("[%s]", NMsg.ofP("inner")));
    }

    @Test
    public void testCFormatSeveralSpecifiers() {
        assertBoth("install a with b (3)", NMsg.ofC("install %s with %s (%d)", "a", "b", 3));
    }

    // =========================================================================
    // graceful degradation of a broken printf template (api path)
    // =========================================================================

    @Test
    public void testCFormatBadConversionReportsError() {
        // %d with a String is a caller bug: the api path must not throw, it reports it
        String s = NMsg.ofC("a=%d", "notanumber").toString();
        Assertions.assertTrue(s.startsWith("[ERROR] Invalid CFORMAT message"), s);
        Assertions.assertTrue(s.contains("d != java.lang.String"), s);
    }

    @Test
    public void testCFormatMissingArgumentReportsError() {
        String s = NMsg.ofC("a=%s b=%s", "only").toString();
        Assertions.assertTrue(s.startsWith("[ERROR] Invalid CFORMAT message"), s);
        Assertions.assertTrue(s.contains("%s"), s);
    }

    @Test
    public void testCFormatTrailingPercentReportsError() {
        String s = NMsg.ofC("a %").toString();
        Assertions.assertTrue(s.startsWith("[ERROR] Invalid CFORMAT message"), s);
    }

    // =========================================================================
    // MessageFormat style ({} and {0})
    // =========================================================================

    @Test
    public void testJFormatIndexed() {
        assertBoth("a-b", NMsg.ofJ("{0}-{1}", "a", "b"));
    }

    @Test
    public void testJFormatAnonymous() {
        assertBoth("a-b", NMsg.ofJ("{}-{}", "a", "b"));
    }

    // =========================================================================
    // dollar / sql / moustache / literal formats
    // =========================================================================

    @Test
    public void testVFormatMap() {
        assertBoth("x=A", NMsg.ofV("x=${a}", NMaps.of("a", "A")));
    }

    @Test
    public void testVFormatFunction() {
        assertBoth("x=A", NMsg.ofV("x=$a", s -> "a".equals(s) ? "A" : null));
    }

    @Test
    public void testVFormatSeveralVars() {
        assertBoth("A/B", NMsg.ofV("${a}/${b}", NMaps.of("a", "A", "b", "B")));
    }

    @Test
    public void testSFormat() {
        assertBoth("select * from t where a=7 and b=q", NMsg.ofS("select * from t where a=? and b=?", 7, "q"));
    }

    @Test
    public void testMFormat() {
        assertBoth("x=A", NMsg.ofM("x={{a}}", NMaps.of("a", "A")));
    }

    @Test
    public void testPlainKeepsSpecifiers() {
        assertBoth("plain %s kept", NMsg.ofP("plain %s kept"));
    }

    @Test
    public void testNtfKeepsSpecifiers() {
        assertBoth("ntf %s kept", NMsg.ofNtf("ntf %s kept"));
    }

    @Test
    public void testCode() {
        assertBoth("int x;", NMsg.ofCode("java", "int x;"));
    }

    @Test
    public void testStyled() {
        assertBoth("bold", NMsg.ofStyled("bold", NTextStyle.bold()));
    }

    @Test
    public void testRichParameterKeepsMarkupOnlyInMarkupMode() {
        NMsg m = NMsg.ofC("[%s]", NText.ofStyled("sty", NTextStyle.error()));
        // plain rendering strips the style, markup rendering keeps it
        Assertions.assertEquals("[sty]", m.toString(true));
        Assertions.assertEquals("[sty]", NText.of(m).filteredText());
        Assertions.assertTrue(NText.of(m).toString().contains("error"), NText.of(m).toString());
    }

    @Test
    public void testThrowableParameter() {
        NMsg m = NMsg.ofC("failed %s", new RuntimeException("boom"));
        Assertions.assertTrue(byApi(m).contains("boom"), byApi(m));
        Assertions.assertTrue(byText(m).contains("boom"), byText(m));
    }

    // =========================================================================
    // Known divergences between the api path and the runtime (NText) path.
    // These are bugs in the runtime helpers, not in the api path; the assertions
    // encode the behaviour the api path already has, so enabling a test is the
    // definition of done for the corresponding fix.
    // =========================================================================

    @Test
    public void testCFormatExplicitArgumentIndex() {
        assertBoth("z-z", NMsg.ofC("%1$s-%1$s", "z"));
    }

    @Test
    public void testCFormatCharConversion() {
        assertBoth("q", NMsg.ofC("%c", 'q'));
    }

    @Test
    public void testCFormatNullArgument() {
        assertBoth("[null]", NMsg.ofC("[%s]", (Object) null));
    }

    @Test
    public void testJFormatNumberPattern() {
        assertBoth("00042", NMsg.ofJ("{0,number,00000}", 42));
    }

    @Test
    public void testJFormatQuotedLiteral() {
        assertBoth("{0}", NMsg.ofJ("'{0}'", "a"));
    }

    @Test
    public void testCFormatBadConversionIsReportedNotThrown() {
        String s = NText.of(NMsg.ofC("a=%d", "notanumber")).filteredText();
        Assertions.assertTrue(s.startsWith("[ERROR] Invalid CFORMAT message"), s);
    }

    @Test
    public void testVFormatUnknownVariableStaysLiteral() {
        assertBoth("x=${zz}", NMsg.ofV("x=${zz}", NMaps.of("a", "A")));
    }
}
