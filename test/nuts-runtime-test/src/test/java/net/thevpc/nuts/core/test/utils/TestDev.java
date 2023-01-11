package net.thevpc.nuts.core.test.utils;

import net.thevpc.nuts.*;
import net.thevpc.nuts.text.NTexts;
import org.junit.jupiter.api.Test;

public class TestDev {
    @Test
    void test() {
        NSession session = TestUtils.openNewTestWorkspace();


        NString s = NTexts.of(session).ofText(
                NMsg.ofC("%s", "Hello")
        );

        session.out().println(NMsg.ofC("%s", "Hello"));
        session.out().println(NMsg.ofC("%s", NMsg.ofPlain("Hello")));
    }
}
