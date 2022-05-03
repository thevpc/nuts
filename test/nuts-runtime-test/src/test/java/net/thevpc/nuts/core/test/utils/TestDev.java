package net.thevpc.nuts.core.test.utils;

import net.thevpc.nuts.*;
import net.thevpc.nuts.text.NutsTexts;
import org.junit.jupiter.api.Test;

public class TestDev {
    @Test
    void test() {
        NutsSession session = TestUtils.openNewTestWorkspace();


        NutsString s = NutsTexts.of(session).toText(
                NutsMessage.ofCstyle("%s", "Hello")
        );

        session.out().printf("%s\n", "Hello");
        session.out().printf("%s", NutsMessage.ofPlain("Hello"));
    }
}
