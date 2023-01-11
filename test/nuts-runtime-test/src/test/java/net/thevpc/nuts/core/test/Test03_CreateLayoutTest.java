/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.*;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.io.NPath;
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
        NSession sessionOne = TestUtils.openNewTestWorkspace(
                TestUtils.sarr(
                        TestUtils.createSysDirs(testBaseFolder),
                        //            "--verbose",
                        "--trace",
                        "info"
                )
        );
        for (NStoreLocation value : NStoreLocation.values()) {
            sessionOne.out().println(NMsg.ofC("%s %s", value, NLocations.of(sessionOne).getStoreLocation(value)));
        }

        NSession sessionAgain = TestUtils.openExistingTestWorkspace(
                TestUtils.sarr(
                        TestUtils.createSysDirs(testBaseFolder),
                        //            "--verbose",
                        "-!Z",
                        "--trace",
                        "info"
                ));

        for (NStoreLocation value : NStoreLocation.values()) {
            Assertions.assertEquals(
                    NLocations.of(sessionOne).getStoreLocation(value),
                    NLocations.of(sessionAgain).getStoreLocation(value)
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
        NSession session = TestUtils.openNewTestWorkspace("--embedded", "--standalone");
        if (NDI_COMPANIONS > 0) {
            NId nshId = null;
            try {
                nshId = NSearchCommand.of(session).setInstallStatus(NInstallStatusFilters.of(session).byInstalled(true)).addId("nsh")
                        .setDistinct(true).getResultIds().singleton();
            } catch (Exception ex) {
                nshId = NSearchCommand.of(session).setInstallStatus(NInstallStatusFilters.of(session).byInstalled(true)).addId("nsh")
                        .setDistinct(true).getResultIds().singleton();
            }
            Assertions.assertTrue(nshId.getVersion().getValue().startsWith(TestUtils.NUTS_VERSION + "."));
        }
        NPath c = NLocations.of(session).getStoreLocation(NStoreLocation.CONFIG);
        TestUtils.println(c);
        File base = session.getWorkspace().getLocation().toFile().toFile();
        TestUtils.println(new File(base, "config").getPath());
        for (NStoreLocation value : NStoreLocation.values()) {
            session.out().println(NMsg.ofC("%s %s", value, NLocations.of(session).getStoreLocation(value)));
        }
        Assertions.assertEquals(
                NPath.of(base, session).resolve("apps"),
                NLocations.of(session).getStoreLocation(NStoreLocation.APPS)
        );
        Assertions.assertEquals(
                NPath.of(base, session).resolve("cache"),
                NLocations.of(session).getStoreLocation(NStoreLocation.CACHE)
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

        NSession w = TestUtils.runExistingTestWorkspace("--system-config-home", new File(base, "system.config.ignored").getPath(),
                "info");
        TestUtils.println("==========================");
        NInfoCommand.of(w).println();
        TestUtils.println("==========================");
        TestUtils.println(new File(base, "system.apps").getPath());
        NLocations locations = NLocations.of(w);
        TestUtils.println(locations.getStoreLocation(NStoreLocation.APPS));
        Assertions.assertEquals(
                NPath.of(new File(base, "system.apps"), w),
                locations.getStoreLocation(NStoreLocation.APPS)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "system.config"), w),
                locations.getStoreLocation(NStoreLocation.CONFIG)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "system.var"), w),
                locations.getStoreLocation(NStoreLocation.VAR)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "system.log"), w),
                locations.getStoreLocation(NStoreLocation.LOG)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "system.temp"), w),
                locations.getStoreLocation(NStoreLocation.TEMP)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "system.cache"), w),
                locations.getStoreLocation(NStoreLocation.CACHE)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "system.lib"), w),
                locations.getStoreLocation(NStoreLocation.LIB)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "system.run"), w),
                locations.getStoreLocation(NStoreLocation.RUN)
        );

        w = TestUtils.openNewTestWorkspace(//            "--workspace", "default-workspace",
//            "--workspace", new File(base, "system.config/default-workspace").getPath(),
                "info");
        TestUtils.println(locations.getStoreLocation(NStoreLocation.APPS));
        Assertions.assertEquals(
                NPath.of(new File(base, "apps"), w),
                locations.getStoreLocation(NStoreLocation.APPS)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "config"), w),
                locations.getStoreLocation(NStoreLocation.CONFIG)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "var"), w),
                locations.getStoreLocation(NStoreLocation.VAR)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "log"), w),
                locations.getStoreLocation(NStoreLocation.LOG)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "temp"), w),
                locations.getStoreLocation(NStoreLocation.TEMP)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "cache"), w),
                locations.getStoreLocation(NStoreLocation.CACHE)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "lib"), w),
                locations.getStoreLocation(NStoreLocation.LIB)
        );
        Assertions.assertEquals(
                NPath.of(new File(base, "run"), w),
                locations.getStoreLocation(NStoreLocation.RUN)
        );
    }

}
