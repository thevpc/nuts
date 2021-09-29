/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test.blackbox;

import net.thevpc.nuts.*;
import net.thevpc.nuts.core.test.utils.TestUtils;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.thevpc.nuts.runtime.core.util.CoreIOUtils;
import org.junit.jupiter.api.*;

/**
 *
 * @author thevpc
 */
public class Test04_CreateLoadLinuxTest {

    private Map<String, String> clearUpExtraSystemProperties;

    @Test
    public void customLayout() throws Exception {
        String test_id = "customLayout_use_export";
        File base = TestUtils.getTestBaseFolder();
        Map<String, String> extraProperties = new HashMap<>();
        extraProperties.put("nuts.export.always-show-command", "true");
//        for (NutsStoreFolder folder : NutsStoreFolder.values()) {
//            for (NutsStoreLocationLayout layout : NutsStoreLocationLayout.values()) {
//                extraProperties.put("nuts.export.home." + folder.id() + "." + layout.id(), new File(base, folder.name().toLowerCase() + "." + layout.name().toLowerCase()).getPath());
//            }
//        }
        TestUtils.setSystemProperties(extraProperties);
        clearUpExtraSystemProperties = extraProperties;

//        CoreIOUtils.delete(null,base);
        TestUtils.runNewTestWorkspace(
                "--system-apps-home", new File(base, "system.apps").getPath(),
                "--system-config-home", new File(base, "system.config").getPath(),
                "--system-var-home", new File(base, "system.var").getPath(),
                "--system-log-home", new File(base, "system.log").getPath(),
                "--system-temp-home", new File(base, "system.temp").getPath(),
                "--system-cache-home", new File(base, "system.cache").getPath(),
                "--system-lib-home", new File(base, "system.lib").getPath(),
                "--system-run-home", new File(base, "system.run").getPath(),
                //            "--verbose",
                "--skip-companions",
                "info");

        NutsWorkspace w = TestUtils.runExistingTestWorkspace("--system-config-home", new File(base, "system.config.ignored").getPath(),
                "info").getWorkspace();
        TestUtils.println("==========================");
        w = w.createSession().getWorkspace();
        w.info().println();
        TestUtils.println("==========================");
        TestUtils.println(new File(base, "system.apps").getPath());
        TestUtils.println(w.locations().getStoreLocation(NutsStoreLocation.APPS));
        Assertions.assertEquals(
                new File(base, "system.apps").getPath(),
                w.locations().getStoreLocation(NutsStoreLocation.APPS)
        );
        Assertions.assertEquals(
                new File(base, "system.config").getPath(),
                w.locations().getStoreLocation(NutsStoreLocation.CONFIG)
        );
        Assertions.assertEquals(
                new File(base, "system.var").getPath(),
                w.locations().getStoreLocation(NutsStoreLocation.VAR)
        );
        Assertions.assertEquals(
                new File(base, "system.log").getPath(),
                w.locations().getStoreLocation(NutsStoreLocation.LOG)
        );
        Assertions.assertEquals(
                new File(base, "system.temp").getPath(),
                w.locations().getStoreLocation(NutsStoreLocation.TEMP)
        );
        Assertions.assertEquals(
                new File(base, "system.cache").getPath(),
                w.locations().getStoreLocation(NutsStoreLocation.CACHE)
        );
        Assertions.assertEquals(
                new File(base, "system.lib").getPath(),
                w.locations().getStoreLocation(NutsStoreLocation.LIB)
        );
        Assertions.assertEquals(
                new File(base, "system.run").getPath(),
                w.locations().getStoreLocation(NutsStoreLocation.RUN)
        );

        w = TestUtils.openNewTestWorkspace(//            "--workspace", "default-workspace",
//            "--workspace", new File(base, "system.config/default-workspace").getPath(),
                "info").getWorkspace();
        w = w.createSession().getWorkspace();
        TestUtils.println(w.locations().getStoreLocation(NutsStoreLocation.APPS));
        Assertions.assertEquals(
                new File(base, "apps").getPath(),
                w.locations().getStoreLocation(NutsStoreLocation.APPS)
        );
        Assertions.assertEquals(
                new File(base, "config").getPath(),
                w.locations().getStoreLocation(NutsStoreLocation.CONFIG)
        );
        Assertions.assertEquals(
                new File(base, "var").getPath(),
                w.locations().getStoreLocation(NutsStoreLocation.VAR)
        );
        Assertions.assertEquals(
                new File(base, "log").getPath(),
                w.locations().getStoreLocation(NutsStoreLocation.LOG)
        );
        Assertions.assertEquals(
                new File(base, "temp").getPath(),
                w.locations().getStoreLocation(NutsStoreLocation.TEMP)
        );
        Assertions.assertEquals(
                new File(base, "cache").getPath(),
                w.locations().getStoreLocation(NutsStoreLocation.CACHE)
        );
        Assertions.assertEquals(
                new File(base, "lib").getPath(),
                w.locations().getStoreLocation(NutsStoreLocation.LIB)
        );
        Assertions.assertEquals(
                new File(base, "run").getPath(),
                w.locations().getStoreLocation(NutsStoreLocation.RUN)
        );
    }

    @BeforeAll
    public static void setUpClass() throws IOException {
//        TestUtils.stashLinuxFolders();
    }

    @AfterAll
    public static void tearUpClass() throws IOException {
//        TestUtils.unstashLinuxFolders();
        TestUtils.println("####### RUNNING TEST @ "+ TestUtils.getCallerClassSimpleName());
    }

    @BeforeEach
    public void startup() throws IOException {
//        Assumptions.assumeTrue(NutsOsFamily.getCurrent()== NutsOsFamily.LINUX);
//        TestUtils.resetLinuxFolders();
        TestUtils.unsetNutsSystemProperties();
    }

    @AfterEach
    public void cleanup() {
        TestUtils.setSystemProperties(clearUpExtraSystemProperties);
        clearUpExtraSystemProperties = null;
    }

}
