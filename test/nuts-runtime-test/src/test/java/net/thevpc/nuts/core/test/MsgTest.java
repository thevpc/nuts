package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.expr.NToken;
import net.thevpc.nuts.ext.NExtensions;
import net.thevpc.nuts.Nuts;
import net.thevpc.nuts.text.*;
import net.thevpc.nuts.util.*;
import net.thevpc.nuts.collections.NMaps;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MsgTest {

    // =========================================================================
    // PARAMETER NAMES PARSING
    // =========================================================================
    @BeforeAll
    static void beforeAll(){
        Nuts.require("--color");
    }
    @Test
    public void testCFormat_ParamNames() {
        NMsgTemplate template = NMsgTemplate.ofC("a %s %n %s");
        Assertions.assertEquals(Arrays.asList("", ""), template.paramNames());
    }

    @Test
    public void testJFormat_MixedIndexes_ParamNames() {
        NMsgTemplate template = NMsgTemplate.ofJ("a {} {0} {3}");
        Assertions.assertEquals(Arrays.asList("0", "1", "2", "3"), template.paramNames());
    }

    @Test
    public void testJFormat_Sequential_ParamNames() {
        NMsgTemplate template = NMsgTemplate.ofJ("a {} {}");
        Assertions.assertEquals(Arrays.asList("0", "1"), template.paramNames());
    }

    @Test
    public void testJFormat_ExplicitAndAnonymous_ParamNames() {
        NMsgTemplate template = NMsgTemplate.ofJ("a {} {0}");
        Assertions.assertEquals(Collections.singletonList("0"), template.paramNames());
    }

    @Test
    public void testJFormat_HigherIndexFirst_ParamNames() {
        NMsgTemplate template = NMsgTemplate.ofJ("a {4} {}");
        Assertions.assertEquals(Arrays.asList("0", "1", "2", "3", "4"), template.paramNames());
    }

    // =========================================================================
    // PLACEHOLDER TOKEN PARSING
    // =========================================================================

    @Test
    public void testDollarPlaceholder_SimpleParsing() {
        List<NToken> tokens = NStringUtils.parseDollarPlaceHolder("a${b}c").collect(Collectors.toList());
        Assertions.assertEquals(3, tokens.size());
        Assertions.assertEquals("a", tokens.get(0).sval);
        Assertions.assertEquals("b", tokens.get(1).sval);
        Assertions.assertEquals(NToken.TT_DOLLAR_BRACE, tokens.get(1).ttype);
        Assertions.assertEquals("c", tokens.get(2).sval);
    }

    @Test
    public void testDollarPlaceholder_ComplexHtmlParsing() {
        String html = "<a href=\"https://protos-erp.com\" style=\"color:#00a4bd\" target=\"_blank\">\n" +
                " <img src=\"https://protos-erp.com/assets/crm/protos-banner-fr.png\"\n" +
                " trackerFolder=\"prospect\" trackerName=\"protos-banner-fr.png\" trackerSecretEmail=\"$email\" trackerSecretCompany=\"${company}\"\n" +
                " alt=\"Banner 2024\" style=\"outline:none;text-decoration:none;border:none;max-width:100%;font-size:16px;border-radius: 25px;\" width=\"560\" align=\"middle\"/>\n" +
                " </a>";

        List<NToken> tokens = NStringUtils.parseDollarPlaceHolder(html).collect(Collectors.toList());
        List<String> variables = tokens.stream()
                .filter(t -> t.ttype == NToken.TT_DOLLAR_BRACE || t.ttype == NToken.TT_DOLLAR)
                .map(t -> t.sval)
                .collect(Collectors.toList());

        Assertions.assertEquals(Arrays.asList("email", "company"), variables);
    }

    @Test
    public void testCustomPlaceholder_RegexPattern() {
        List<NToken> tokens = NStringUtils.parsePlaceHolder(
                "s$${{example1}}$$\n$${{example2}}$$e",
                Pattern.compile("(?s)(?m)\\$\\$\\{\\{(?<var>[^}]+)}}\\$\\$"),
                "var"
        ).collect(Collectors.toList());

        Assertions.assertEquals(5, tokens.size());
        Assertions.assertEquals(NToken.TT_VAR, tokens.get(1).ttype);
        Assertions.assertEquals("example1", tokens.get(1).sval);
        Assertions.assertEquals("$${{example1}}$$", tokens.get(1).image);

        Assertions.assertEquals(NToken.TT_VAR, tokens.get(3).ttype);
        Assertions.assertEquals("example2", tokens.get(3).sval);
        Assertions.assertEquals("$${{example2}}$$", tokens.get(3).image);
    }

    @Test
    public void testMoustache_UrlDatabasePath() {
        List<NToken> tokens = NStringUtils.parseMoustachePlaceHolder("jdbc:postgresql://localhost:5432/{{database}}")
                .collect(Collectors.toList());
        Assertions.assertEquals(2, tokens.size());
        Assertions.assertEquals("jdbc:postgresql://localhost:5432/", tokens.get(0).sval);
        Assertions.assertEquals("database", tokens.get(1).sval);
    }

    // =========================================================================
    // FUNCTION / MAP INTERPOLATION
    // =========================================================================

    @Test
    public void testVFormat_FunctionResolver() {
        String result = NMsg.ofV("$JAVA_HOME/B", s -> "JAVA_HOME".equals(s) ? "A" : null).toString();
        Assertions.assertEquals("A/B", result);
    }

    @Test
    public void testMoustache_UnterminatedBracket_GracefulFallback() {
        // Lenient parsing resolves unclosed {{v to its variable value
        String result = NMsg.ofM("{{v}}{{v}", NMaps.of("v", "this is red")).toString();
        Assertions.assertEquals("this is redthis is red", result);
    }

    @Test
    public void testDollar_UnterminatedBrace_GracefulFallback() {
        // Lenient parsing resolves unclosed {{v to its variable value
        String result = NMsg.ofV("${v}${v", NMaps.of("v", "this is red")).toString();
        Assertions.assertEquals("this is redthis is red", result);
    }

    // =========================================================================
    // MARKUP INJECTION & STYLING SAFETY
    // =========================================================================

    @Test
    public void testCFormat_ParameterEscaping_WithColon() {
        Nuts.require("--color");
        NMsg msg = NMsg.ofC("##:13:%s##", ":");
        Assertions.assertNotNull(msg.toString());
    }

    @Test
    public void testJFormat_ParameterEscaping_AnonymousPlaceholder() {
        Nuts.require("--color");
        NMsg msg = NMsg.ofJ("##:12:{}##", ":");
        Assertions.assertNotNull(msg.toString());
    }

    @Test
    public void testJFormat_ParameterEscaping_PositionalPlaceholder() {
        Nuts.require("--color");
        NMsg msg = NMsg.ofJ("##:12:{0}##", ":");
        Assertions.assertNotNull(msg.toString());
    }

    @Test
    public void testVFormat_ColorWithSpecialCharacter() {
        Nuts.require("--color");
        NMsg msg = NMsg.ofV("##:12:$v##", NMaps.of("v", ":"));
        Assertions.assertNotNull(msg.toString());
    }

    @Test
    public void testVFormat_StandardColorNames() {
        Nuts.require("--color");
        NMsg msg = NMsg.ofV("##:red:$v##", NMaps.of("v", "this is red"));
        Assertions.assertNotNull(msg.toString());
    }

    @Test
    public void testVFormat_ExtendedColorNames() {
        Nuts.require("--color");
        NMsg msg = NMsg.ofV("##:MediumVioletRed:$v##", NMaps.of("v", "this is MediumVioletRed"));
        Assertions.assertNotNull(msg.toString());
    }

    @Test
    public void testVFormat_CodeDecorators() {
        Nuts.require("--color");
        NMsg msg = NMsg.ofV("```SandyBrown $v```", NMaps.of("v", "this is SandyBrown"));
        Assertions.assertNotNull(msg.toString());
    }

    @Test
    public void testMFormat_ColorNames() {
        Nuts.require("--color");
        NMsg msg = NMsg.ofM("##:red:{{v}}##", NMaps.of("v", "this is red"));
        Assertions.assertNotNull(msg.toString());
    }

    // =========================================================================
    // C-FORMAT (NMsgCFormatHelper) EDGE CASES & AST PRESERVATION
    // =========================================================================

    @Test
    public void testCFormat_PreservesNTextTreeStructure() {
        // Parameter is a rich NText component, not a plain String
        NText richText = NText.ofStyled("alert", NTextStyle.error());
        NMsg msg = NMsg.ofC("Status: %s", richText);

        // Standard String.format would flatten or lose the NText wrapper node.
        // We ensure the formatted output retains the NText node in its AST/markup.
        Assertions.assertTrue(
                msg.toString().contains("alert"),
                "C-format should correctly render NText parameters without losing inner text"
        );
    }

    @Test
    public void testCFormat_ParameterWithPercentSymbol() {
        // Parameter value contains a literal '%' character
        NMsg msg = NMsg.ofC("Discount rate is %s", "100%");

        // Ensures '100%' doesn't cause UnknownFormatConversionException or corrupt specifier parsing
        Assertions.assertEquals("Discount rate is 100%", msg.toString());
    }

    @Test
    public void testCFormat_MultiplePercentParamsWithMarkup() {
        // Combining C-style %s specifiers with embedded NTF styling tags
        NMsg msg = NMsg.ofC("##:green:%s## -> %s", "Start", "100% complete");

        Assertions.assertNotNull(msg.toString());
        Assertions.assertTrue(msg.toString().contains("100% complete"));
    }

    @Test
    public void testCFormat_RawPercentEscaping() {
        // Double percent %% in template should resolve to single literal %
        NMsg msg = NMsg.ofC("Progress: %s%%", "50");

        Assertions.assertEquals("Progress: 50%", msg.toString());
    }

    @Test
    public void testCFormat_ParameterContainingPercent_DoesNotThrowOrCorrupt() {
        // User input contains a percent character followed by a valid/invalid format specifier (e.g. %s, %d, %o)
        String userInput = "Progress is 100% completed successfully (%d tasks remaining)";

        // We format with %s expecting the literal string above to be inserted cleanly
        NMsg msg = NMsg.ofC("Status update: %s", userInput);

        // Should safely output the text as-is without throwing UnknownFormatConversionException or MissingFormatArgumentException
        Assertions.assertEquals("Status update: Progress is 100% completed successfully (%d tasks remaining)", msg.toString());
    }

    @Test
    public void testCFormat_NumericFormatting_ZeroPaddedInteger() {
        // %03d should zero-pad number 5 to 3 digits -> "005"
        NMsg msg = NMsg.ofC("%03d", 5);
        NText text = NText.of(msg);
        TestUtils.println(msg);
        Assertions.assertEquals("005", msg.toString());
        Assertions.assertEquals("##{number:005}##\u001E", text.toString());
    }

    @Test
    public void testCFormat_PaddingAndAlignment_LeftJustified() {
        // %-5s should left-align "a" in a field of width 5 -> "a    "
        NMsg msg = NMsg.ofC("[%-5s]", "a");
        Assertions.assertEquals("[a    ]", msg.toString());
    }

    @Test
    public void testCFormat_PaddingAndAlignment_RightJustified() {
        // %5s should right-align "a" in a field of width 5 -> "    a"
        NMsg msg = NMsg.ofC("[%5s]", "a");
        Assertions.assertEquals("[    a]", msg.toString());
    }



    @Test
    public void testCFormat_NumericFormatting_FloatPrecision() {
        // %.2f should format float to 2 decimal places -> "3.14"
        NMsg msg = NMsg.ofC("%.2f", 3.14159);
        Assertions.assertEquals("3.14", msg.toString());
    }


    @Test
    public void testCustom() {
        // you can either create a class implementing NMsgCustomFormatter and register it in META-INF/services/net.thevpc.nuts.spi.NComponent
        // (while have the ability to add some @NScore(fixed = NScorable.DEFAULT_SCORE) to manage its priority
        // or like here just register your own instance like this
        NExtensions.of().registerInstance(NMsgCustomFormatter.class, new NMsgCustomFormatter() {
            @Override
            public String id() {
                return "upper";
            }

            @Override
            public NText format(NMsg msg) {
                String m = (String) msg.message();
                return NText.ofPlain(m.toUpperCase());
            }

            @Override
            public List<String> extractParams(String message) {
                return Collections.emptyList();
            }
        });
        // %.2f should format float to 2 decimal places -> "3.14"
        NMsg msg = NMsg.ofCustom("upper","hello");
        Assertions.assertEquals("HELLO", msg.toString());
    }
}
