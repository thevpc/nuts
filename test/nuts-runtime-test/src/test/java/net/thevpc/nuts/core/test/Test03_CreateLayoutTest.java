/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.*;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NutsPath;
import org.junit.jupiter.api.*;

import java.io.File;

/**
 * @author thevpc
 */
public class Test03_CreateLayoutTest {

    private static final int NSH_BUILTINS = 0;// 34;
    private static final int NDI_COMPANIONS = 0;//1;

    @Test
    public void customLayout_reload() throws Exception {
//        String test_id = TestUtils.getCallerMethodId();
//        File base = new File("./runtime/test/" + test_id).getCanonicalFile();
//        CoreIOUtils.delete(null, base);
//        TestUtils.resetLinuxFolders();
        File testBaseFolder = TestUtils.getTestBaseFolder();
        NutsSession sessionOne = TestUtils.openNewTestWorkspace(
                TestUtils.sarr(
                        TestUtils.createSysDirs(testBaseFolder),
                        //            "--verbose",
                        "--trace",
                        "info"
                )
        );
        for (NutsStoreLocation value : NutsStoreLocation.values()) {
            sessionOne.out().printf("%s %s%n", value, sessionOne.locations().getStoreLocation(value));
        }

        NutsSession sessionAgain = TestUtils.openExistingTestWorkspace(
                TestUtils.sarr(
                        TestUtils.createSysDirs(testBaseFolder),
                        //            "--verbose",
                        "-!Z",
                        "--trace",
                        "info"
                ));

        for (NutsStoreLocation value : NutsStoreLocation.values()) {
            Assertions.assertEquals(
                    sessionOne.locations().getStoreLocation(value),
                    sessionAgain.locations().getStoreLocation(value)
            );
        }
    }

//    @Test
//    public void customLayout_use_standard() {
//        String test_id = TestUtils.getCallerMethodId();
//        Assumptions.assumeTrue(Nuts.getPlatformOsFamily() == NutsOsFamily.LINUX);
//        NutsWorkspace ws = TestUtils.openNewTestWorkspace("--verbose", "--yes", "info");
//        NutsId ndiId = ws.search().installed().id("nsh").getResultIds().singleton();
//        Assertions.assertTrue(ndiId.getVersion().getValue().startsWith(NUTS_VERSION + "."));
//        Assertions.assertEquals(
//                createNamesSet("nsh"),
//                listNamesSet(new File(TestUtils.LINUX_CONFIG, "default-workspace/config/" + NutsConstants.Folders.ID + "/net/thevpc/nuts/toolbox"), File::isDirectory)
//        );
//        Assertions.assertEquals(
//                NSH_BUILTINS,
//                listNamesSet(new File(TestUtils.LINUX_APPS, "default-workspace/apps/net/thevpc/nuts/nuts/" + TestUtils.NUTS_VERSION), x -> x.getName().endsWith(NutsConstants.Files.NUTS_COMMAND_FILE_EXTENSION)).size()
//        );
//        Assertions.assertEquals(
//                NDI_COMPANIONS,
//                listNamesSet(new File(TestUtils.LINUX_APPS, "default-workspace/apps/net/thevpc/nuts/nuts/" + ndiId.getVersion()+"/bin"), x -> x.isFile() && !x.getName().startsWith(".")).size()
//        );
//        Assertions.assertEquals(
//                3,
//                listNamesSet(new File(TestUtils.LINUX_CACHE, "default-workspace/cache/" + NutsConstants.Folders.ID), x -> x.isDirectory()).size()
//        );
////        Assertions.assertEquals(
////                false,
////                new File(base, "repositories/system/repositories/system-ref/system-ref").exists()
////        );
//    }


    @Test
    public void customLayout_use_standalone() {
        NutsSession session = TestUtils.openNewTestWorkspace("--embedded", "--standalone");
        if (NDI_COMPANIONS > 0) {
            NutsId nshId = null;
            try {
                nshId = session.search().setInstallStatus(NutsInstallStatusFilters.of(session).byInstalled(true)).addId("nsh")
                        .setDistinct(true).getResultIds().singleton();
            } catch (Exception ex) {
                nshId = session.search().setInstallStatus(NutsInstallStatusFilters.of(session).byInstalled(true)).addId("nsh")
                        .setDistinct(true).getResultIds().singleton();
            }
            Assertions.assertTrue(nshId.getVersion().getValue().startsWith(TestUtils.NUTS_VERSION + "."));
        }
        NutsPath c = session.locations().getStoreLocation(NutsStoreLocation.CONFIG);
        TestUtils.println(c);
        File base = session.getWorkspace().getLocation().toFile().toFile();
        TestUtils.println(new File(base, "config").getPath());
        for (NutsStoreLocation value : NutsStoreLocation.values()) {
            session.out().printf("%s %s%n", value, session.locations().getStoreLocation(value));
        }
        Assertions.assertEquals(
                NutsPath.of(base, session).resolve("apps"),
                session.locations().getStoreLocation(NutsStoreLocation.APPS)
        );
        Assertions.assertEquals(
                NutsPath.of(base, session).resolve("cache"),
                session.locations().getStoreLocation(NutsStoreLocation.CACHE)
        );
    }

    @Test
    public void customLayout() throws Exception {
        String test_id = "customLayout_use_export";
        File base = TestUtils.getTestBaseFolder();

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

        NutsSession w = TestUtils.runExistingTestWorkspace("--system-config-home", new File(base, "system.config.ignored").getPath(),
                "info");
        TestUtils.println("==========================");
        w.info().println();
        TestUtils.println("==========================");
        TestUtils.println(new File(base, "system.apps").getPath());
        TestUtils.println(w.locations().getStoreLocation(NutsStoreLocation.APPS));
        Assertions.assertEquals(
                NutsPath.of(new File(base, "system.apps"), w),
                w.locations().getStoreLocation(NutsStoreLocation.APPS)
        );
        Assertions.assertEquals(
                NutsPath.of(new File(base, "system.config"), w),
                w.locations().getStoreLocation(NutsStoreLocation.CONFIG)
        );
        Assertions.assertEquals(
                NutsPath.of(new File(base, "system.var"), w),
                w.locations().getStoreLocation(NutsStoreLocation.VAR)
        );
        Assertions.assertEquals(
                NutsPath.of(new File(base, "system.log"), w),
                w.locations().getStoreLocation(NutsStoreLocation.LOG)
        );
        Assertions.assertEquals(
                NutsPath.of(new File(base, "system.temp"), w),
                w.locations().getStoreLocation(NutsStoreLocation.TEMP)
        );
        Assertions.assertEquals(
                NutsPath.of(new File(base, "system.cache"), w),
                w.locations().getStoreLocation(NutsStoreLocation.CACHE)
        );
        Assertions.assertEquals(
                NutsPath.of(new File(base, "system.lib"), w),
                w.locations().getStoreLocation(NutsStoreLocation.LIB)
        );
        Assertions.assertEquals(
                NutsPath.of(new File(base, "system.run"), w),
                w.locations().getStoreLocation(NutsStoreLocation.RUN)
        );

        w = TestUtils.openNewTestWorkspace(//            "--workspace", "default-workspace",
//            "--workspace", new File(base, "system.config/default-workspace").getPath(),
                "info");
        TestUtils.println(w.locations().getStoreLocation(NutsStoreLocation.APPS));
        Assertions.assertEquals(
                NutsPath.of(new File(base, "apps"), w),
                w.locations().getStoreLocation(NutsStoreLocation.APPS)
        );
        Assertions.assertEquals(
                NutsPath.of(new File(base, "config"), w),
                w.locations().getStoreLocation(NutsStoreLocation.CONFIG)
        );
        Assertions.assertEquals(
                NutsPath.of(new File(base, "var"), w),
                w.locations().getStoreLocation(NutsStoreLocation.VAR)
        );
        Assertions.assertEquals(
                NutsPath.of(new File(base, "log"), w),
                w.locations().getStoreLocation(NutsStoreLocation.LOG)
        );
        Assertions.assertEquals(
                NutsPath.of(new File(base, "temp"), w),
                w.locations().getStoreLocation(NutsStoreLocation.TEMP)
        );
        Assertions.assertEquals(
                NutsPath.of(new File(base, "cache"), w),
                w.locations().getStoreLocation(NutsStoreLocation.CACHE)
        );
        Assertions.assertEquals(
                NutsPath.of(new File(base, "lib"), w),
                w.locations().getStoreLocation(NutsStoreLocation.LIB)
        );
        Assertions.assertEquals(
                NutsPath.of(new File(base, "run"), w),
                w.locations().getStoreLocation(NutsStoreLocation.RUN)
        );
    }

}
