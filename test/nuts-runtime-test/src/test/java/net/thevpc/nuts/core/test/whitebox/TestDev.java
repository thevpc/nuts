package net.thevpc.nuts.core.test.whitebox;

import net.thevpc.nuts.*;
import net.thevpc.nuts.core.test.utils.TestUtils;
import org.junit.jupiter.api.Test;

public class TestDev {
    @Test
    void test() {
        NutsSession session = TestUtils.openNewTestWorkspace();


        NutsString s = NutsTexts.of(session).toText(
                NutsMessage.cstyle("%s", "Hello")
        );

        session.out().printf("%s\n", "Hello");
        session.out().printf("%s", NutsMessage.cstyle("Hello"));
    }
}
