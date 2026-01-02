package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElementReader;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TsonTest {

    @BeforeAll
    static void init() {
        TestUtils.openNewMinTestWorkspace();
    }


    @Test
    public void test01() {
        String tson = "a:b {a:b} @a a(b,c)[x]";
        NElement parsed = NElementReader.ofTson().read(tson);
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


}
