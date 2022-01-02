package net.thevpc.nuts.boot;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class TestPrivateNutsArgument {

    @Test
    public void test01() {
        PrivateNutsArgument a = new PrivateNutsArgument("-a=2", '=');
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("-a", a.getKey().getString());
        Assertions.assertEquals("2", a.getValue().getString());
        Assertions.assertEquals("a", a.getOptionName().getString());
        Assertions.assertEquals("-", a.getOptionPrefix().getString());
        Assertions.assertEquals("-a=2", a.getString());
    }

    @Test
    public void test02() {
        PrivateNutsArgument a = new PrivateNutsArgument("-//a=2", '=');
        Assertions.assertTrue(a.isOption());
        Assertions.assertFalse(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("-a", a.getKey().getString());
        Assertions.assertEquals("2", a.getValue().getString());
        Assertions.assertEquals("a", a.getOptionName().getString());
        Assertions.assertEquals("-", a.getOptionPrefix().getString());
        Assertions.assertEquals("-//a=2", a.getString());
    }

    @Test
    public void test03() {
        PrivateNutsArgument a = new PrivateNutsArgument("-!a=2", '=');
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertTrue(a.isNegated());
        Assertions.assertEquals("-a", a.getKey().getString());
        Assertions.assertEquals("2", a.getValue().getString());
        Assertions.assertEquals("a", a.getOptionName().getString());
        Assertions.assertEquals("-", a.getOptionPrefix().getString());
        Assertions.assertEquals("-!a=2", a.getString());
    }

    @Test
    public void test04() {
        PrivateNutsArgument a = new PrivateNutsArgument("-!a", '=');
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertTrue(a.isNegated());
        Assertions.assertEquals("-a", a.getKey().getString());
        Assertions.assertNull(a.getValue().getString());
        Assertions.assertEquals("a", a.getOptionName().getString());
        Assertions.assertEquals("-", a.getOptionPrefix().getString());
        Assertions.assertEquals("-!a", a.getString());
    }

    @Test
    public void test05() {
        PrivateNutsArgument a = new PrivateNutsArgument("-!=a", '=');
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertTrue(a.isNegated());
        Assertions.assertEquals("-", a.getKey().getString());
        Assertions.assertEquals("a",a.getValue().getString());
        Assertions.assertEquals("", a.getOptionName().getString());
        Assertions.assertEquals("-", a.getOptionPrefix().getString());
        Assertions.assertEquals("-!=a", a.getString());
    }

    @Test
    public void test06() {
        PrivateNutsArgument a = new PrivateNutsArgument("-!=", '=');
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertTrue(a.isNegated());
        Assertions.assertEquals("-", a.getKey().getString());
        Assertions.assertEquals("",a.getValue().getString());
        Assertions.assertEquals("", a.getOptionName().getString());
        Assertions.assertEquals("-", a.getOptionPrefix().getString());
        Assertions.assertEquals("-!=", a.getString());
    }

    @Test
    public void test07() {
        PrivateNutsArgument a = new PrivateNutsArgument("-!", '=');
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertTrue(a.isNegated());
        Assertions.assertEquals("-", a.getKey().getString());
        Assertions.assertNull(a.getValue().getString());
        Assertions.assertEquals("", a.getOptionName().getString());
        Assertions.assertEquals("-", a.getOptionPrefix().getString());
        Assertions.assertEquals("-!", a.getString());
    }

    @Test
    public void test08() {
        PrivateNutsArgument a = new PrivateNutsArgument("-", '=');
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("-", a.getKey().getString());
        Assertions.assertNull(a.getValue().getString());
        Assertions.assertEquals("", a.getOptionName().getString());
        Assertions.assertEquals("-", a.getOptionPrefix().getString());
        Assertions.assertEquals("-", a.getString());
    }

    @Test
    public void test09() {
        PrivateNutsArgument a = new PrivateNutsArgument("", '=');
        Assertions.assertTrue(!a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("", a.getKey().getString());
        Assertions.assertNull(a.getValue().getString());
        Assertions.assertNull(a.getOptionName().getString());
        Assertions.assertNull(a.getOptionPrefix().getString());
        Assertions.assertEquals("", a.getString());
    }

    @Test
    public void test10() {
        PrivateNutsArgument a = new PrivateNutsArgument("c=/a", '=');
        Assertions.assertTrue(!a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("c", a.getKey().getString());
        Assertions.assertEquals("/a",a.getValue().getString());
        Assertions.assertNull(a.getOptionName().getString());
        Assertions.assertNull(a.getOptionPrefix().getString());
        Assertions.assertEquals("c=/a", a.getString());
    }

    @Test
    public void test11() {
        PrivateNutsArgument a = new PrivateNutsArgument("c", '=');
        Assertions.assertTrue(!a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("c", a.getKey().getString());
        Assertions.assertNull(a.getValue().getString());
        Assertions.assertNull(a.getOptionName().getString());
        Assertions.assertNull(a.getOptionPrefix().getString());
        Assertions.assertEquals("c", a.getString());
    }

    @Test
    public void test12() {
        PrivateNutsArgument a = new PrivateNutsArgument("!//c=30", '=');
        Assertions.assertTrue(!a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("!//c=30", a.getKey().getString());
        Assertions.assertNull(a.getValue().getString());
        Assertions.assertNull(a.getOptionName().getString());
        Assertions.assertNull(a.getOptionPrefix().getString());
        Assertions.assertEquals("!//c=30", a.getString());
    }

    @Test
    public void test13() {
        PrivateNutsArgument a = new PrivateNutsArgument("!", '=');
        Assertions.assertTrue(!a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("!", a.getKey().getString());
        Assertions.assertNull(a.getValue().getString());
        Assertions.assertNull(a.getOptionName().getString());
        Assertions.assertNull(a.getOptionPrefix().getString());
        Assertions.assertEquals("!", a.getString());
    }

    @Test
    public void test14() {
        PrivateNutsArgument a = new PrivateNutsArgument("", '=');
        Assertions.assertTrue(!a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("", a.getKey().getString());
        Assertions.assertNull(a.getValue().getString());
        Assertions.assertNull(a.getOptionName().getString());
        Assertions.assertNull(a.getOptionPrefix().getString());
        Assertions.assertEquals("", a.getString());
    }
}
