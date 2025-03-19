package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.elem.NElement;
import net.thevpc.nuts.elem.NElements;
import net.thevpc.nuts.util.NMemorySize;
import net.thevpc.nuts.util.NMemoryUnit;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class Test58_Tson {

    @BeforeAll
    static void init() {
        TestUtils.openNewMinTestWorkspace();
    }


    @Test
    public void test01() {
        String tson = "a:b {a:b} @a a(b,c)[x]";
        NElement parsed = NElements.of().tson().parse(tson);
        TestUtils.println(parsed);
    }


}
