package net.thevpc.nuts.core.test.whitebox;

import net.thevpc.nuts.*;
import org.junit.jupiter.api.Test;

import java.io.PrintStream;

public class TestDev {
    @Test
    void test(){
        NutsWorkspace ws = Nuts.openWorkspace();
        NutsSession session = ws.createSession();

        NutsString s=session.getWorkspace().formats().text().toString(
                NutsMessage.cstyle(
                        NutsString.of("%s"), "Hello"
                ), session
        );

        session.out().printf("%s\n","Hello");
        session.out().printf("%s",NutsMessage.cstyle(NutsString.of("Hello")));
    }
}
