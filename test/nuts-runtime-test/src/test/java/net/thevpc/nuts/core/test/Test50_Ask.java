package net.thevpc.nuts.core.test;

import net.thevpc.nuts.*;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.util.*;

public class Test50_Ask {

    //@Test
    public static void main(String[] args) {
        NSession session = TestUtils.openNewMinTestWorkspace();
        char[] youDontLike = NAsk.of(session)
                .forPassword(NMsg.ofC("Ask me something %s", "you dont like"))
                .getValue();
        session.out().println(new String(youDontLike));
    }
}
