package net.thevpc.nuts.core.test.whitebox;

import net.thevpc.nuts.NutsMessage;
import net.thevpc.nuts.NutsSession;
import net.thevpc.nuts.NutsString;
import net.thevpc.nuts.NutsWorkspace;
import net.thevpc.nuts.core.test.utils.TestUtils;
import org.junit.jupiter.api.Test;

public class TestDev {
    @Test
    void test() {
        NutsSession session = TestUtils.openNewTestWorkspace();


        NutsString s = session.text().toText(
                NutsMessage.cstyle("%s", "Hello")
        );

        session.out().printf("%s\n", "Hello");
        session.out().printf("%s", NutsMessage.cstyle("Hello"));
    }
}
