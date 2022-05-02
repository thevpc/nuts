package net.thevpc.nuts.core.test;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.cmdline.DefaultNutsArgument;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class Test28_Argument {
    static NutsSession session;

    @BeforeAll
    public static void init() {
        session = TestUtils.openNewMinTestWorkspace();
    }

    @Test
    public void test01() {
        DefaultNutsArgument a = new DefaultNutsArgument("-a=2");
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("-a", a.getKey().asString().get());
        Assertions.assertEquals("2", a.getStringValue().get());
        Assertions.assertEquals("a", a.getOptionName().asString().get());
        Assertions.assertEquals("-", a.getOptionPrefix().asString().get());
        Assertions.assertEquals("-a=2", a.asString().get());
    }

    @Test
    public void test02() {
        DefaultNutsArgument a = new DefaultNutsArgument("-//a=2");
        Assertions.assertTrue(a.isOption());
        Assertions.assertFalse(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("-a", a.getKey().asString().get());
        Assertions.assertEquals("2", a.getStringValue().get());
        Assertions.assertEquals("a", a.getOptionName().asString().get());
        Assertions.assertEquals("-", a.getOptionPrefix().asString().get());
        Assertions.assertEquals("-//a=2", a.asString().get());
    }

    @Test
    public void test03() {
        DefaultNutsArgument a = new DefaultNutsArgument("-!a=2");
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertTrue(a.isNegated());
        Assertions.assertEquals("-a", a.getKey().asString().get());
        Assertions.assertEquals("2", a.getStringValue().get());
        Assertions.assertEquals("a", a.getOptionName().asString().get());
        Assertions.assertEquals("-", a.getOptionPrefix().asString().get());
        Assertions.assertEquals("-!a=2", a.asString().get());
    }

    @Test
    public void test04() {
        DefaultNutsArgument a = new DefaultNutsArgument("-!a");
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertTrue(a.isNegated());
        Assertions.assertEquals("-a", a.getKey().asString().get());
        Assertions.assertTrue(a.getStringValue().isEmpty());
        Assertions.assertEquals("a", a.getOptionName().asString().get());
        Assertions.assertEquals("-", a.getOptionPrefix().asString().get());
        Assertions.assertEquals("-!a", a.asString().get());
    }

    @Test
    public void test05() {
        DefaultNutsArgument a = new DefaultNutsArgument("-!=a");
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertTrue(a.isNegated());
        Assertions.assertEquals("-", a.getKey().asString().get());
        Assertions.assertEquals("a",a.getStringValue().get());
        Assertions.assertEquals("", a.getOptionName().asString().get());
        Assertions.assertEquals("-", a.getOptionPrefix().asString().get());
        Assertions.assertEquals("-!=a", a.asString().get());
    }

    @Test
    public void test06() {
        NutsSession  session= TestUtils.openNewMinTestWorkspace();
        DefaultNutsArgument a = new DefaultNutsArgument("-!=");
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertTrue(a.isNegated());
        Assertions.assertEquals("-", a.getKey().asString().get());
        Assertions.assertEquals("",a.getStringValue().get());
        Assertions.assertEquals("", a.getOptionName().asString().get());
        Assertions.assertEquals("-", a.getOptionPrefix().asString().get());
        Assertions.assertEquals("-!=", a.asString().get());
    }

    @Test
    public void test07() {
        DefaultNutsArgument a = new DefaultNutsArgument("-!");
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertTrue(a.isNegated());
        Assertions.assertEquals("-", a.getKey().asString().get());
        Assertions.assertTrue(a.getStringValue().isEmpty());
        Assertions.assertEquals("", a.getOptionName().asString().get());
        Assertions.assertEquals("-", a.getOptionPrefix().asString().get());
        Assertions.assertEquals("-!", a.asString().get());
    }

    @Test
    public void test08() {
        DefaultNutsArgument a = new DefaultNutsArgument("-");
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("-", a.getKey().asString().get());
        Assertions.assertTrue(a.getStringValue().isEmpty());
        Assertions.assertEquals("", a.getOptionName().asString().get());
        Assertions.assertEquals("-", a.getOptionPrefix().asString().get());
        Assertions.assertEquals("-", a.asString().get());
    }

    @Test
    public void test09() {
        DefaultNutsArgument a = new DefaultNutsArgument("");
        Assertions.assertTrue(a.isNonOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("", a.getKey().asString().get());
        Assertions.assertTrue(a.getStringValue().isEmpty());
        Assertions.assertTrue(a.getOptionName().asString().isEmpty());
        Assertions.assertTrue(a.getOptionPrefix().asString().isEmpty());
        Assertions.assertEquals("", a.asString().get());
    }

    @Test
    public void test10() {
        DefaultNutsArgument a = new DefaultNutsArgument("c=/a");
        Assertions.assertTrue(a.isNonOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("c", a.getKey().asString().get());
        Assertions.assertEquals("/a",a.getStringValue().get());
        Assertions.assertTrue(a.getOptionName().asString().isEmpty());
        Assertions.assertTrue(a.getOptionPrefix().asString().isEmpty());
        Assertions.assertEquals("c=/a", a.asString().get());
    }

    @Test
    public void test11() {
        DefaultNutsArgument a = new DefaultNutsArgument("c");
        Assertions.assertTrue(a.isNonOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("c", a.getKey().asString().get());
        Assertions.assertTrue(a.getStringValue().isEmpty());
        Assertions.assertTrue(a.getOptionName().asString().isEmpty());
        Assertions.assertTrue(a.getOptionPrefix().asString().isEmpty());
        Assertions.assertEquals("c", a.asString().get());
    }

    @Test
    public void test12() {
        DefaultNutsArgument a = new DefaultNutsArgument("!//c=30");
        Assertions.assertTrue(a.isNonOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("!//c=30", a.getKey().asString().get());
        Assertions.assertTrue(a.getStringValue().isEmpty());
        Assertions.assertTrue(a.getOptionName().asString().isEmpty());
        Assertions.assertTrue(a.getOptionPrefix().asString().isEmpty());
        Assertions.assertEquals("!//c=30", a.asString().get());
    }

    @Test
    public void test13() {
        DefaultNutsArgument a = new DefaultNutsArgument("!");
        Assertions.assertTrue(a.isNonOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("!", a.getKey().asString().get());
        Assertions.assertTrue(a.getStringValue().isEmpty());
        Assertions.assertTrue(a.getOptionName().asString().isEmpty());
        Assertions.assertTrue(a.getOptionPrefix().asString().isEmpty());
        Assertions.assertEquals("!", a.asString().get());
    }

    @Test
    public void test14() {
        DefaultNutsArgument a = new DefaultNutsArgument("");
        Assertions.assertTrue(a.isNonOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("", a.getKey().asString().get());
        Assertions.assertTrue(a.getStringValue().isEmpty());
        Assertions.assertTrue(a.getOptionName().asString().isEmpty());
        Assertions.assertTrue(a.getOptionPrefix().asString().isEmpty());
        Assertions.assertEquals("", a.asString().get());
    }
}
