package net.thevpc.nuts.core.test.heavy;

import net.thevpc.nuts.cmdline.NArg;
import net.thevpc.nuts.command.NExec;
import net.thevpc.nuts.core.test.utils.TestUtils;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class HeavyInstallTest {
    @BeforeAll
    public static void init() {
        TestUtils.openNewMinTestWorkspace("---local-urls","--verbose");
    }

    @Test
    public void test01() {
        NExec.of("org.apache.netbeans:netbeans","--help").failFast(true).run();
    }

}
