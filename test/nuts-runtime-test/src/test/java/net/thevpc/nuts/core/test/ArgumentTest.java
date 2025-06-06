package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.cmdline.DefaultNArg;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class ArgumentTest {
    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void test01() {
        DefaultNArg a = new DefaultNArg("-a=2");
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isNonCommented());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("-a", a.getKey().asString().get());
        Assertions.assertEquals("2", a.getStringValue().get());
        Assertions.assertEquals("a", a.getOptionName().asString().get());
        Assertions.assertEquals("-", a.getOptionPrefix().asString().get());
        Assertions.assertEquals("-a=2", a.asString().get());
    }

    @Test
    public void test02() {
        DefaultNArg a = new DefaultNArg("-//a=2");
        Assertions.assertTrue(a.isOption());
        Assertions.assertFalse(a.isNonCommented());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("-a", a.getKey().asString().get());
        Assertions.assertEquals("2", a.getStringValue().get());
        Assertions.assertEquals("a", a.getOptionName().asString().get());
        Assertions.assertEquals("-", a.getOptionPrefix().asString().get());
        Assertions.assertEquals("-//a=2", a.asString().get());
    }

    @Test
    public void test03() {
        DefaultNArg a = new DefaultNArg("-!a=2");
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isNonCommented());
        Assertions.assertTrue(a.isNegated());
        Assertions.assertEquals("-a", a.getKey().asString().get());
        Assertions.assertEquals("2", a.getStringValue().get());
        Assertions.assertEquals("a", a.getOptionName().asString().get());
        Assertions.assertEquals("-", a.getOptionPrefix().asString().get());
        Assertions.assertEquals("-!a=2", a.asString().get());
    }

    @Test
    public void test04() {
        DefaultNArg a = new DefaultNArg("-!a");
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isNonCommented());
        Assertions.assertTrue(a.isNegated());
        Assertions.assertEquals("-a", a.getKey().asString().get());
        Assertions.assertTrue(a.getStringValue().isEmpty());
        Assertions.assertEquals("a", a.getOptionName().asString().get());
        Assertions.assertEquals("-", a.getOptionPrefix().asString().get());
        Assertions.assertEquals("-!a", a.asString().get());
    }

    @Test
    public void test05() {
        DefaultNArg a = new DefaultNArg("-!=a");
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isNonCommented());
        Assertions.assertTrue(a.isNegated());
        Assertions.assertEquals("-", a.getKey().asString().get());
        Assertions.assertEquals("a",a.getStringValue().get());
        Assertions.assertEquals("", a.getOptionName().asString().get());
        Assertions.assertEquals("-", a.getOptionPrefix().asString().get());
        Assertions.assertEquals("-!=a", a.asString().get());
    }

    @Test
    public void test06() {
        DefaultNArg a = new DefaultNArg("-!=");
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isNonCommented());
        Assertions.assertTrue(a.isNegated());
        Assertions.assertEquals("-", a.getKey().asString().get());
        Assertions.assertEquals("",a.getStringValue().get());
        Assertions.assertEquals("", a.getOptionName().asString().get());
        Assertions.assertEquals("-", a.getOptionPrefix().asString().get());
        Assertions.assertEquals("-!=", a.asString().get());
    }

    @Test
    public void test07() {
        DefaultNArg a = new DefaultNArg("-!");
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isNonCommented());
        Assertions.assertTrue(a.isNegated());
        Assertions.assertEquals("-", a.getKey().asString().get());
        Assertions.assertTrue(a.getStringValue().isEmpty());
        Assertions.assertEquals("", a.getOptionName().asString().get());
        Assertions.assertEquals("-", a.getOptionPrefix().asString().get());
        Assertions.assertEquals("-!", a.asString().get());
    }

    @Test
    public void test08() {
        DefaultNArg a = new DefaultNArg("-");
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isNonCommented());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("-", a.getKey().asString().get());
        Assertions.assertTrue(a.getStringValue().isEmpty());
        Assertions.assertEquals("", a.getOptionName().asString().get());
        Assertions.assertEquals("-", a.getOptionPrefix().asString().get());
        Assertions.assertEquals("-", a.asString().get());
    }

    @Test
    public void test09() {
        DefaultNArg a = new DefaultNArg("");
        Assertions.assertTrue(a.isNonOption());
        Assertions.assertTrue(a.isNonCommented());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("", a.getKey().asString().get());
        Assertions.assertTrue(a.getStringValue().isEmpty());
        Assertions.assertTrue(a.getOptionName().asString().isEmpty());
        Assertions.assertTrue(a.getOptionPrefix().asString().isEmpty());
        Assertions.assertEquals("", a.asString().get());
    }

    @Test
    public void test10() {
        DefaultNArg a = new DefaultNArg("c=/a");
        Assertions.assertTrue(a.isNonOption());
        Assertions.assertTrue(a.isNonCommented());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("c", a.getKey().asString().get());
        Assertions.assertEquals("/a",a.getStringValue().get());
        Assertions.assertTrue(a.getOptionName().asString().isEmpty());
        Assertions.assertTrue(a.getOptionPrefix().asString().isEmpty());
        Assertions.assertEquals("c=/a", a.asString().get());
    }

    @Test
    public void test11() {
        DefaultNArg a = new DefaultNArg("c");
        Assertions.assertTrue(a.isNonOption());
        Assertions.assertTrue(a.isNonCommented());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("c", a.getKey().asString().get());
        Assertions.assertTrue(a.getStringValue().isEmpty());
        Assertions.assertTrue(a.getOptionName().asString().isEmpty());
        Assertions.assertTrue(a.getOptionPrefix().asString().isEmpty());
        Assertions.assertEquals("c", a.asString().get());
    }

    @Test
    public void test12() {
        DefaultNArg a = new DefaultNArg("!//c=30");
        Assertions.assertTrue(a.isNonOption());
        Assertions.assertTrue(a.isNonCommented());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("!//c=30", a.getKey().asString().get());
        Assertions.assertTrue(a.getStringValue().isEmpty());
        Assertions.assertTrue(a.getOptionName().asString().isEmpty());
        Assertions.assertTrue(a.getOptionPrefix().asString().isEmpty());
        Assertions.assertEquals("!//c=30", a.asString().get());
    }

    @Test
    public void test13() {
        DefaultNArg a = new DefaultNArg("!");
        Assertions.assertTrue(a.isNonOption());
        Assertions.assertTrue(a.isNonCommented());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("!", a.getKey().asString().get());
        Assertions.assertTrue(a.getStringValue().isEmpty());
        Assertions.assertTrue(a.getOptionName().asString().isEmpty());
        Assertions.assertTrue(a.getOptionPrefix().asString().isEmpty());
        Assertions.assertEquals("!", a.asString().get());
    }

    @Test
    public void test14() {
        DefaultNArg a = new DefaultNArg("");
        Assertions.assertTrue(a.isNonOption());
        Assertions.assertTrue(a.isNonCommented());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("", a.getKey().asString().get());
        Assertions.assertTrue(a.getStringValue().isEmpty());
        Assertions.assertTrue(a.getOptionName().asString().isEmpty());
        Assertions.assertTrue(a.getOptionPrefix().asString().isEmpty());
        Assertions.assertEquals("", a.asString().get());
    }
}
