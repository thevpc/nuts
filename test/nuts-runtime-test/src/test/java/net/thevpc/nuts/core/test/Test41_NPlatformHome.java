package net.thevpc.nuts.core.test;

import net.thevpc.nuts.NOsFamily;
import net.thevpc.nuts.NStoreType;
import net.thevpc.nuts.util.NPlatformHome;
import org.junit.jupiter.api.Test;

public class Test41_NPlatformHome {

    @Test
    public void test01() {
        go(NOsFamily.LINUX, false);
        go(NOsFamily.LINUX, true);
        go(NOsFamily.WINDOWS, false);
        go(NOsFamily.WINDOWS, true);
    }


    private void go(NOsFamily family, boolean system) {
        NPlatformHome z = NPlatformHome.ofPortable(family, system, "user-name");
        String workspaceName = "workspace-name";
        System.out.println();
        System.out.println("-----------------------------------------------------------------------------------------------------------------");
        System.out.printf("%-20s %-8s %-6s %-8s %s%n", "getHome", family, system ? "system" : "user", "", z.getHome());

        for (NStoreType location : NStoreType.values()) {
            System.out.printf("%-20s %-8s %-6s %-8s %s%n", "getStore", family, system ? "system" : "user", location, z.getStore(location));
        }

        System.out.printf("%-20s %-8s %-6s %-8s %s%n", "getWorkspaceLocation", family, system ? "system" : "user", "", z.getWorkspaceLocation(workspaceName));
        for (NStoreType location : NStoreType.values()) {
            System.out.printf("%-20s %-8s %-6s %-8s %s%n", "getWorkspaceStore", family, system ? "system" : "user", location, z.getWorkspaceStore(location, workspaceName));
        }
    }
}