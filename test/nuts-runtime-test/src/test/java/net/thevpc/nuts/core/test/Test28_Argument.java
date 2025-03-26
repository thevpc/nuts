package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.cmdline.DefaultNArg;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class Test28_Argument {
    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void test01() {
        DefaultNArg a = new DefaultNArg("-a=2");
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("-a", a.getKey().asStringValue().get());
        Assertions.assertEquals("2", a.getStringValue().get());
        Assertions.assertEquals("a", a.getOptionName().asStringValue().get());
        Assertions.assertEquals("-", a.getOptionPrefix().asStringValue().get());
        Assertions.assertEquals("-a=2", a.asStringValue().get());
    }

    @Test
    public void test02() {
        DefaultNArg a = new DefaultNArg("-//a=2");
        Assertions.assertTrue(a.isOption());
        Assertions.assertFalse(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("-a", a.getKey().asStringValue().get());
        Assertions.assertEquals("2", a.getStringValue().get());
        Assertions.assertEquals("a", a.getOptionName().asStringValue().get());
        Assertions.assertEquals("-", a.getOptionPrefix().asStringValue().get());
        Assertions.assertEquals("-//a=2", a.asStringValue().get());
    }

    @Test
    public void test03() {
        DefaultNArg a = new DefaultNArg("-!a=2");
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertTrue(a.isNegated());
        Assertions.assertEquals("-a", a.getKey().asStringValue().get());
        Assertions.assertEquals("2", a.getStringValue().get());
        Assertions.assertEquals("a", a.getOptionName().asStringValue().get());
        Assertions.assertEquals("-", a.getOptionPrefix().asStringValue().get());
        Assertions.assertEquals("-!a=2", a.asStringValue().get());
    }

    @Test
    public void test04() {
        DefaultNArg a = new DefaultNArg("-!a");
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertTrue(a.isNegated());
        Assertions.assertEquals("-a", a.getKey().asStringValue().get());
        Assertions.assertTrue(a.getStringValue().isEmpty());
        Assertions.assertEquals("a", a.getOptionName().asStringValue().get());
        Assertions.assertEquals("-", a.getOptionPrefix().asStringValue().get());
        Assertions.assertEquals("-!a", a.asStringValue().get());
    }

    @Test
    public void test05() {
        DefaultNArg a = new DefaultNArg("-!=a");
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertTrue(a.isNegated());
        Assertions.assertEquals("-", a.getKey().asStringValue().get());
        Assertions.assertEquals("a",a.getStringValue().get());
        Assertions.assertEquals("", a.getOptionName().asStringValue().get());
        Assertions.assertEquals("-", a.getOptionPrefix().asStringValue().get());
        Assertions.assertEquals("-!=a", a.asStringValue().get());
    }

    @Test
    public void test06() {
        DefaultNArg a = new DefaultNArg("-!=");
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertTrue(a.isNegated());
        Assertions.assertEquals("-", a.getKey().asStringValue().get());
        Assertions.assertEquals("",a.getStringValue().get());
        Assertions.assertEquals("", a.getOptionName().asStringValue().get());
        Assertions.assertEquals("-", a.getOptionPrefix().asStringValue().get());
        Assertions.assertEquals("-!=", a.asStringValue().get());
    }

    @Test
    public void test07() {
        DefaultNArg a = new DefaultNArg("-!");
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertTrue(a.isNegated());
        Assertions.assertEquals("-", a.getKey().asStringValue().get());
        Assertions.assertTrue(a.getStringValue().isEmpty());
        Assertions.assertEquals("", a.getOptionName().asStringValue().get());
        Assertions.assertEquals("-", a.getOptionPrefix().asStringValue().get());
        Assertions.assertEquals("-!", a.asStringValue().get());
    }

    @Test
    public void test08() {
        DefaultNArg a = new DefaultNArg("-");
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("-", a.getKey().asStringValue().get());
        Assertions.assertTrue(a.getStringValue().isEmpty());
        Assertions.assertEquals("", a.getOptionName().asStringValue().get());
        Assertions.assertEquals("-", a.getOptionPrefix().asStringValue().get());
        Assertions.assertEquals("-", a.asStringValue().get());
    }

    @Test
    public void test09() {
        DefaultNArg a = new DefaultNArg("");
        Assertions.assertTrue(a.isNonOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("", a.getKey().asStringValue().get());
        Assertions.assertTrue(a.getStringValue().isEmpty());
        Assertions.assertTrue(a.getOptionName().asStringValue().isEmpty());
        Assertions.assertTrue(a.getOptionPrefix().asStringValue().isEmpty());
        Assertions.assertEquals("", a.asStringValue().get());
    }

    @Test
    public void test10() {
        DefaultNArg a = new DefaultNArg("c=/a");
        Assertions.assertTrue(a.isNonOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("c", a.getKey().asStringValue().get());
        Assertions.assertEquals("/a",a.getStringValue().get());
        Assertions.assertTrue(a.getOptionName().asStringValue().isEmpty());
        Assertions.assertTrue(a.getOptionPrefix().asStringValue().isEmpty());
        Assertions.assertEquals("c=/a", a.asStringValue().get());
    }

    @Test
    public void test11() {
        DefaultNArg a = new DefaultNArg("c");
        Assertions.assertTrue(a.isNonOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("c", a.getKey().asStringValue().get());
        Assertions.assertTrue(a.getStringValue().isEmpty());
        Assertions.assertTrue(a.getOptionName().asStringValue().isEmpty());
        Assertions.assertTrue(a.getOptionPrefix().asStringValue().isEmpty());
        Assertions.assertEquals("c", a.asStringValue().get());
    }

    @Test
    public void test12() {
        DefaultNArg a = new DefaultNArg("!//c=30");
        Assertions.assertTrue(a.isNonOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("!//c=30", a.getKey().asStringValue().get());
        Assertions.assertTrue(a.getStringValue().isEmpty());
        Assertions.assertTrue(a.getOptionName().asStringValue().isEmpty());
        Assertions.assertTrue(a.getOptionPrefix().asStringValue().isEmpty());
        Assertions.assertEquals("!//c=30", a.asStringValue().get());
    }

    @Test
    public void test13() {
        DefaultNArg a = new DefaultNArg("!");
        Assertions.assertTrue(a.isNonOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("!", a.getKey().asStringValue().get());
        Assertions.assertTrue(a.getStringValue().isEmpty());
        Assertions.assertTrue(a.getOptionName().asStringValue().isEmpty());
        Assertions.assertTrue(a.getOptionPrefix().asStringValue().isEmpty());
        Assertions.assertEquals("!", a.asStringValue().get());
    }

    @Test
    public void test14() {
        DefaultNArg a = new DefaultNArg("");
        Assertions.assertTrue(a.isNonOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("", a.getKey().asStringValue().get());
        Assertions.assertTrue(a.getStringValue().isEmpty());
        Assertions.assertTrue(a.getOptionName().asStringValue().isEmpty());
        Assertions.assertTrue(a.getOptionPrefix().asStringValue().isEmpty());
        Assertions.assertEquals("", a.asStringValue().get());
    }
}
