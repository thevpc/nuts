package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.util.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class MemorySizeTest {

    @BeforeAll
    static void init() {
        TestUtils.openNewMinTestWorkspace();
    }



    @Test
    public void test01() {
        NMemorySize z = NMemorySize.parse("0", NMemoryUnit.BYTE).get();
        TestUtils.println(z);
    }
    @Test
    public void test02() {
        NMemorySize z = NMemorySize.parse("123456789", NMemoryUnit.BYTE).get();
        TestUtils.println(z);
        NMemorySize n = z.normalize();
        TestUtils.println(n);
    }


}
