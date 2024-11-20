package net.thevpc.nuts.core.test.utils;

import net.thevpc.nuts.*;
import net.thevpc.nuts.text.NString;
import net.thevpc.nuts.text.NTexts;
import net.thevpc.nuts.util.NMsg;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class TestDev {
    @BeforeAll
    public static void init() {
        TestUtils.openNewTestWorkspace();
    }

    @Test
    void test() {
        NString s = NTexts.of().ofText(
                NMsg.ofC("%s", "Hello")
        );

        NSession.get().out().println(NMsg.ofC("%s", "Hello"));
        NSession.get().out().println(NMsg.ofC("%s", NMsg.ofPlain("Hello")));
    }
}
