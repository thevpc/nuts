package net.thevpc.nuts.core.test.utils;

import net.thevpc.nuts.*;
import net.thevpc.nuts.text.NTexts;
import org.junit.jupiter.api.Test;

public class TestDev {
    @Test
    void test() {
        NSession session = TestUtils.openNewTestWorkspace();


        NString s = NTexts.of(session).ofText(
                NMsg.ofCstyle("%s", "Hello")
        );

        session.out().printf("%s\n", "Hello");
        session.out().printf("%s", NMsg.ofPlain("Hello"));
    }
}
