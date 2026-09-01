package net.thevpc.nuts.core.test;

import net.thevpc.nuts.collections.NCollections;
import net.thevpc.nuts.collections.NMaps;
import net.thevpc.nuts.util.NOptional;
import net.thevpc.nuts.util.NToStringBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

public class ToStringTest {

    public static class StaticNested {
    }

    public class MemberInner {
    }

    @Test
    public void testBasicAndFactories() {
        // Anonymous empty
        Assertions.assertEquals("{}", NToStringBuilder.of().toString());

        // Named empty
        Assertions.assertEquals("User{}", NToStringBuilder.of("User").toString());

        // From top-level class
        Assertions.assertEquals("ToStringTest{}", NToStringBuilder.of(ToStringTest.class).toString());

        // From object instance
        Assertions.assertEquals("ToStringTest{}", NToStringBuilder.of(this).toString());

        // Static nested class
        Assertions.assertEquals("ToStringTest.StaticNested{}", NToStringBuilder.of(StaticNested.class).toString());
        Assertions.assertEquals("ToStringTest.StaticNested{}", NToStringBuilder.of(new StaticNested()).toString());

        // Member inner class
        Assertions.assertEquals("ToStringTest.MemberInner{}", NToStringBuilder.of(MemberInner.class).toString());
        Assertions.assertEquals("ToStringTest.MemberInner{}", NToStringBuilder.of(new MemberInner()).toString());

        // Local class inside method
        class LocalClass {
        }
        Assertions.assertEquals("ToStringTest.LocalClass{}", NToStringBuilder.of(LocalClass.class).toString());

        // Anonymous inner class
        Object anon = new Object() {
        };
        Assertions.assertEquals("ToStringTest$anonymous{}", NToStringBuilder.of(anon).toString());

        // Array class
        Assertions.assertEquals("ToStringTest.StaticNested[]{}", NToStringBuilder.of(StaticNested[].class).toString());

        // Simple single line
        String s1 = NToStringBuilder.of("User")
                .add("id", 100)
                .add("name", "Alice")
                .add("active", true)
                .toString();
        Assertions.assertEquals("User{id : 100, name : \"Alice\", active : true}", s1);
    }

    @Test
    public void testPrimitiveTypesAndArrays() {
        String res = NToStringBuilder.of("Data")
                .add("b", (byte) 1)
                .add("s", (short) 2)
                .add("i", 3)
                .add("l", 4L)
                .add("f", 5.5f)
                .add("d", 6.6)
                .add("bool", true)
                .add("c", 'X')
                .add("ints", new int[]{1, 2, 3})
                .add("bools", new boolean[]{true, false})
                .add("matrix", new Integer[][]{{1, 2}, {3, 4}})
                .singleLine()
                .toString();

        Assertions.assertTrue(res.contains("b : 1"));
        Assertions.assertTrue(res.contains("s : 2"));
        Assertions.assertTrue(res.contains("i : 3"));
        Assertions.assertTrue(res.contains("l : 4"));
        Assertions.assertTrue(res.contains("bool : true"));
        Assertions.assertTrue(res.contains("ints : [1, 2, 3]"));
        Assertions.assertTrue(res.contains("bools : [true, false]"));
        Assertions.assertTrue(res.contains("matrix : [[1, 2], [3, 4]]"));
    }

    @Test
    public void testOptionalsAndSuppliers() {
        String res = NToStringBuilder.of("OptTest")
                .add("javaOptPresent", Optional.of("hello"))
                .add("javaOptEmpty", Optional.empty())
                .add("nutsOptPresent", NOptional.of("world"))
                .add("nutsOptEmpty", NOptional.ofEmpty())
                .addIf(true, "lazyField", () -> "computedValue")
                .addIf(false, "skippedField", () -> "shouldNotCompute")
                .singleLine()
                .toString();

        Assertions.assertTrue(res.contains("javaOptPresent : \"hello\""));
        Assertions.assertTrue(res.contains("javaOptEmpty : Optional.empty"));
        Assertions.assertTrue(res.contains("nutsOptPresent : \"world\""));
        Assertions.assertTrue(res.contains("nutsOptEmpty : NOptional.empty"));
        Assertions.assertTrue(res.contains("lazyField : \"computedValue\""));
        Assertions.assertFalse(res.contains("skippedField"));
    }

    @Test
    public void testOmitNullsAndOmitEmpty() {
        // omitNulls
        String sNulls = NToStringBuilder.of("Person")
                .omitNulls(true)
                .add("name", "Alice")
                .add("nickname", (String) null)
                .toString();
        Assertions.assertEquals("Person{name : \"Alice\"}", sNulls);

        // omitEmpty
        String sEmpty = NToStringBuilder.of("Container")
                .omitEmpty(true)
                .add("name", "Box")
                .add("emptyStr", "")
                .add("emptyList", Collections.emptyList())
                .add("emptyMap", Collections.emptyMap())
                .add("emptyArray", new String[0])
                .add("emptyOpt", Optional.empty())
                .add("emptyNOpt", NOptional.ofEmpty())
                .toString();
        Assertions.assertEquals("Container{name : \"Box\"}", sEmpty);

        // omitBlanks
        String sBlanks = NToStringBuilder.of("Config")
                .omitBlanks(true)
                .add("key", "secret")
                .add("whitespace", "   \t  \n ")
                .add("empty", "")
                .add("nullVal", (String) null)
                .toString();
        Assertions.assertEquals("Config{key : \"secret\"}", sBlanks);
    }

    @Test
    public void testConditionalAddMethods() {
        String res = NToStringBuilder.of("Conditions")
                .addIfNonBlank("blankStr", "   ")
                .addIfNonBlank("validStr", "hello")
                .addIfNonEmpty("emptyStr", "")
                .addIfNonEmpty("nonEmptyStr", "world")
                .addIfNonEmpty("emptyCol", Collections.emptyList())
                .addIfNonEmpty("nonEmptyCol", Arrays.asList("item1"))
                .addIfNonEmpty("emptyMap", Collections.emptyMap())
                .addIfNonEmpty("nonEmptyMap", NMaps.of("k", "v"))
                .addIfNonEmpty("emptyArr", new int[0])
                .addIfNonEmpty("nonEmptyArr", new int[]{1, 2})
                .addIfTrue("flagTrue", true)
                .addIfTrue("flagFalse", false)
                .addIfFalse("disabledTrue", false)
                .addIfFalse("disabledFalse", true)
                .addIfNonZero("countZero", 0)
                .addIfNonZero("countTen", 10)
                .addIfNonZero("longZero", 0L)
                .addIfNonZero("longVal", 42L)
                .addIfNonZero("doubleZero", 0.0)
                .addIfNonZero("doubleVal", 3.14)
                .singleLine()
                .toString();

        Assertions.assertFalse(res.contains("blankStr"));
        Assertions.assertTrue(res.contains("validStr : \"hello\""));
        Assertions.assertFalse(res.contains("emptyStr"));
        Assertions.assertTrue(res.contains("nonEmptyStr : \"world\""));
        Assertions.assertFalse(res.contains("emptyCol"));
        Assertions.assertTrue(res.contains("nonEmptyCol : [item1]"));
        Assertions.assertFalse(res.contains("emptyMap"));
        Assertions.assertTrue(res.contains("nonEmptyMap"));
        Assertions.assertFalse(res.contains("emptyArr"));
        Assertions.assertTrue(res.contains("nonEmptyArr : [1, 2]"));
        Assertions.assertTrue(res.contains("flagTrue : true"));
        Assertions.assertFalse(res.contains("flagFalse"));
        Assertions.assertTrue(res.contains("disabledTrue : false"));
        Assertions.assertFalse(res.contains("disabledFalse"));
        Assertions.assertFalse(res.contains("countZero"));
        Assertions.assertTrue(res.contains("countTen : 10"));
        Assertions.assertFalse(res.contains("longZero"));
        Assertions.assertTrue(res.contains("longVal : 42"));
        Assertions.assertFalse(res.contains("doubleZero"));
        Assertions.assertTrue(res.contains("doubleVal : 3.14"));
    }

    @Test
    public void testFormattingAndLayoutModes() {
        // Compact mode
        String compact = NToStringBuilder.of("Config")
                .compact()
                .add("port", 8080)
                .add("host", "localhost")
                .toString();
        Assertions.assertEquals("Config{port=8080, host=localhost}", compact);

        // Custom separator and raw values
        String custom = NToStringBuilder.of("Pair")
                .separator("=")
                .quoteStrings(false)
                .add("k1", "v1")
                .add("k2", "v2")
                .toString();
        Assertions.assertEquals("Pair{k1=v1, k2=v2}", custom);

        // Explicit multi-line mode
        String multiline = NToStringBuilder.of("Node")
                .multiLine(true)
                .indentString("  ")
                .add("id", 1)
                .add("label", "Root")
                .toString();
        String expectedMulti = "Node{\n  id : 1,\n  label : \"Root\"\n}";
        Assertions.assertEquals(expectedMulti, multiline);

        // Auto multi-line when rowSize is exceeded
        String longRow = NToStringBuilder.of("LongObject")
                .rowSize(30)
                .add("fieldOne", "very long string value that exceeds row size")
                .add("fieldTwo", "another long value")
                .toString();
        Assertions.assertTrue(longRow.contains("\n"));
        Assertions.assertTrue(longRow.startsWith("LongObject{\n"));
        Assertions.assertTrue(longRow.endsWith("\n}"));
    }

    @Test
    public void testNestedBuilders() {
        NToStringBuilder address = NToStringBuilder.of("Address")
                .add("street", "123 Main St")
                .add("city", "Springfield");

        NToStringBuilder person = NToStringBuilder.of("Person")
                .multiLine(true)
                .add("name", "Homer")
                .add("address", address);

        String out = person.toString();
        String expected = "Person{\n" +
                "    name : \"Homer\",\n" +
                "    address : Address{street : \"123 Main St\", city : \"Springfield\"}\n" +
                "}";
        Assertions.assertEquals(expected, out);

        // Multiline nested inside multiline
        address.multiLine(true);
        String outNestedMulti = person.toString();
        String expectedNestedMulti = "Person{\n" +
                "    name : \"Homer\",\n" +
                "    address : Address{\n" +
                "        street : \"123 Main St\",\n" +
                "        city : \"Springfield\"\n" +
                "    }\n" +
                "}";
        Assertions.assertEquals(expectedNestedMulti, outNestedMulti);
    }

    @Test
    public void testCollectionsMapsAndUtilities() {
        NToStringBuilder b1 = NToStringBuilder.of("A").add("x", 1);
        NToStringBuilder b2 = NToStringBuilder.of("B").add("y", 2).addAll(b1);

        Assertions.assertEquals(2, b2.size());
        Assertions.assertFalse(b2.isEmpty());
        b2.clear();
        Assertions.assertEquals(0, b2.size());
        Assertions.assertTrue(b2.isEmpty());

        Map<String, Object> map = new LinkedHashMap<>();
        map.put("k1", "v1");
        map.put("k2", 42);
        b2.addAll(map);
        Assertions.assertEquals(2, b2.size());
    }

    @Test
    public void testMapWithNewlines() {
        NToStringBuilder r = NToStringBuilder.of("hello").add("a", NMaps.of("a\nb", "a\nb"));
        String a = r.toString();
        Assertions.assertNotNull(a);
        Assertions.assertTrue(a.contains("hello{"));
    }
}
