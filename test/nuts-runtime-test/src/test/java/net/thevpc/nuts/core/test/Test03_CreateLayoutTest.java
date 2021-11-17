/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.thevpc.nuts.core.test;

import net.thevpc.nuts.*;
import net.thevpc.nuts.core.test.utils.TestUtils;
import net.thevpc.nuts.runtime.standalone.util.CoreIOUtils;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;

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
        NutsSession s = TestUtils.openNewTestWorkspace(
                TestUtils.sarr(
                        TestUtils.createSysDirs(testBaseFolder),
                        //            "--verbose",
                        "--trace",
                        "info"
                )
        );
        for (NutsStoreLocation value : NutsStoreLocation.values()) {
            s.out().printf("%s %s%n", value, s.locations().getStoreLocation(value));
        }

        NutsSession wsAgain = TestUtils.openExistingTestWorkspace(
                TestUtils.sarr(
                        TestUtils.createSysDirs(testBaseFolder),
                        //            "--verbose",
                        "-!Z",
                        "--trace",
                        "info"
                ));

        if (NDI_COMPANIONS > 0) {
            NutsId ndiId = s.search().setInstallStatus(NutsInstallStatusFilters.of(s).byInstalled(true)).addId("nsh")
                    .setDistinct(true)
                    .getResultIds().singleton();
            Assertions.assertTrue(ndiId.getVersion().getValue().startsWith(TestUtils.NUTS_VERSION + "."));
        }

        Assertions.assertEquals(
                (NDI_COMPANIONS > 0) ? TestUtils.createNamesSet("nsh") : TestUtils.createNamesSet(),
                TestUtils.listNamesSet(
                        testBaseFolder.toPath().resolve("system.config").resolve(NutsConstants.Folders.ID).resolve("net/thevpc/nuts/toolbox").toFile()
                        , File::isDirectory)
        );
        Assertions.assertEquals(
                NSH_BUILTINS,
                TestUtils.listNamesSet(
                        testBaseFolder.toPath().resolve("system.apps").resolve(NutsConstants.Folders.ID)
                                .resolve("net/thevpc/nuts/nuts/" + TestUtils.NUTS_VERSION + "/cmd")
                                .toFile()
                        , x -> x.getName().endsWith(NutsConstants.Files.NUTS_COMMAND_FILE_EXTENSION)).size()
        );
        Assertions.assertEquals(
                (NDI_COMPANIONS > 0) ? (NDI_COMPANIONS + 2) : 0,
                TestUtils.listNamesSet(
                        testBaseFolder.toPath().resolve("system.apps").resolve(NutsConstants.Folders.ID)
                                .resolve("net/thevpc/nuts/nuts/" + TestUtils.NUTS_VERSION + "/bin")
                                .toFile()
                        , x -> x.isFile() && !x.getName().startsWith(".")).size()
        );
        Assertions.assertEquals(
                3,
                TestUtils.listNamesSet(
                        testBaseFolder.toPath().resolve("system.cache").resolve(NutsConstants.Folders.ID)
                                .toFile()
                        , x -> x.isDirectory()).size()
        );
//        for (String f : TestUtils.NUTS_STD_FOLDERS) {
//            Assertions.assertFalse(new File(f).exists());
//        }
//        Assertions.assertEquals(
//                false,
//                new File(base, "repositories/system/repositories/system-ref/system-ref").exists()
//        );
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
    public void customLayout_use_export() throws Exception {
        String test_id = TestUtils.getCallerMethodId();
        File base = new File("./runtime/test/" + test_id).getCanonicalFile();
        NutsWorkspace ws1 = TestUtils.openNewTestWorkspace(
                TestUtils.sarr(
                        TestUtils.createSysDirs(base),
                        //            "--verbose",
                        "--trace",
                        "info")).getWorkspace();

        NutsSession s2 = TestUtils.openNewTestWorkspace(TestUtils.sarr(
                TestUtils.createSysDirs(base),
                //            "--verbose",
                "--trace",
                "info"));

        if (NDI_COMPANIONS > 0) {
            NutsId ndiId = s2.search().setInstallStatus(NutsInstallStatusFilters.of(s2).byInstalled(true)).addId("nsh")
                    .setDistinct(true).getResultIds().singleton();
            Assertions.assertTrue(ndiId.getVersion().getValue().startsWith(TestUtils.NUTS_VERSION + "."));
        }

        Assertions.assertEquals(
                (NDI_COMPANIONS > 0) ? TestUtils.createNamesSet("nsh") : TestUtils.createNamesSet(),
                TestUtils.listNamesSet(new File(base, "system.config/" + NutsConstants.Folders.ID + "/net/thevpc/nuts/toolbox"), File::isDirectory)
        );
        Assertions.assertEquals(
                NSH_BUILTINS,
                TestUtils.listNamesSet(new File(base, "system.apps/" + NutsConstants.Folders.ID + "/net/thevpc/nuts/nuts/" + TestUtils.NUTS_VERSION + "/cmd"), x -> x.getName().endsWith(NutsConstants.Files.NUTS_COMMAND_FILE_EXTENSION)).size()
        );
        Assertions.assertEquals(
                (NDI_COMPANIONS > 0) ? (NDI_COMPANIONS + 2) : 0,
                TestUtils.listNamesSet(new File(base, "system.apps/" + NutsConstants.Folders.ID + "/net/thevpc/nuts/nuts/" + TestUtils.NUTS_VERSION + "/bin"), x -> x.isFile() && !x.getName().startsWith(".")).size()
        );
        Assertions.assertEquals(
                3,
                TestUtils.listNamesSet(new File(base, "system.cache/" + NutsConstants.Folders.ID), x -> x.isDirectory()).size()
        );
//        for (String f : TestUtils.NUTS_STD_FOLDERS) {
//            Assertions.assertFalse(new File(f).exists());
//        }
//        Assertions.assertEquals(
//                false,
//                new File(base, "repositories/system/repositories/system-ref/system-ref").exists()
//        );
    }

    @Test
    public void customLayout_use_standalone() throws Exception {
        String test_id = TestUtils.getCallerMethodId();
        File base = new File("./runtime/test/" + test_id).getCanonicalFile();
//        Assumptions.assumeTrue(NutsOsFamily.getCurrent() == NutsOsFamily.LINUX);
        TestUtils.println("Deleting " + base);
        CoreIOUtils.delete(null, base);
        NutsSession s = TestUtils.openNewTestWorkspace("--embedded");
        if (NDI_COMPANIONS > 0) {
            NutsId nshId = null;
            try {
                nshId = s.search().setInstallStatus(NutsInstallStatusFilters.of(s).byInstalled(true)).addId("nsh")
                        .setDistinct(true).getResultIds().singleton();
            } catch (Exception ex) {
                nshId = s.search().setInstallStatus(NutsInstallStatusFilters.of(s).byInstalled(true)).addId("nsh")
                        .setDistinct(true).getResultIds().singleton();
            }
            Assertions.assertTrue(nshId.getVersion().getValue().startsWith(TestUtils.NUTS_VERSION + "."));
        }
        NutsPath c = s.locations().getStoreLocation(NutsStoreLocation.CONFIG);
        TestUtils.println(c);
        TestUtils.println(new File(base, "config").getPath());
        for (NutsStoreLocation value : NutsStoreLocation.values()) {
            s.out().printf("%s %s%n", value, s.locations().getStoreLocation(value));
        }

        Assertions.assertEquals(
                (NDI_COMPANIONS > 0) ? TestUtils.createNamesSet("nsh") : TestUtils.createNamesSet(),
                TestUtils.listNamesSet(new File(base, "/config/" + NutsConstants.Folders.ID + "/net/thevpc/nuts/toolbox"), File::isDirectory)
        );
        Assertions.assertEquals(
                NSH_BUILTINS,
                TestUtils.listNamesSet(new File(base, "apps/" + NutsConstants.Folders.ID + "/net/thevpc/nuts/nuts/" + TestUtils.NUTS_VERSION + "/cmd"), x -> x.getName().endsWith(NutsConstants.Files.NUTS_COMMAND_FILE_EXTENSION)).size()
        );
        Assertions.assertEquals(
                (NDI_COMPANIONS > 0) ? (NDI_COMPANIONS + 2) : 0, //2=nuts and nuts-term
                TestUtils.listNamesSet(new File(base, "apps/" + NutsConstants.Folders.ID + "/net/thevpc/nuts/nuts/" + TestUtils.NUTS_VERSION + "/bin"), x -> x.isFile() && !x.getName().startsWith(".")).size()
        );
        Assertions.assertEquals(
                TestUtils.createNamesSet("com", "net", "org"),
                TestUtils.listNamesSet(new File(base, "cache/" + NutsConstants.Folders.ID), x -> x.isDirectory())
        );
//        for (String f : TestUtils.NUTS_STD_FOLDERS) {
//            Assertions.assertFalse(f + " should not exist", new File(f).exists());
//        }
//        Assertions.assertEquals(
//                false,
//                new File(base, "repositories/system/repositories/system-ref/system-ref").exists()
//        );
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
                NutsPath.of(new File(base, "system.apps"),w),
                w.locations().getStoreLocation(NutsStoreLocation.APPS)
        );
        Assertions.assertEquals(
                NutsPath.of(new File(base, "system.config"),w),
                w.locations().getStoreLocation(NutsStoreLocation.CONFIG)
        );
        Assertions.assertEquals(
                NutsPath.of(new File(base, "system.var"),w),
                w.locations().getStoreLocation(NutsStoreLocation.VAR)
        );
        Assertions.assertEquals(
                NutsPath.of(new File(base, "system.log"),w),
                w.locations().getStoreLocation(NutsStoreLocation.LOG)
        );
        Assertions.assertEquals(
                NutsPath.of(new File(base, "system.temp"),w),
                w.locations().getStoreLocation(NutsStoreLocation.TEMP)
        );
        Assertions.assertEquals(
                NutsPath.of(new File(base, "system.cache"),w),
                w.locations().getStoreLocation(NutsStoreLocation.CACHE)
        );
        Assertions.assertEquals(
                NutsPath.of(new File(base, "system.lib"),w),
                w.locations().getStoreLocation(NutsStoreLocation.LIB)
        );
        Assertions.assertEquals(
                NutsPath.of(new File(base, "system.run"),w),
                w.locations().getStoreLocation(NutsStoreLocation.RUN)
        );

        w = TestUtils.openNewTestWorkspace(//            "--workspace", "default-workspace",
//            "--workspace", new File(base, "system.config/default-workspace").getPath(),
                "info");
        TestUtils.println(w.locations().getStoreLocation(NutsStoreLocation.APPS));
        Assertions.assertEquals(
                NutsPath.of(new File(base, "apps"),w),
                w.locations().getStoreLocation(NutsStoreLocation.APPS)
        );
        Assertions.assertEquals(
                NutsPath.of(new File(base, "config"),w),
                w.locations().getStoreLocation(NutsStoreLocation.CONFIG)
        );
        Assertions.assertEquals(
                NutsPath.of(new File(base, "var"),w),
                w.locations().getStoreLocation(NutsStoreLocation.VAR)
        );
        Assertions.assertEquals(
                NutsPath.of(new File(base, "log"),w),
                w.locations().getStoreLocation(NutsStoreLocation.LOG)
        );
        Assertions.assertEquals(
                NutsPath.of(new File(base, "temp"),w),
                w.locations().getStoreLocation(NutsStoreLocation.TEMP)
        );
        Assertions.assertEquals(
                NutsPath.of(new File(base, "cache"),w),
                w.locations().getStoreLocation(NutsStoreLocation.CACHE)
        );
        Assertions.assertEquals(
                NutsPath.of(new File(base, "lib"),w),
                w.locations().getStoreLocation(NutsStoreLocation.LIB)
        );
        Assertions.assertEquals(
                NutsPath.of(new File(base, "run"),w),
                w.locations().getStoreLocation(NutsStoreLocation.RUN)
        );
    }

}
