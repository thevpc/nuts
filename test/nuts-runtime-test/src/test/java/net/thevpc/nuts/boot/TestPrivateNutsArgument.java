package net.thevpc.nuts.boot;

import net.thevpc.nuts.NutsArgument;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestPrivateNutsArgument {

    @Test
    public void test01() {
        NutsArgument a = NutsArgument.of("-a=2");
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("-a", a.getKey().asString().get());
        Assertions.assertEquals("2", a.getStringValue().get());
        Assertions.assertEquals("a", a.getOptionName().asString().get());
        Assertions.assertEquals("-", a.getOptionPrefix().asString().get());
        Assertions.assertEquals("-a=2", a.asString());
    }

    @Test
    public void test02() {
        NutsArgument a = NutsArgument.of("-//a=2");
        Assertions.assertTrue(a.isOption());
        Assertions.assertFalse(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("-a", a.getKey().asString().get());
        Assertions.assertEquals("2", a.getStringValue().get());
        Assertions.assertEquals("a", a.getOptionName().asString().get());
        Assertions.assertEquals("-", a.getOptionPrefix().asString().get());
        Assertions.assertEquals("-//a=2", a.asString());
    }

    @Test
    public void test03() {
        NutsArgument a = NutsArgument.of("-!a=2");
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertTrue(a.isNegated());
        Assertions.assertEquals("-a", a.getKey().asString().get());
        Assertions.assertEquals("2", a.getStringValue().get());
        Assertions.assertEquals("a", a.getOptionName().asString().get());
        Assertions.assertEquals("-", a.getOptionPrefix().asString().get());
        Assertions.assertEquals("-!a=2", a.asString());
    }

    @Test
    public void test04() {
        NutsArgument a = NutsArgument.of("-!a");
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertTrue(a.isNegated());
        Assertions.assertEquals("-a", a.getKey().asString().get());
        Assertions.assertNull(a.getStringValue().orNull());
        Assertions.assertEquals("a", a.getOptionName().asString().get());
        Assertions.assertEquals("-", a.getOptionPrefix().asString().get());
        Assertions.assertEquals("-!a", a.asString());
    }

    @Test
    public void test05() {
        NutsArgument a = NutsArgument.of("-!=a");
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertTrue(a.isNegated());
        Assertions.assertEquals("-", a.getKey().asString().get());
        Assertions.assertEquals("a",a.getStringValue().get());
        Assertions.assertEquals("", a.getOptionName().asString().get());
        Assertions.assertEquals("-", a.getOptionPrefix().asString().get());
        Assertions.assertEquals("-!=a", a.asString());
    }

    @Test
    public void test06() {
        NutsArgument a = NutsArgument.of("-!=");
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertTrue(a.isNegated());
        Assertions.assertEquals("-", a.getKey().asString().get());
        Assertions.assertEquals("",a.getStringValue().get());
        Assertions.assertEquals("", a.getOptionName().asString().get());
        Assertions.assertEquals("-", a.getOptionPrefix().asString().get());
        Assertions.assertEquals("-!=", a.asString());
    }

    @Test
    public void test07() {
        NutsArgument a = NutsArgument.of("-!");
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertTrue(a.isNegated());
        Assertions.assertEquals("-", a.getKey().asString().get());
        Assertions.assertNull(a.getStringValue().orNull());
        Assertions.assertEquals("", a.getOptionName().asString().get());
        Assertions.assertEquals("-", a.getOptionPrefix().asString().get());
        Assertions.assertEquals("-!", a.asString());
    }

    @Test
    public void test08() {
        NutsArgument a = NutsArgument.of("-");
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("-", a.getKey().asString().get());
        Assertions.assertNull(a.getStringValue().orNull());
        Assertions.assertEquals("", a.getOptionName().asString().get());
        Assertions.assertEquals("-", a.getOptionPrefix().asString().get());
        Assertions.assertEquals("-", a.asString());
    }

    @Test
    public void test09() {
        NutsArgument a = NutsArgument.of("");
        Assertions.assertTrue(!a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("", a.getKey().asString().get());
        Assertions.assertNull(a.getStringValue().orNull());
        Assertions.assertNull(a.getOptionName().asString().orNull());
        Assertions.assertNull(a.getOptionPrefix().asString().orNull());
        Assertions.assertEquals("", a.asString());
    }

    @Test
    public void test10() {
        NutsArgument a = NutsArgument.of("c=/a");
        Assertions.assertTrue(!a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("c", a.getKey().asString().get());
        Assertions.assertEquals("/a",a.getStringValue().get());
        Assertions.assertNull(a.getOptionName().asString().orNull());
        Assertions.assertNull(a.getOptionPrefix().asString().orNull());
        Assertions.assertEquals("c=/a", a.asString());
    }

    @Test
    public void test11() {
        NutsArgument a = NutsArgument.of("c");
        Assertions.assertTrue(!a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("c", a.getKey().asString().get());
        Assertions.assertNull(a.getStringValue().orNull());
        Assertions.assertNull(a.getOptionName().asString().orNull());
        Assertions.assertNull(a.getOptionPrefix().asString().orNull());
        Assertions.assertEquals("c", a.asString());
    }

    @Test
    public void test12() {
        NutsArgument a = NutsArgument.of("!//c=30");
        Assertions.assertTrue(!a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("!//c=30", a.getKey().asString().get());
        Assertions.assertNull(a.getStringValue().orNull());
        Assertions.assertNull(a.getOptionName().asString().orNull());
        Assertions.assertNull(a.getOptionPrefix().asString().orNull());
        Assertions.assertEquals("!//c=30", a.asString());
    }

    @Test
    public void test13() {
        NutsArgument a = NutsArgument.of("!");
        Assertions.assertTrue(!a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("!", a.getKey().asString().get());
        Assertions.assertNull(a.getStringValue().orNull());
        Assertions.assertNull(a.getOptionName().asString().orNull());
        Assertions.assertNull(a.getOptionPrefix().asString().orNull());
        Assertions.assertEquals("!", a.asString());
    }

    @Test
    public void test14() {
        NutsArgument a = NutsArgument.of("");
        Assertions.assertTrue(!a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("", a.getKey().asString().get());
        Assertions.assertNull(a.getStringValue().orNull());
        Assertions.assertNull(a.getOptionName().asString().orNull());
        Assertions.assertNull(a.getOptionPrefix().asString().orNull());
        Assertions.assertEquals("", a.asString());
    }
}
