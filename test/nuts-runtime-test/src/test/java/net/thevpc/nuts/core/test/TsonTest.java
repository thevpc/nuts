package net.thevpc.nuts.core.test;

import java.io.StringReader;
import java.math.BigInteger;
import java.util.List;
import java.util.stream.Collectors;

import net.thevpc.nuts.elem.*;
import net.thevpc.nuts.io.NIOUtils;
import net.thevpc.nuts.time.NChronometer;
import net.thevpc.nuts.util.NAssert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.standalone.format.tson.parser.NElementTokenImpl;
import net.thevpc.nuts.runtime.standalone.format.tson.parser.NElementTokenType;
import net.thevpc.nuts.runtime.standalone.format.tson.parser.custom.TsonCustomLexer;
import net.thevpc.nuts.runtime.standalone.format.tson.parser.custom.TsonCustomParser;
import net.thevpc.nuts.util.NOptional;

public class TsonTest {

    @BeforeAll
    static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void test01_00() {
        String tson = "if(MACHINE==\"i9\"){\n" +
                "        JAVA8_HOME  : \"/home/vpc/.jdks/corretto-1.8.0_442\"\n" +
                "        JAVA17_HOME : \"/home/groups/ctsgroup/programs/dev/jdk/jdk-17.0.17+10/\"\n" +
                "        IDEA_HOME   : \"/home/groups/ctsgroup/programs/dev/idea\"\n" +
                "        NUTS_GRAALVM_DIR: \"/home/groups/ctsgroup/programs/dev/graalvm/graalvm-jdk-22+36.1/\"\n" +
                "        INSTALLER_JRE8_LINUX64: \"/home/groups/ctsgroup/programs/dev/jre/openlogic-openjdk-jre-8u402-b06-linux-x64.tar.gz\"\n" +
                "        INSTALLER_JRE8_LINUX32: \"/home/groups/ctsgroup/programs/dev/jre/openlogic-openjdk-jre-8u402-b06-linux-x32.tar.gz\"\n" +
                "        INSTALLER_JRE8_WINDOWS64: \"/home/groups/ctsgroup/programs/dev/jre/openlogic-openjdk-jre-8u402-b06-windows-x64.zip\"\n" +
                "        INSTALLER_JRE8_WINDOWS32: \"/home/groups/ctsgroup/programs/dev/jre/openlogic-openjdk-jre-8u402-b06-windows-x32.zip\"\n" +
                "        INSTALLER_JRE8_MAC64: \"/home/groups/ctsgroup/programs/dev/jre/openlogic-openjdk-jre-8u402-b06-mac-x64.zip\"\n" +
                "}";
        NElement parsed = NElementReader.ofTson().read(tson);
        TestUtils.println(parsed.toString());
        TestUtils.println(NElementWriter.ofTson().setFormatterCompact().formatPlain(parsed));
    }


    @Test
    public void test01b() {
        String tson = "a:b b";
        NElement parsed = NElementReader.ofTson().read(tson);
        TestUtils.println(NElementWriter.ofTson().setFormatterCompact().formatPlain(parsed));
    }

    @Test
    public void test01c() {
        String tson = "\n" +
                "// load configuration from the following path. will ignore all the remaining\n" +
                "{\n" +
                " redirect : \"/home/install\"\n" +
                "}\n" +
                "\n";
        NElement parsed = NElementReader.ofTson().read(tson);
        Assertions.assertTrue(parsed.asObject().get().get("redirect").get().asStringValue().get().equals("/home/install"));
        TestUtils.println(NElementWriter.ofTson().setFormatterCompact().formatPlain(parsed));
    }

    @Test
    public void test01d() {
        String tson = "\n" +
                "// load configuration from the following path. will ignore all the remaining\n" +
                "(redirect : \"/home/install\"\n)\n" +
                "\n" +
                "\n";
        NElement parsed = NElementReader.ofTson().read(tson);
        String s = NElementWriter.ofTson().setFormatterCompact().formatPlain(parsed);
        TestUtils.println(s);
        String expected = "// load configuration from the following path. will ignore all the remaining\n" +
                "(redirect:\"/home/install\")";
        Assertions.assertEquals(expected, s);
    }

    @Test
    public void test010() {
        String tson = "// This is the main nops config file\n" +
                "// update paths accordingly\n" +
                "\n" +
                "env{\n" +
                "//    JAVA8_HOME  : \"/home/vpc/.jdks/corretto-1.8.0_442\"\n" +
                "\n" +
                "    JAVA8_HOME  : \"/home/vpc/programs/dev/jdk/jdk8u472-b08/\"\n" +
                "\n" +
                "\n" +
                "\n" +
                "    NUTS_INSTALLER_TARGET: \"linux-x64\"\n" +
                "}\n" +
                "\n" +
                "\n" +
                "\n";
        NElement parsed = NElementReader.ofTson().read(tson);
        Assertions.assertEquals("env", parsed.asObject().get().name().get());
        TestUtils.println(NElementWriter.ofTson().setFormatterCompact().formatPlain(parsed));
    }

    @Test
    public void test011() {
        String tson = "env{\n" +
                "    MACHINE=\"DESKTOP\"\n" +
                "\n" +
                "    if(MACHINE==\"DESKTOP\"){\n" +
                "        JAVA8_HOME  : \"/home/vpc/.jdks/corretto-1.8.0_442\"\n" +
                "    }\n" +
                "\n" +
                "    if(MACHINE==\"LAPTOP\"){\n" +
                "        JAVA8_HOME  : \"/home/vpc/programs/dev/jdk/jdk8u472-b08/\"\n" +
                "    }\n" +
                "\n" +
                "    MVN_HOME                       : \"${IDEA_HOME}/plugins/maven/lib/maven3\"\n" +
                "}\n";
        NElement parsed = NElementReader.ofTson().read(tson);
        TestUtils.println(NElementWriter.ofTson().setFormatterCompact().formatPlain(parsed));

        NObjectElement env = parsed.asNamedObject("env").get();
        NBinaryOperatorElement assign1 = env.get(0).get().asFlatExpression().get().reshape().asBinaryOperator(NOperatorSymbol.EQ).get();
        NObjectElement if1 = env.get(1).get().asFullObject("if").get();
        NObjectElement if2 = env.get(2).get().asFullObject("if").get();
        NPairElement assign2 = env.get(3).get().asNamedPair("MVN_HOME").get();
        Assertions.assertEquals("env", env.name().get());

    }

    @Test
    public void test013() {
        String tson = "\n" +
                "// load configuration from the following path. will ignore all the remaining\n" +
                "@a(/**/redirect : \"/home/install\"\n)\n" +
                "// load configuration from the following path. will ignore all the remaining\n" +
                "@b(redirect : \"/home/install\"\n)\n" +
                "\n" +
                "\n";
        NElement parsed = NElementReader.ofTson().read(tson);
        TestUtils.println(NElementWriter.ofTson().setFormatterCompact().formatPlain(parsed));
    }

    @Test
    public void test015() {
        NObjectElement b1 = NElement.ofObjectBuilder().name("a").addParam("a", "a").add("b", "b").build();
        NObjectElement b2 = b1.builder().build();
        Assertions.assertEquals(b1, b2);
    }

    @Test
    public void test01() {
        String tson = "a:b {a:b} @a a(b,c)[x]";
        NElement parsed = NElementReader.ofTson().read(tson);
        TestUtils.println(parsed);
    }

    @Test
    public void test014() {
//        String tson = "github(\"thevpc/nsh\" , \"/xprojects/nuts-world/nuts-companions\"   , tag:[\"nuts-world\",\"nuts\"] , mvnDeploy:\"thevpc\" )";
        String tson = "[1,2] b:3";
        NElement parsed = NElementReader.ofTson().read(tson);
        NFragmentElement o = parsed.asFragment().get();
        Assertions.assertEquals(2, o.size());
        NArrayElement a = o.get(0).get().asArray().get();
        NPairElement b = o.get(1).get().asPair().get();
        TestUtils.println(parsed);
    }


    @Test
    public void test02() {
        String tson = "x:1.2%g";
        NElement parsed = NElementReader.ofTson().read(tson);
        TestUtils.println(parsed.toString());
    }

    @Test
    public void test03() {
        String tson = "@(here){a:b}";
        NElement parsed = NElementReader.ofTson().read(tson);
        TestUtils.println(parsed.toString());
    }

    @Test
    public void test04() {
        String tson = "-1";
        NElement parsed = NElementReader.ofTson().read(tson);
        TestUtils.println(parsed.toString());
    }

    @Test
    public void test05() {
        String tson = "-1E-5";
        NElement parsed = NElementReader.ofTson().read(tson);
        TestUtils.println(parsed.toString());
    }

    @Test
    public void test06() {
        String tson = "ω=π+freq*g";
        NElement parsed = NElementReader.ofTson().read(tson);
        NElement reshaped = parsed.asFlatExpression().get().reshape();
        Assertions.assertTrue(reshaped.isBinaryInfixOperator(NOperatorSymbol.EQ));
        Assertions.assertEquals("ω", reshaped.asBinaryOperator().get().firstOperand().asStringValue().get());
        NElement so = reshaped.asBinaryOperator().get().secondOperand();
        Assertions.assertTrue(so.isBinaryInfixOperator(NOperatorSymbol.PLUS));
        Assertions.assertEquals("π", so.asBinaryOperator().get().firstOperand().asStringValue().get());
        NElement m = so.asBinaryOperator().get().secondOperand();
        Assertions.assertTrue(m.isBinaryInfixOperator(NOperatorSymbol.MUL));
        TestUtils.println(parsed.toString());
    }


    @Test
    public void test07() {
        String tson = "-1E-5hello";
        NElement parsed = NElementReader.ofTson().read(tson);
        TestUtils.println(parsed.toString());
    }

    @Test
    public void test08() {
        String tson = "ω=π+f*g";
//        String tson = "-1E-5hello";
        TsonCustomLexer parsed = new TsonCustomLexer(new StringReader(tson));
        List<NElementTokenImpl> a = parsed.all();
        Assertions.assertEquals(7, a.size());
        Assertions.assertEquals("ω", a.get(0).image());
        Assertions.assertEquals("=", a.get(1).image());
        Assertions.assertEquals("π", a.get(2).image());
        Assertions.assertEquals("+", a.get(3).image());
        Assertions.assertEquals("f", a.get(4).image());
        Assertions.assertEquals("*", a.get(5).image());
        Assertions.assertEquals("g", a.get(6).image());
    }

    @Test
    public void test09() {
        String tson = "-1E-5hello";
        TsonCustomLexer parsed = new TsonCustomLexer(new StringReader(tson));
        List<NElementTokenImpl> a = parsed.all();
        TestUtils.println(a);
        Assertions.assertEquals(1, a.size());
        Assertions.assertEquals("-1E-5hello", a.get(0).image());
    }

    @Test
    public void test11_comments() {
        String tson = "// line comment\n/* block\n comment */";
        TsonCustomLexer lexer = new TsonCustomLexer(new StringReader(tson));
        List<NElementTokenImpl> tokens = lexer.all();
        Assertions.assertEquals(2, tokens.size());
        Assertions.assertEquals(NElementTokenType.LINE_COMMENT, tokens.get(0).type());
        Assertions.assertEquals(NElementTokenType.BLOCK_COMMENT, tokens.get(1).type());
    }

    @Test
    public void test12_unicode_identifiers() {
        String tson = "ω π identifier_123 identifier-two  identifier.two $var";
        TsonCustomLexer lexer = new TsonCustomLexer(new StringReader(tson));
        List<NElementTokenImpl> tokens = lexer.all();
        Assertions.assertEquals("ω", tokens.get(0).image());
        Assertions.assertEquals("π", tokens.get(2).image());
        Assertions.assertEquals("identifier_123", tokens.get(4).image());
        Assertions.assertEquals("identifier-two", tokens.get(6).image());
        Assertions.assertEquals("identifier.two", tokens.get(8).image());
        Assertions.assertEquals("$var", tokens.get(10).image());
    }

    @Test
    public void test13_numbers_bitwidth() {
        String tson = "123s8 456u16 789s32 1011u64 1213n";
        TsonCustomLexer lexer = new TsonCustomLexer(new StringReader(tson));
        List<NElementTokenImpl> tokens = lexer.all();
        Assertions.assertEquals("123s8", tokens.get(0).image());
        Assertions.assertEquals("456u16", tokens.get(2).image());
        Assertions.assertEquals("789s32", tokens.get(4).image());
        Assertions.assertEquals("1011u64", tokens.get(6).image());
        Assertions.assertEquals("1213n", tokens.get(8).image());
    }

    @Test
    public void test14_complex_numbers() {
        String tson = "1+2i 3.5-4.2i 5i";
        TsonCustomLexer lexer = new TsonCustomLexer(new StringReader(tson));
        List<NElementTokenImpl> tokens = lexer.all();
        Assertions.assertEquals("1+2i", tokens.get(0).image());
        Assertions.assertEquals("3.5-4.2i", tokens.get(2).image());
        Assertions.assertEquals("5i", tokens.get(4).image());
    }

    @Test
    public void test15_temporal_types() {
        String tson = "2023-10-27 10:30:00 2023-10-27T10:30:00Z";
        TsonCustomLexer lexer = new TsonCustomLexer(new StringReader(tson));
        List<NElementTokenImpl> tokens = lexer.all();
        Assertions.assertEquals(NElementTokenType.DATE, tokens.get(0).type());
        Assertions.assertEquals(NElementTokenType.TIME, tokens.get(2).type());
        Assertions.assertEquals(NElementTokenType.INSTANT, tokens.get(4).type());
    }

    @Test
    public void test16_streams1() {
        String tson = "^[SGVsbG8=]";
        TsonCustomLexer lexer = new TsonCustomLexer(new StringReader(tson));
        List<NElementTokenImpl> tokens = lexer.all();
        Assertions.assertEquals("^[SGVsbG8=]", tokens.get(0).image());
    }

    @Test
    public void test16_streams2() {
        String tson = "^b64[SGVsbG8=]";
        TsonCustomLexer lexer = new TsonCustomLexer(new StringReader(tson));
        List<NElementTokenImpl> tokens = lexer.all();
        Assertions.assertEquals("^b64[SGVsbG8=]", tokens.get(0).image());
    }

    @Test
    public void test16_streams3() {
        String tson = "^myid{Hello World^myid}";
        TsonCustomLexer lexer = new TsonCustomLexer(new StringReader(tson));
        List<NElementTokenImpl> tokens = lexer.all();
        Assertions.assertEquals("^myid{Hello World^myid}", tokens.get(0).image());
    }

    @Test
    public void test16_streams4() {
        String tson = "^m{Hello World^m}";
        TsonCustomLexer lexer = new TsonCustomLexer(new StringReader(tson));
        List<NElementTokenImpl> tokens = lexer.all();
        Assertions.assertEquals("^m{Hello World^m}", tokens.get(0).image());
    }

    @Test
    public void test16_streams5() {
        String tson = "^^ ^^^";
        TsonCustomLexer lexer = new TsonCustomLexer(new StringReader(tson));
        List<NElementTokenImpl> tokens = lexer.all();
        Assertions.assertEquals("^^", tokens.get(0).image());
        Assertions.assertEquals("^^^", tokens.get(2).image());
    }

    @Test
    public void test17_custom_parser() {
        String tson = "/*hello*/a:b {c:d, e:[1,2,3]} @ann(x:y) f(g)";
        TsonCustomParser parser = new TsonCustomParser(tson);
        NElement element = parser.parseDocument();
        TestUtils.println(element);
        Assertions.assertNotNull(element);
    }

    @Test
    public void test18_custom_parser() {
        String tson = "a:b} {c:d";
        TsonCustomParser parser = new TsonCustomParser(tson);
        NElement element = parser.parseDocument();
        TestUtils.println(element);
        Assertions.assertNotNull(element);
    }

    @Test
    public void testBlocComments() {
        String tson = "  /* Apple\n.. Banana\n.. Cherry\n. Date\n**/ ";
        TsonCustomParser parser = new TsonCustomParser(tson);
        NElement element = parser.parseDocument();
        TestUtils.println(element);
    }

    @Test
    public void testList_unordered_basic_ast() {
        String tson = "[.] Apple\n[..] Banana\n[..] Cherry\n[.] Date";
        TsonCustomParser parser = new TsonCustomParser(tson);
        NElement element = parser.parseDocument();
        TestUtils.println(element);

        Assertions.assertNotNull(element);

        // 1. Confirm it's a list (and specifically unordered)
        Assertions.assertTrue(element.isList());
        Assertions.assertTrue(element.isUnorderedList());
        NOptional<NListElement> topLevelOpt = element.asList();
        Assertions.assertTrue(topLevelOpt.isPresent());
        NListElement topLevel = topLevelOpt.get();

        // 2. Top-level list has 2 items: "Apple", "Date"
        Assertions.assertEquals(2, topLevel.items().size());

        // 3. First item: "Apple" with a sublist
        NListItemElement firstItem = topLevel.items().get(0);
        Assertions.assertTrue(firstItem.value().isPresent());
        // Assuming value is an NString-like element; compare raw content if needed
        Assertions.assertEquals("Apple", firstItem.value().get().asName().get().stringValue());

        NOptional<NListElement> subListOpt = firstItem.subList();
        Assertions.assertTrue(subListOpt.isPresent());
        NListElement subList = subListOpt.get();
        Assertions.assertTrue(subList.isUnorderedList()); // sublist inherits type
        Assertions.assertEquals(2, subList.items().size());

        // 4. Sublist items: "Banana", "Cherry"
        Assertions.assertEquals("Banana", subList.items().get(0).value().get().asName().get().stringValue());
        Assertions.assertEquals("Cherry", subList.items().get(1).value().get().asName().get().stringValue());

        // 5. Second top-level item: "Date", no sublist
        NListItemElement secondItem = topLevel.items().get(1);
        Assertions.assertTrue(secondItem.value().isPresent());
        Assertions.assertEquals("Date", secondItem.value().get().asName().get().stringValue());
        Assertions.assertTrue(secondItem.subList().isEmpty());

    }

    @Test
    public void testStr() {
        TestUtils.println(NElement.ofString("Hello \"world\""));
    }

    @Test
    public void testUnorderedList_depthDriven_roundTrip() {
        String tson = "/*01*/[.]/*02*/A   [.....]   B   [..]   C";
        TsonCustomParser parser = new TsonCustomParser(tson);
        NElement element = parser.parseDocument();
        TestUtils.println(element);

        Assertions.assertNotNull(element);
        // ensure same format and spaces
        Assertions.assertEquals(tson, element.toString());

        Assertions.assertTrue(element.isUnorderedList());

        NOptional<NListElement> topLevelOpt = element.asUnorderedList();
        Assertions.assertTrue(topLevelOpt.isPresent());
        NListElement topLevel = topLevelOpt.get();

        // Top-level list has depth 1
        Assertions.assertEquals(1, topLevel.depth());
        // Only one top-level item: A
        Assertions.assertEquals(1, topLevel.items().size());

        // First (and only) item is A
        NListItemElement itemA = topLevel.items().get(0);
        Assertions.assertEquals(1, itemA.depth());
        Assertions.assertTrue(itemA.value().isPresent());
        Assertions.assertEquals("A", itemA.value().get().asName().get().stringValue());

        // A must have a sublist
        Assertions.assertTrue(itemA.subList().isPresent());
        NListElement subList = itemA.subList().get();

        // Sublist depth = min(5, 2) = 2
        Assertions.assertEquals(2, subList.depth());
        Assertions.assertEquals(2, subList.items().size());

        // First child: B (depth=5)
        NListItemElement itemB = subList.items().get(0);
        Assertions.assertEquals(5, itemB.depth());
        Assertions.assertTrue(itemB.value().isPresent());
        Assertions.assertEquals("B", itemB.value().get().asName().get().stringValue());
        // B has no sublist
        Assertions.assertTrue(itemB.subList().isEmpty());

        // Second child: C (depth=2)
        NListItemElement itemC = subList.items().get(1);
        Assertions.assertEquals(2, itemC.depth());
        Assertions.assertTrue(itemC.value().isPresent());
        Assertions.assertEquals("C", itemC.value().get().asName().get().stringValue());
        // C has no sublist
        Assertions.assertTrue(itemC.subList().isEmpty());
    }

    @Test
    public void testList_unordered_basic_ast3() {
        String tson = "[.] A\n" +
                "[..] B\n" +
                "[...] C\n" +
                "[..] D";

        TsonCustomParser parser = new TsonCustomParser(tson);
        NElement element = parser.parseDocument();
        TestUtils.println(element);

        Assertions.assertNotNull(element);
        Assertions.assertTrue(element.isUnorderedList());

        NOptional<NListElement> topLevelOpt = element.asUnorderedList();
        Assertions.assertTrue(topLevelOpt.isPresent());
        NListElement topLevel = topLevelOpt.get();

        // Top-level list has depth 1
        Assertions.assertEquals(1, topLevel.depth());
        // Only one top-level item: A
        Assertions.assertEquals(1, topLevel.items().size());

        // === Item A ===
        NListItemElement itemA = topLevel.items().get(0);
        Assertions.assertEquals(1, itemA.depth());
        Assertions.assertTrue(itemA.value().isPresent());
        Assertions.assertEquals("A", itemA.value().get().asName().get().stringValue());

        // A must have a sublist
        Assertions.assertTrue(itemA.subList().isPresent());
        NListElement subListA = itemA.subList().get();
        Assertions.assertEquals(2, subListA.depth()); // min(2, 2) = 2
        Assertions.assertEquals(2, subListA.items().size());

        // === Item B (first child of A's sublist) ===
        NListItemElement itemB = subListA.items().get(0);
        Assertions.assertEquals(2, itemB.depth());
        Assertions.assertTrue(itemB.value().isPresent());
        Assertions.assertEquals("B", itemB.value().get().asName().get().stringValue());

        // B must have a sublist
        Assertions.assertTrue(itemB.subList().isPresent());
        NListElement subListB = itemB.subList().get();
        Assertions.assertEquals(3, subListB.depth()); // only child has depth 3
        Assertions.assertEquals(1, subListB.items().size());

        // === Item C (child of B's sublist) ===
        NListItemElement itemC = subListB.items().get(0);
        Assertions.assertEquals(3, itemC.depth());
        Assertions.assertTrue(itemC.value().isPresent());
        Assertions.assertEquals("C", itemC.value().get().asName().get().stringValue());
        Assertions.assertTrue(itemC.subList().isEmpty()); // no further nesting

        // === Item D (second child of A's sublist) ===
        NListItemElement itemD = subListA.items().get(1);
        Assertions.assertEquals(2, itemD.depth());
        Assertions.assertTrue(itemD.value().isPresent());
        Assertions.assertEquals("D", itemD.value().get().asName().get().stringValue());
        Assertions.assertTrue(itemD.subList().isEmpty()); // no sublist
    }

    @Test
    public void testList_unordered_basic() {
        String tson = "[.] Apple\n[..] Banana\n[..] Cherry\n[.] Date";
        TsonCustomLexer lexer = new TsonCustomLexer(new StringReader(tson));
        List<NElementTokenImpl> tokens = lexer.all().stream().filter(x -> x.type() != NElementTokenType.SPACE).collect(Collectors.toList());
        Assertions.assertEquals("[.]", tokens.get(0).image());
        Assertions.assertEquals("Apple", tokens.get(1).image());
        Assertions.assertEquals("[..]", tokens.get(3).image());
        Assertions.assertEquals("Banana", tokens.get(4).image());
        Assertions.assertEquals("[..]", tokens.get(6).image());
        Assertions.assertEquals("Cherry", tokens.get(7).image());
        Assertions.assertEquals("[.]", tokens.get(9).image());
        Assertions.assertEquals("Date", tokens.get(10).image());
    }

    @Test
    public void testNumberConstants() {
        // Signed Integers
        checkConstant("0max_s8", (byte) 127);
        checkConstant("0min_s8", (byte) -128);
        checkConstant("0max_s16", (short) 32767);
        checkConstant("0min_s16", (short) -32768);
        checkConstant("0max_s32", 2147483647);
        checkConstant("0min_s32", -2147483648);
        checkConstant("0max_s64", 9223372036854775807L);
        checkConstant("0min_s64", -9223372036854775808L);

        // Unsigned Integers
        checkConstant("0max_u8", (short) 255);
        checkConstant("0min_u8", (short) 0);
        checkConstant("0max_u16", 65535);
        checkConstant("0min_u16", 0);
        checkConstant("0max_u32", 4294967295L);
        checkConstant("0min_u32", 0L);
        checkConstant("0max_u64", new BigInteger("18446744073709551615"));
        checkConstant("0min_u64", BigInteger.ZERO);

        // Floating Point
        checkConstant("0max_f32", Float.MAX_VALUE);
        checkConstant("0min_f32", Float.MIN_VALUE);
        checkConstant("0pinf_f32", Float.POSITIVE_INFINITY);
        checkConstant("0ninf_f32", Float.NEGATIVE_INFINITY);
        checkConstant("0nan_f32", Float.NaN);

        checkConstant("0max_f64", Double.MAX_VALUE);
        checkConstant("0min_f64", Double.MIN_VALUE);
        checkConstant("0pinf_f64", Double.POSITIVE_INFINITY);
        checkConstant("0ninf_f64", Double.NEGATIVE_INFINITY);
        checkConstant("0nan_f64", Double.NaN);
        checkConstant("0NaN", Double.NaN);
    }

    @Test
    public void testNumberConstantsCaseInsensitivity() {
        checkConstant("0MAX_s8", (byte) 127);
        checkConstant("0Pinf_f32", Float.POSITIVE_INFINITY);
        checkConstant("0nAn_f64", Double.NaN);
    }

    @Test
    public void testNumberConstantsUnderscoreOptionality() {
        checkConstant("0maxs8", (byte) 127);
        checkConstant("0max", Integer.MAX_VALUE);
        checkConstant("0pinf_f32", Float.POSITIVE_INFINITY);
    }

    @Test
    public void testSpecial() {
        NElement e = NElementReader.ofTson().read("styles{\n" +
                "    (*){\n" +
                "        font-size : 5%P\n" +
                "        debug-color: red,\n" +
                "        font-family : \"Serif\",\n" +
                "    }}");
        TestUtils.println(e);
    }
    @Test
    public void testSpecial0() {
        NElement e = NElementReader.ofTson().read("(*)");
        TestUtils.println(e);
        NUpletElement u = e.asUplet().get();
        Assertions.assertEquals(1, u.size());
        u.get(0).get().asOperatorSymbol(NOperatorSymbol.MUL).get();
    }

    @Test
    public void testSpecial1() {
        String expected = "styles{\n" +
                "    (*){\n" +
                "        a,b,c,\n" +
                "    }}";
        NElement e = NElementReader.ofTson().read(expected);
        String s2 = e.toString();
        TestUtils.println(s2);
        Assertions.assertEquals(expected, s2);
    }

    @Test
    public void testSpecial2() {
        String expected = "{" +
                "a,b\n" +
                "}";
        NElement e = NElementReader.ofTson().read(expected);
        String s2 = e.toString();
        TestUtils.println(s2);
        Assertions.assertEquals(expected, s2);
    }

    @Test
    public void testSpecial3() {
        String expected = "{include(\n" +
                "    eitherPath(\n" +
                "        \"$HOME/xprojects/nuts-world/nuts-productivity/ntexup/ntexup-templates/${themeName}/v1.0/theme\"\n" +
                "        \"github://thevpc/ntexup-templates/${themeName}/v1.0/theme\"\n" +
                "    )\n" +
                ")\n" +
                "\n" +
                "@define miniPage{\n" +
                "    group(background:white,draw-contour){\n" +
                "        body\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "@define miniCode{\n" +
                "    group(background:Gray89,draw-contour,margin:5){\n" +
                "        body\n" +
                "    }\n" +
                "}\n" +
                "\n" +
                "@define miniCodeNTexup(code){\n" +
                "    group(background:Gray89,draw-contour,margin:5){\n" +
                "        source(ntexup, code)\n" +
                "    }\n" +
                "}\n}";
        NElement e = NElementReader.ofTson().read(expected);
        String s2 = e.toString();
        TestUtils.println(s2);
        Assertions.assertEquals(expected, s2);
    }

    @Test
    public void testSpecial11() {
        String expected =
                "@define mini(code){\n" +
                        "}";
        NElement e = NElementReader.ofTson().read(expected);
        String s2 = e.toString();
        TestUtils.println(s2);
        Assertions.assertEquals(expected, s2);
    }

    @Test
    public void testSpecial4() {
        String expected =
                "@define miniPage{\n" +
                        "}";
        NElement e = NElementReader.ofTson().read(expected);
        String s2 = e.toString();
        TestUtils.println(s2);
        Assertions.assertEquals(expected, s2);
    }

    @Test
    public void testSpecial5() {
        String expected =
                "@define() miniPage[\n" +
                        "]";
        NElement e = NElementReader.ofTson().read(expected);
        String s2 = e.toString();
        TestUtils.println(s2);
        Assertions.assertEquals(expected, s2);
    }

    @Test
    public void testSpecial6() {
        String expected =
                "@define() [\n" +
                        "]";
        NElement e = NElementReader.ofTson().read(expected);
        String s2 = e.toString();
        TestUtils.println(s2);
        Assertions.assertEquals(expected, s2);
    }

    @Test
    public void testSpecial8() {
        String expected =
                "@define() {\n" +
                        "}";
        NElement e = NElementReader.ofTson().read(expected);
        String s2 = e.toString();
        TestUtils.println(s2);
        Assertions.assertEquals(expected, s2);
    }

    @Test
    public void testSpecial9() {
        String expected =
                "@define() (){\n" +
                        "}";
        NElement e = NElementReader.ofTson().read(expected);
        String s2 = e.toString();
        TestUtils.println(s2);
        Assertions.assertEquals(expected, s2);
    }

    @Test
    public void testSpecial10() {
        String expected =
                "@define() a";
        NElement e = NElementReader.ofTson().read(expected);
        String s2 = e.toString();
        TestUtils.println(s2);
        Assertions.assertEquals(expected, s2);
    }

    @Test
    public void testSpecial7() {
        String expected =
                "@define 13";
        NElement e = NElementReader.ofTson().read(expected);
        String s2 = e.toString();
        TestUtils.println(s2);
        Assertions.assertEquals(expected, s2);
    }

    @Test
    public void testSpecial12() {
        String expected = "    eitherPath(\n" +
                "        \"$HOME/xprojects/nuts-world/nuts-productivity/ntexup/ntexup-templates/${themeName}/v1.0/theme\"\n" +
                "        \"github://thevpc/ntexup-templates/${themeName}/v1.0/theme\"\n" +
                "    )\n";
        NElement e = NElementReader.ofTson().read(expected);
        String s2 = e.toString();
        TestUtils.println(s2);
        Assertions.assertEquals(expected, s2);
    }

    @Test
    public void testSpecial13() {
        String expected = "agenda-slide(title:\"Rationale\"){\n" +
                "    body{\n" +
                "        a: b\n" +
                "        a: •¶ Text-based, declarative, and intuitive syntax\n" +
                "           •• {a:b}\n" +
                "           •• {a:b}\n" +
                "           • \"test\"\n" +
                "\n" +
                "        •¶ Text-based, declarative, and intuitive syntax\n" +
                "        •¶ Readable by humans, writable with ease\n" +
                "        •¶ Designed for long-lived documents with effortless maintenance\n" +
                "        •¶ Unmatched control over rendering\n" +
                "        •¶ Seamless multi-file support\n" +
                "        •¶ Version-control friendly (Git & more)\n" +
                "        •¶ Integrates with LaTeX, UML, and beyond\n" +
                "    }\n" +
                "}\n";
        NElement e = NElementReader.ofTson().read(expected);
        String s2 = e.toString();
        TestUtils.println(s2);
        Assertions.assertEquals(expected, s2);
    }

    @Test
    public void testSpecial14() {
        String expected =
                "        •¶ Text-based, declarative, and intuitive syntax\n" +
                        "        •¶ Readable by humans, writable with ease\n" +
                        "        •¶ Designed for long-lived documents with effortless maintenance\n" +
                        "        •¶ Unmatched control over rendering\n" +
                        "        •¶ Seamless multi-file support\n" +
                        "        •¶ Version-control friendly (Git & more)\n" +
                        "        •¶ Integrates with LaTeX, UML, and beyond\n";
        NElement e = NElementReader.ofTson().read(expected);
        String s2 = e.toString();
        TestUtils.println(s2);
        Assertions.assertEquals(expected, s2);
    }

    @Test
    public void testSpecial15() {
        String expected =
                          "• a+b\n"
                        + "• b\n"
                ;
        NElement e = NElementReader.ofTson().read(expected);
        String s2 = e.toString();
        TestUtils.println(s2);
        Assertions.assertEquals(expected, s2);
    }

    @Test
    public void testSpecial16() {
        String expected =
                          "•¶ a\n"
                        + "•¶ b\n"
                ;
        NElement e = NElementReader.ofTson().read(expected);
        String s2 = e.toString();
        TestUtils.println(s2);
        Assertions.assertEquals(expected, s2);
    }

    @Test
    public void testSpecial17() {
        String expected =
                          "• ¶ a\n"
                        + "• ¶ b\n";
        NElement e = NElementReader.ofTson().read(expected);
        String s2 = e.toString();
        TestUtils.println(s2);
        Assertions.assertEquals(expected, s2);
    }

    @Test
    public void testSpecial18() {
        String expected =
                          "• ¶ a\n"
                        ;
        NElement e = NElementReader.ofTson().read(expected);
        String s2 = e.toString();
        TestUtils.println(s2);
        Assertions.assertEquals(expected, s2);
    }




    @Test
    public void testSpecial19() {
        NElement e = NElementReader.ofTson().read("@()text()");
        TestUtils.println(e.toPrettyString());
    }

    @Test
    public void testSpecial20() {
        NElement e = NElementReader.ofTson().read("a\"b\"");
        TestUtils.println(e.toPrettyString());
    }

    @Test
    public void testSpecial21() {
        NElement e = NElementReader.ofTson().read("  @(\"version\") text(\"value\" : either(\"${documentVersion}\" \"\")) ");
        TestUtils.println(e.toPrettyString());
    }

    @Test
    public void testSpecial22() {
        String expected =
                "port : a b"
                ;
        NElement e = NElementReader.ofTson().read(expected);
        String s2 = e.toString();
        TestUtils.println(s2);
        Assertions.assertEquals(expected, s2);
    }

    @Test
    public void testSpecial23() {
        NChronometer c = NChronometer.startNow();
        String expected = NIOUtils.readString(
                TsonTest.class.getResourceAsStream("bigtson.tson")
        );
        c.stop();
        TestUtils.println("load in "+c);

        c = NChronometer.startNow();
        NElement e = NElementReader.ofTson().read(expected);
        c.stop();
        TestUtils.println("read in "+c);

        c = NChronometer.startNow();
        String s2 = e.toString();
        c.stop();
        TestUtils.println("toString in "+c);

        c = NChronometer.startNow();
        s2 = e.toPrettyString();
        c.stop();
        TestUtils.println("toPrettyString in "+c);

        c = NChronometer.startNow();
        s2 = e.toCompactString();
        c.stop();
        TestUtils.println("toCompactString in "+c);
    }


    @Test
    public void testEdges() {
        checkConstant("0maxs32", Integer.MAX_VALUE);
    }

    @Test
    public void testNumberConstantsSuffixes() {
        NElement e;
        e = net.thevpc.nuts.elem.NElementReader.ofTson().read("0max_s8%");
        Assertions.assertEquals(NElementType.BYTE, e.type());
        Assertions.assertEquals((byte) 127, e.asByteValue().get());
        Assertions.assertEquals("%", e.asNumber().get().numberSuffix());

        e = net.thevpc.nuts.elem.NElementReader.ofTson().read("0pinf_f32ms");
        Assertions.assertEquals(NElementType.FLOAT, e.type());
        Assertions.assertEquals(Float.POSITIVE_INFINITY, e.asFloatValue().get());
        Assertions.assertEquals("ms", e.asNumber().get().numberSuffix());

        e = net.thevpc.nuts.elem.NElementReader.ofTson().read("0ninf_f32ms");
        Assertions.assertEquals(NElementType.FLOAT, e.type());
        Assertions.assertEquals(Float.NEGATIVE_INFINITY, e.asFloatValue().get());
        Assertions.assertEquals("ms", e.asNumber().get().numberSuffix());

        e = net.thevpc.nuts.elem.NElementReader.ofTson().read("0nan_ms");
        Assertions.assertEquals(NElementType.DOUBLE, e.type());
        Assertions.assertEquals(Double.NaN, e.asDoubleValue().get());
        Assertions.assertEquals("ms", e.asNumber().get().numberSuffix());

        e = net.thevpc.nuts.elem.NElementReader.ofTson().read("0nanms");
        Assertions.assertEquals(NElementType.INT, e.type());
        Assertions.assertEquals(0, e.asIntValue().get());
        Assertions.assertEquals("nanms", e.asNumber().get().numberSuffix());

        // auto
        e = net.thevpc.nuts.elem.NElementReader.ofTson().read(String.valueOf(Long.MAX_VALUE));
        Assertions.assertEquals(NElementType.LONG, e.type());
        Assertions.assertEquals(Long.MAX_VALUE, e.asLongValue().get());
        Assertions.assertEquals(null, e.asNumber().get().numberSuffix());
    }

    private void checkConstant(String tson, Object expected) {
        NElement e = net.thevpc.nuts.elem.NElementReader.ofTson().read(tson);
        NNumberElement nbr = e.asNumber().get();
        Assertions.assertEquals(tson, e.toString());
        Object actual = nbr.numberValue();
        if (expected instanceof Float && ((Float) expected).isNaN()) {
            Assertions.assertTrue(actual instanceof Float && ((Float) actual).isNaN(), "Expected NaN (float) for " + tson + " but got " + actual);
        } else if (expected instanceof Double && ((Double) expected).isNaN()) {
            Assertions.assertTrue(actual instanceof Double && ((Double) expected).isNaN(), "Expected NaN (double) for " + tson + " but got " + actual);
        } else {
            Assertions.assertEquals(expected, actual, "Failed for " + tson);
        }
    }
}
