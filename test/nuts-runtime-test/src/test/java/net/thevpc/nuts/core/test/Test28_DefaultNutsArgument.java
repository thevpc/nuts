package net.thevpc.nuts.core.test;

import net.thevpc.nuts.NutsElements;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.standalone.app.cmdline.DefaultNutsArgument;
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
        DefaultNutsArgument a = new DefaultNutsArgument("-a=2", '=',NutsElements.of(session));
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
        DefaultNutsArgument a = new DefaultNutsArgument("-//a=2", '=',NutsElements.of(session));
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
        DefaultNutsArgument a = new DefaultNutsArgument("-!a=2", '=',NutsElements.of(session));
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
        DefaultNutsArgument a = new DefaultNutsArgument("-!a", '=',NutsElements.of(session));
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
        DefaultNutsArgument a = new DefaultNutsArgument("-!=a", '=',NutsElements.of(session));
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
        NutsSession  session= TestUtils.openNewMinTestWorkspace();
        DefaultNutsArgument a = new DefaultNutsArgument("-!=", '=',NutsElements.of(session));
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
        DefaultNutsArgument a = new DefaultNutsArgument("-!", '=',NutsElements.of(session));
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
        DefaultNutsArgument a = new DefaultNutsArgument("-", '=',NutsElements.of(session));
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
        DefaultNutsArgument a = new DefaultNutsArgument("", '=',NutsElements.of(session));
        Assertions.assertTrue(a.isNonOption());
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
        DefaultNutsArgument a = new DefaultNutsArgument("c=/a", '=',NutsElements.of(session));
        Assertions.assertTrue(a.isNonOption());
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
        DefaultNutsArgument a = new DefaultNutsArgument("c", '=',NutsElements.of(session));
        Assertions.assertTrue(a.isNonOption());
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
        DefaultNutsArgument a = new DefaultNutsArgument("!//c=30", '=',NutsElements.of(session));
        Assertions.assertTrue(a.isNonOption());
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
        DefaultNutsArgument a = new DefaultNutsArgument("!", '=',NutsElements.of(session));
        Assertions.assertTrue(a.isNonOption());
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
        DefaultNutsArgument a = new DefaultNutsArgument("", '=',NutsElements.of(session));
        Assertions.assertTrue(a.isNonOption());
        Assertions.assertTrue(a.isActive());
        Assertions.assertFalse(a.isNegated());
        Assertions.assertEquals("", a.getKey().getString());
        Assertions.assertNull(a.getValue().getString());
        Assertions.assertNull(a.getOptionName().getString());
        Assertions.assertNull(a.getOptionPrefix().getString());
        Assertions.assertEquals("", a.getString());
    }
}
