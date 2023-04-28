package net.thevpc.nuts.core.test;

import net.thevpc.nuts.*;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NPath;
import net.thevpc.nuts.runtime.standalone.workspace.NWorkspaceUtils;
import net.thevpc.nuts.util.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Type;
import java.util.List;

public class Test50_Ask {

    //@Test
    public static void main(String[] args) {
        NSession session = TestUtils.openNewMinTestWorkspace();
        char[] youDontLike = session.getTerminal().ask()
                .resetLine()
                .forPassword(NMsg.ofC("Ask me something %s", "you dont like"))
                .getValue();
        session.out().println(new String(youDontLike));
    }
}
