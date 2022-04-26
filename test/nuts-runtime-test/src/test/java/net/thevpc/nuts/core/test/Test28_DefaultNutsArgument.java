package net.thevpc.nuts.core.test;

import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.DefaultNutsArgument;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class Test28_DefaultNutsArgument {
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
        Assertions.assertEquals("-a", a.getKey().asString());
        Assertions.assertEquals("2", a.getStringValue());
        Assertions.assertEquals("a", a.getOptionName().asString());
        Assertions.assertEquals("-", a.getOptionPrefix().asString());
        Assertions.assertEquals("-a=2", a.asString());
    }

    @Test
    public void test02() {
        DefaultNutsArgument a = new DefaultNutsArgument("-//a=2");
        Assertions.assertTrue(a.isOption());
        Assertions.assertFalse(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("-a", a.getKey().asString());
        Assertions.assertEquals("2", a.getStringValue());
        Assertions.assertEquals("a", a.getOptionName().asString());
        Assertions.assertEquals("-", a.getOptionPrefix().asString());
        Assertions.assertEquals("-//a=2", a.asString());
    }

    @Test
    public void test03() {
        DefaultNutsArgument a = new DefaultNutsArgument("-!a=2");
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertTrue(a.isNegated());
        Assertions.assertEquals("-a", a.getKey().asString());
        Assertions.assertEquals("2", a.getStringValue());
        Assertions.assertEquals("a", a.getOptionName().asString());
        Assertions.assertEquals("-", a.getOptionPrefix().asString());
        Assertions.assertEquals("-!a=2", a.asString());
    }

    @Test
    public void test04() {
        DefaultNutsArgument a = new DefaultNutsArgument("-!a");
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertTrue(a.isNegated());
        Assertions.assertEquals("-a", a.getKey().asString());
        Assertions.assertNull(a.getStringValue());
        Assertions.assertEquals("a", a.getOptionName().asString());
        Assertions.assertEquals("-", a.getOptionPrefix().asString());
        Assertions.assertEquals("-!a", a.asString());
    }

    @Test
    public void test05() {
        DefaultNutsArgument a = new DefaultNutsArgument("-!=a");
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertTrue(a.isNegated());
        Assertions.assertEquals("-", a.getKey().asString());
        Assertions.assertEquals("a",a.getStringValue());
        Assertions.assertEquals("", a.getOptionName().asString());
        Assertions.assertEquals("-", a.getOptionPrefix().asString());
        Assertions.assertEquals("-!=a", a.asString());
    }

    @Test
    public void test06() {
        NutsSession  session= TestUtils.openNewMinTestWorkspace();
        DefaultNutsArgument a = new DefaultNutsArgument("-!=");
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertTrue(a.isNegated());
        Assertions.assertEquals("-", a.getKey().asString());
        Assertions.assertEquals("",a.getStringValue());
        Assertions.assertEquals("", a.getOptionName().asString());
        Assertions.assertEquals("-", a.getOptionPrefix().asString());
        Assertions.assertEquals("-!=", a.asString());
    }

    @Test
    public void test07() {
        DefaultNutsArgument a = new DefaultNutsArgument("-!");
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertTrue(a.isNegated());
        Assertions.assertEquals("-", a.getKey().asString());
        Assertions.assertNull(a.getStringValue());
        Assertions.assertEquals("", a.getOptionName().asString());
        Assertions.assertEquals("-", a.getOptionPrefix().asString());
        Assertions.assertEquals("-!", a.asString());
    }

    @Test
    public void test08() {
        DefaultNutsArgument a = new DefaultNutsArgument("-");
        Assertions.assertTrue(a.isOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("-", a.getKey().asString());
        Assertions.assertNull(a.getStringValue());
        Assertions.assertEquals("", a.getOptionName().asString());
        Assertions.assertEquals("-", a.getOptionPrefix().asString());
        Assertions.assertEquals("-", a.asString());
    }

    @Test
    public void test09() {
        DefaultNutsArgument a = new DefaultNutsArgument("");
        Assertions.assertTrue(a.isNonOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("", a.getKey().asString());
        Assertions.assertNull(a.getStringValue());
        Assertions.assertNull(a.getOptionName().asString());
        Assertions.assertNull(a.getOptionPrefix().asString());
        Assertions.assertEquals("", a.asString());
    }

    @Test
    public void test10() {
        DefaultNutsArgument a = new DefaultNutsArgument("c=/a");
        Assertions.assertTrue(a.isNonOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("c", a.getKey().asString());
        Assertions.assertEquals("/a",a.getStringValue());
        Assertions.assertNull(a.getOptionName().asString());
        Assertions.assertNull(a.getOptionPrefix().asString());
        Assertions.assertEquals("c=/a", a.asString());
    }

    @Test
    public void test11() {
        DefaultNutsArgument a = new DefaultNutsArgument("c");
        Assertions.assertTrue(a.isNonOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("c", a.getKey().asString());
        Assertions.assertNull(a.getStringValue());
        Assertions.assertNull(a.getOptionName().asString());
        Assertions.assertNull(a.getOptionPrefix().asString());
        Assertions.assertEquals("c", a.asString());
    }

    @Test
    public void test12() {
        DefaultNutsArgument a = new DefaultNutsArgument("!//c=30");
        Assertions.assertTrue(a.isNonOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("!//c=30", a.getKey().asString());
        Assertions.assertNull(a.getStringValue());
        Assertions.assertNull(a.getOptionName().asString());
        Assertions.assertNull(a.getOptionPrefix().asString());
        Assertions.assertEquals("!//c=30", a.asString());
    }

    @Test
    public void test13() {
        DefaultNutsArgument a = new DefaultNutsArgument("!");
        Assertions.assertTrue(a.isNonOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("!", a.getKey().asString());
        Assertions.assertNull(a.getStringValue());
        Assertions.assertNull(a.getOptionName().asString());
        Assertions.assertNull(a.getOptionPrefix().asString());
        Assertions.assertEquals("!", a.asString());
    }

    @Test
    public void test14() {
        DefaultNutsArgument a = new DefaultNutsArgument("");
        Assertions.assertTrue(a.isNonOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("", a.getKey().asString());
        Assertions.assertNull(a.getStringValue());
        Assertions.assertNull(a.getOptionName().asString());
        Assertions.assertNull(a.getOptionPrefix().asString());
        Assertions.assertEquals("", a.asString());
    }
}
