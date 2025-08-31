package net.thevpc.nuts.core.test;

import net.thevpc.nuts.cmdline.NArg;
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
        NArg a = NArg.of("-a=2");
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isUncommented());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("-a", a.getKey().asString().get());
        Assertions.assertEquals("2", a.getStringValue().get());
        Assertions.assertEquals("a", a.getOptionName().asString().get());
        Assertions.assertEquals("-", a.getOptionPrefix().asString().get());
        Assertions.assertEquals("-a=2", a.asString().get());
    }

    @Test
    public void test02() {
        NArg a = NArg.of("-//a=2");
        Assertions.assertTrue(a.isOption());
        Assertions.assertFalse(a.isUncommented());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("-a", a.getKey().asString().get());
        Assertions.assertEquals("2", a.getStringValue().get());
        Assertions.assertEquals("a", a.getOptionName().asString().get());
        Assertions.assertEquals("-", a.getOptionPrefix().asString().get());
        Assertions.assertEquals("-//a=2", a.asString().get());
    }

    @Test
    public void test03() {
        NArg a = NArg.of("-!a=2");
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isUncommented());
        Assertions.assertTrue(a.isNegated());
        Assertions.assertEquals("-a", a.getKey().asString().get());
        Assertions.assertEquals("2", a.getStringValue().get());
        Assertions.assertEquals("a", a.getOptionName().asString().get());
        Assertions.assertEquals("-", a.getOptionPrefix().asString().get());
        Assertions.assertEquals("-!a=2", a.asString().get());
    }

    @Test
    public void test04() {
        NArg a = NArg.of("-!a");
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isUncommented());
        Assertions.assertTrue(a.isNegated());
        Assertions.assertEquals("-a", a.getKey().asString().get());
        Assertions.assertTrue(a.getStringValue().isEmpty());
        Assertions.assertEquals("a", a.getOptionName().asString().get());
        Assertions.assertEquals("-", a.getOptionPrefix().asString().get());
        Assertions.assertEquals("-!a", a.asString().get());
    }

    @Test
    public void test05() {
        NArg a = NArg.of("-!=a");
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isUncommented());
        Assertions.assertTrue(a.isNegated());
        Assertions.assertEquals("-", a.getKey().asString().get());
        Assertions.assertEquals("a",a.getStringValue().get());
        Assertions.assertEquals("", a.getOptionName().asString().get());
        Assertions.assertEquals("-", a.getOptionPrefix().asString().get());
        Assertions.assertEquals("-!=a", a.asString().get());
    }

    @Test
    public void test06() {
        NArg a = NArg.of("-!=");
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isUncommented());
        Assertions.assertTrue(a.isNegated());
        Assertions.assertEquals("-", a.getKey().asString().get());
        Assertions.assertEquals("",a.getStringValue().get());
        Assertions.assertEquals("", a.getOptionName().asString().get());
        Assertions.assertEquals("-", a.getOptionPrefix().asString().get());
        Assertions.assertEquals("-!=", a.asString().get());
    }

    @Test
    public void test07() {
        NArg a = NArg.of("-!");
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isUncommented());
        Assertions.assertTrue(a.isNegated());
        Assertions.assertEquals("-", a.getKey().asString().get());
        Assertions.assertTrue(a.getStringValue().isEmpty());
        Assertions.assertEquals("", a.getOptionName().asString().get());
        Assertions.assertEquals("-", a.getOptionPrefix().asString().get());
        Assertions.assertEquals("-!", a.asString().get());
    }

    @Test
    public void test08() {
        NArg a = NArg.of("-");
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isUncommented());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("-", a.getKey().asString().get());
        Assertions.assertTrue(a.getStringValue().isEmpty());
        Assertions.assertEquals("", a.getOptionName().asString().get());
        Assertions.assertEquals("-", a.getOptionPrefix().asString().get());
        Assertions.assertEquals("-", a.asString().get());
    }

    @Test
    public void test09() {
        NArg a = NArg.of("");
        Assertions.assertTrue(a.isNonOption());
        Assertions.assertTrue(a.isUncommented());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("", a.getKey().asString().get());
        Assertions.assertTrue(a.getStringValue().isEmpty());
        Assertions.assertTrue(a.getOptionName().asString().isEmpty());
        Assertions.assertTrue(a.getOptionPrefix().asString().isEmpty());
        Assertions.assertEquals("", a.asString().get());
    }

    @Test
    public void test10() {
        NArg a = NArg.of("c=/a");
        Assertions.assertTrue(a.isNonOption());
        Assertions.assertTrue(a.isUncommented());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("c", a.getKey().asString().get());
        Assertions.assertEquals("/a",a.getStringValue().get());
        Assertions.assertTrue(a.getOptionName().asString().isEmpty());
        Assertions.assertTrue(a.getOptionPrefix().asString().isEmpty());
        Assertions.assertEquals("c=/a", a.asString().get());
    }

    @Test
    public void test11() {
        NArg a = NArg.of("c");
        Assertions.assertTrue(a.isNonOption());
        Assertions.assertTrue(a.isUncommented());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("c", a.getKey().asString().get());
        Assertions.assertTrue(a.getStringValue().isEmpty());
        Assertions.assertTrue(a.getOptionName().asString().isEmpty());
        Assertions.assertTrue(a.getOptionPrefix().asString().isEmpty());
        Assertions.assertEquals("c", a.asString().get());
    }

    @Test
    public void test12() {
        NArg a = NArg.of("!//c=30");
        Assertions.assertTrue(a.isNonOption());
        Assertions.assertTrue(a.isUncommented());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("!//c=30", a.getKey().asString().get());
        Assertions.assertTrue(a.getStringValue().isEmpty());
        Assertions.assertTrue(a.getOptionName().asString().isEmpty());
        Assertions.assertTrue(a.getOptionPrefix().asString().isEmpty());
        Assertions.assertEquals("!//c=30", a.asString().get());
    }

    @Test
    public void test13() {
        NArg a = NArg.of("!");
        Assertions.assertTrue(a.isNonOption());
        Assertions.assertTrue(a.isUncommented());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("!", a.getKey().asString().get());
        Assertions.assertTrue(a.getStringValue().isEmpty());
        Assertions.assertTrue(a.getOptionName().asString().isEmpty());
        Assertions.assertTrue(a.getOptionPrefix().asString().isEmpty());
        Assertions.assertEquals("!", a.asString().get());
    }

    @Test
    public void test14() {
        NArg a = NArg.of("");
        Assertions.assertTrue(a.isNonOption());
        Assertions.assertTrue(a.isUncommented());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("", a.getKey().asString().get());
        Assertions.assertTrue(a.getStringValue().isEmpty());
        Assertions.assertTrue(a.getOptionName().asString().isEmpty());
        Assertions.assertTrue(a.getOptionPrefix().asString().isEmpty());
        Assertions.assertEquals("", a.asString().get());
    }
}
