package net.thevpc.nuts.core.test;

import net.thevpc.nuts.*;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.util.*;
import org.junit.jupiter.api.BeforeAll;

public class Test50_Ask {
    @BeforeAll
    public static void init() {
        TestUtils.openNewTestWorkspace();
    }
    //@Test
    public static void main(String[] args) {
        char[] youDontLike = NAsk.of()
                .forPassword(NMsg.ofC("Ask me something %s", "you dont like"))
                .getValue();
        NOut.println(new String(youDontLike));
    }
}
