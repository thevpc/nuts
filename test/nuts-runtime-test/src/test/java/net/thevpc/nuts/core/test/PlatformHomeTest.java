package net.thevpc.nuts.core.test;

import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.platform.*;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class PlatformHomeTest {
    @BeforeAll
    public static void init() {
        TestUtils.openNewTestWorkspace();
    }

    @Test
    public void test01() {
        go(NOsFamily.LINUX, false);
        go(NOsFamily.LINUX, true);
        go(NOsFamily.WINDOWS, false);
        go(NOsFamily.WINDOWS, true);
    }

    @Test
    public void test02() {
        NExecutionEngineLocation z = NExecutionEngines.of().findExecutionEngineByVersion(NExecutionEngineFamily.JAVA, "[1.8,6]").orNull();
        TestUtils.println(z);
    }


    private void go(NOsFamily family, boolean system) {
        NPlatformHome z = NPlatformHome.ofPortable(family, system, "user-name");
        String workspaceName = "workspace-name";
        TestUtils.println();
        TestUtils.println("-----------------------------------------------------------------------------------------------------------------");
        TestUtils.printfln("%-20s %-8s %-6s %-8s %s", "getHome", family, system ? "system" : "user", "", z.home());

        for (NStoreType location : NStoreType.values()) {
            TestUtils.printfln("%-20s %-8s %-6s %-8s %s", "getStore", family, system ? "system" : "user", location, z.getStore(location));
        }

        TestUtils.printfln("%-20s %-8s %-6s %-8s %s", "getWorkspaceLocation", family, system ? "system" : "user", "", z.getWorkspaceLocation(workspaceName));
        for (NStoreType location : NStoreType.values()) {
            TestUtils.printfln("%-20s %-8s %-6s %-8s %s", "getWorkspaceStore", family, system ? "system" : "user", location, z.getWorkspaceStore(location, workspaceName));
        }
    }
}
